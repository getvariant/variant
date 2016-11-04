package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.session.SessionStore
import play.api.Logger
import play.api.libs.json._
import com.variant.server.UserError
import java.util.Date
import play.api.mvc.Result
import play.api.mvc.AnyContent
import com.variant.server.UserError
import play.api.libs.json.JsValue
import play.api.http.HeaderNames
import scala.collection.mutable.Map
import com.variant.server.event.RemoteEvent

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

      def parse(json: JsValue): Result = {
         
         val sid = (json \ "sid").asOpt[String]
         val name = (json \ "name").asOpt[String]
         val value = (json \ "value").asOpt[String]
         val timestamp = (json \ "ts").asOpt[Long]
         val params = (json \ "params").asOpt[JsArray]

         val parsedParams = Map[String,String]()
         params.map((x:JsArray) => {
            x.as[Array[JsValue]].foreach(p => {
               val name = (p \ "name").asOpt[String] 
               if (name.isEmpty) return UserError.errors(UserError.MissingParamName).asResult()
               val value = (p \ "value").asOpt[String]
               parsedParams(name.get) = value.getOrElse(null)                            
            })
         })

         // 400 if no required fields 
         if (sid.isEmpty)  {
            UserError.errors(UserError.MissingProperty).asResult("sid")
         }
         else if (name.isEmpty)  {
            UserError.errors(UserError.MissingProperty).asResult("name")
         }
         else {    
            val ssn = store.asSession(sid.get)
            if (ssn.isEmpty) {
               UserError.errors(UserError.SessionExpired).asResult()
            }
            else {
               if (ssn.get.getStateRequest == null) {
                  UserError.errors(UserError.UnknownState).asResult()   
               }
               else {
                  val remoteEvent = new RemoteEvent(name.get, value.get, new Date(timestamp.getOrElse(System.currentTimeMillis())));   
                  parsedParams.foreach(e => remoteEvent.setParameter(e._1, e._2))
                  ssn.get.triggerEvent(remoteEvent)            
                  Ok
               }
            }
         }
      }
      
      req.contentType match {
         case Some(ct) if ct.equalsIgnoreCase("text/plain") =>
            try {
               parse(Json.parse(req.body.asText.get))
            }
            catch {
               case t: Throwable => UserError.errors(UserError.JsonParseError).asResult(t.getMessage)
            }
         case _ => UserError.errors(UserError.BadContentType).asResult()
      }
   }   
}

