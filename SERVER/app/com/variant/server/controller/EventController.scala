package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.conn.SessionStore
import play.api.Logger
import play.api.libs.json._
import com.variant.core.ServerError._
import java.util.Date
import play.api.mvc.Result
import play.api.mvc.AnyContent
import play.api.libs.json.JsValue
import play.api.http.HeaderNames
import scala.collection.mutable.Map
import com.variant.server.event.ServerEvent
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.conn.ConnectionStore
import com.variant.server.boot.VariantServer
import com.variant.server.api.ServerException
import com.variant.server.conn.Connection
import com.variant.server.session.ServerSession

//@Singleton -- Is this for non-shared state controllers?
class EventController @Inject() (override val connStore: ConnectionStore, server: VariantServer) extends VariantController  {
   
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
         
         val scid = (json \ "sid").asOpt[String]
         val name = (json \ "name").asOpt[String]
         val value = (json \ "value").asOpt[String]
         val timestamp = (json \ "ts").asOpt[Long]
         val params = (json \ "params").asOpt[List[JsObject]]

         // 400 if no required fields 
         if (scid.isEmpty)
            throw new ServerException.Remote(MissingProperty, "sid")   

         if (name.isEmpty)
            throw new ServerException.Remote(MissingProperty, "name")
         
         if (value.isEmpty)
            throw new ServerException.Remote(MissingProperty, "value")

         val (cid, sid) = parseScid(scid.get)
         val (conn, ssn) = lookupSession(scid.get).getOrElse {
            throw new ServerException.Remote(SessionExpired)
         }
         
         if (ssn.getStateRequest == null)
            throw new ServerException.Remote(UnknownState)   

         val event = new ServerEvent(name.get, value.get, new Date(timestamp.getOrElse(System.currentTimeMillis())));  
         
         if (params.isDefined) {
            params.get.foreach(p => {
               val name = (p \ "name").asOpt[String].getOrElse {
                  throw new ServerException.Remote(MissingParamName)
               }
               val value = (p \ "value").asOpt[String].getOrElse("")
               event.setParameter(name, value)
            })
         }            
         ssn.triggerEvent(event)            
         Ok
      }
            
      req.contentType match {
         case Some(ct) if ct.equalsIgnoreCase("text/plain") =>
            parse {
               try {
                  Json.parse(req.body.asText.get)
               }
               catch {
                  case t: Throwable => throw new ServerException.Remote(JsonParseError, t.getMessage)
               }
            }
         case _ => ServerErrorRemote(BadContentType).asResult()
      }
   }   
}

