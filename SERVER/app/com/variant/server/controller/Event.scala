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
import play.api.libs.json.JsValue
import play.api.http.HeaderNames

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

      def parse(json: JsValue) = {
         
         val sid = (json \ "sid").asOpt[String]
         val name = (json \ "name").asOpt[String]
         val value = (json \ "val").asOpt[String]
         val createDate = (json \ "crdate").asOpt[Long]
         val params = (json \ "params").asOpt[Map[String,String]]

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
                  val remoteEvent = new RemoteEvent(name.get, value.get, new Date(createDate.getOrElse(System.currentTimeMillis())));   
                  for ((k,v)<-params.getOrElse(Map.empty)) remoteEvent.setParameter(k, v.asInstanceOf[String])
                  ssn.get.triggerEvent(remoteEvent)               
                  Ok
               }
            }
         }
      }
      
//      val bodySize = req.headers.get(HeaderNames.CONTENT_LENGTH)

      // TODO: this will only work for text requests which are consumed by default Action without
      // JSON parsing. Write a custom parser that will examine the size before parsing
      // and will not attempt parse if body is empty regardless of the content type header.
      // Probably a composable action that will be in front of all our actions.
      req.contentType match {
         case Some(ct) if ct.equalsIgnoreCase("text/plain") =>
            try {
               parse(Json.parse(req.body.asText.get))
            }
            catch {
               case t: Throwable => UserError.errors(UserError.JsonParseError).asResult(t.getMessage)
            }
         case Some(ct) if ct.equalsIgnoreCase("application/json") => 
            try {
               parse(req.body.asJson.get)
            }
            catch {
               case t: Throwable => UserError.errors(UserError.JsonParseError).asResult(t.getMessage)
            }

         case None => UserError.errors(UserError.BadContentType).asResult()
      }

   }   
}

