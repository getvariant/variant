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
import com.variant.server.ServerException
import com.variant.core.schema.State
import com.variant.server.session.ServerSession

//@Singleton -- Is this for non-shared state controllers?
class TargetController @Inject() (override val connStore: ConnectionStore) extends VariantController  {
   
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

      def parse(json: JsValue): (ServerSession, State) = {
         
         val scid = (json \ "sid").asOpt[String]
         val state = (json \ "state").asOpt[String]
         
         if (scid.isEmpty)
            throw new ServerException.Remote(MissingProperty, "sid")
   
         if (state.isEmpty) 
            throw new ServerException.Remote(MissingProperty, "state")

         val (sid,cid) = parseScid(scid.get)
         val ssn = lookupSession(scid.get)

         if (!ssn.isDefined)
            throw new ServerException.Remote(SessionExpired, "state")

         (ssn.get, VariantServer.server.schema.get.getState(state.get))
      }
      
      req.contentType match {
         case Some(ct) if ct.equalsIgnoreCase("text/plain") =>
            try {
               val (ssn, state) = parse(Json.parse(req.body.asText.get))
               VariantServer.server.runtime.targetSessionForState(ssn.coreSession, state);
               Ok("TODO")
            }
            catch {
               case t: Throwable => ServerErrorRemote(JsonParseError).asResult(t.getMessage)
            }
         case _ => ServerErrorRemote(BadContentType).asResult()
      }

   }
  
}
