package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.session.SessionStore
import play.api.Logger
import play.api.libs.json._
import com.variant.server.boot.ServerErrorApi._
import java.util.Date
import play.api.mvc.Result
import play.api.mvc.AnyContent
import play.api.libs.json.JsValue
import play.api.http.HeaderNames
import scala.collection.mutable.Map
import com.variant.server.event.ServerEvent

//@Singleton -- Is this for non-shared state controllers?
class EventController @Inject() (store: SessionStore) extends Controller  {
   
   private val logger = Logger(this.getClass)	
 
   /**
    * POST a remote event, i.e. write it off to external storage.
    * test with:
curl -v -H "Content-Type: text/plain; charset=utf-8" \
     -X POST \
     -d '{"sid":"SID","name":"NAME","val":"VALUE","crdate":1476124715698,"params":{"parm1":"foo","parm2":"bar"}}' \
     http://localhost:9000/variant/event
    */
   def post() = VariantAction { req =>

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
               if (name.isEmpty) return MissingParamName.asResult()
               val value = (p \ "value").asOpt[String]
               parsedParams(name.get) = value.getOrElse(null)                            
            })
         })

         // 400 if no required fields 
         if (sid.isEmpty)  {
            MissingProperty.asResult("sid")
         }
         else if (name.isEmpty)  {
            MissingProperty.asResult("name")
         }
         else {    
            val ssn = store.asSession(sid.get)
            if (ssn.isEmpty) {
               SessionExpired.asResult()
            }
            else {
               if (ssn.get.getStateRequest == null) {
                  UnknownState.asResult()   
               }
               else {
                  val event = new ServerEvent(name.get, value.get, new Date(timestamp.getOrElse(System.currentTimeMillis())));   
                  parsedParams.foreach(e => event.setParameter(e._1, e._2))
                  ssn.get.triggerEvent(event)            
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
               case t: Throwable => JsonParseError.asResult(t.getMessage)
            }
         case _ => BadContentType.asResult()
      }
   }   
}

