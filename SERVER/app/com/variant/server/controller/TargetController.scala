package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.conn.SessionStore
import play.api.Logger
import com.variant.core.exception.ServerError._
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerSchema
import com.variant.server.conn.Connection
import com.variant.server.conn.ConnectionStore
import play.api.libs.json._
import play.api.mvc.Result
import com.variant.server.boot.ServerErrorRemote

//@Singleton -- Is this for non-shared state controllers?
class TargetController @Inject() (store: SessionStore) extends Controller  {
   
   private val logger = Logger(this.getClass)	

   /**
    * POST: target a session for a state.
    * test with:
curl -v -H "Content-Type: text/plain; charset=utf-8" \
     -X POST \
     -d '{"sid":"SID","state":"STATE"}' \
     http://localhost:9000/variant/target
    */
   def post() = VariantAction { req =>

      def parse(json: JsValue): Result = {
         
         val sid = (json \ "sid").asOpt[String]
         val state = (json \ "state").asOpt[String]
         
         if (sid.isEmpty)  {
            ServerErrorRemote(MissingProperty).asResult("sid")
         }
         else if (state.isEmpty)  {
            ServerErrorRemote(MissingProperty).asResult("state")
         }
         else {    
            val ssn = store.asSession(sid.get)
            if (ssn.isEmpty) {
               ServerErrorRemote(SessionExpired).asResult()
            }
            else {
               Ok("TODO")
            }

         }
      }
      
      req.contentType match {
         case Some(ct) if ct.equalsIgnoreCase("text/plain") =>
            try {
               parse(Json.parse(req.body.asText.get))
            }
            catch {
               case t: Throwable => ServerErrorRemote(JsonParseError).asResult(t.getMessage)
            }
         case _ => ServerErrorRemote(BadContentType).asResult()
      }

   }
  
}
