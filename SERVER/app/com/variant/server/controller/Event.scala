package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.session.SessionStore
import play.api.Logger
import play.api.libs.json.Json
import com.variant.server.UserError
import com.variant.server.RemoteEvent
import java.util.Date
import play.api.mvc.Result
import play.api.mvc.AnyContent
import com.variant.server.UserError

//@Singleton -- Is this for non-shared state controllers?
class Event @Inject() (store: SessionStore) extends Controller  {
   
   private val logger = Logger(this.getClass)	
 
   /**
    * POST a remote event, i.e. write it off to external storage.
    * test with:
curl -v -H "Content-Type: text/plain; charset=utf-8" \
     -X POST \
     -d '{"sid":"SID","name":"NAME","val":"VALUE","crdate":1476124715698,"params":{"parm1":"foo","parm2":"bar"}}' \
     http://localhost:9000/variant/event
    */
   def post() = Action { req =>
      // To be a text, Content-Type header has to be text and supply a charset.
      req.body.asText match {
         case Some(body) => {
            val json = Json.parse(body)
            
            // Parse the input and construct the remote event.
            val sid = (json \ "sid").asOpt[String]
            val name = (json \ "name").asOpt[String]
            val value = (json \ "val").asOpt[String]
            val createDate = (json \ "crdate").asOpt[Long]
            val params = (json \ "params").asOpt[Map[String,String]]
   
            // 400 if no required fields 
            if (sid.isEmpty)  {
               UserError.errors(UserError.MissingProperty).toResult("sid")
            }
            else if (name.isEmpty)  {
               UserError.errors(UserError.MissingProperty).toResult("name")
            }
            else if (value.isEmpty) {
               UserError.errors(UserError.MissingProperty).toResult("value")
            }
            else {    
               val ssn = store.asSession(sid.get)
               if (ssn.isEmpty) {
                  UserError.errors(UserError.SessionExpired).toResult()
               }
               else {
                  if (ssn.get.getStateRequest == null) {
                     UserError.errors(UserError.UnknownState).toResult()   
                  }
                  else {
                     val remoteEvent = new RemoteEvent(name.get, value.get, new Date(createDate.getOrElse(System.currentTimeMillis())));   
                     for ((k,v)<-params.getOrElse(Map.empty)) remoteEvent.setParameter(k, v.asInstanceOf[String])
                     ssn.get.triggerEvent(remoteEvent)               
                     Ok
                  }
               }
            }
         }
         case None => BadRequest("Body expected but was null");
      }
   }
 
}
