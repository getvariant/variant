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
import com.variant.core.session.CoreStateRequest

//@Singleton -- Is this for non-shared state controllers?
class RequestController @Inject() (override val connStore: ConnectionStore) extends VariantController  {
   
   private val logger = Logger(this.getClass)	
   private lazy val schema = VariantServer.server.schema.get
   
   /**
    * POST: Create state request by targeting a session.
    * test with:
curl -v -H "Content-Type: text/plain; charset=utf-8" \
     -X POST \
     -d '{"sid":"SID","state":"STATE"}' \
     http://localhost:9000/variant/target
    */
   def create() = VariantAction { req =>

      def parse(json: JsValue): (ServerSession, State) = {
         
         val scid = (json \ "sid").asOpt[String]
         val state = (json \ "state").asOpt[String]
         
         if (scid.isEmpty)
            throw new ServerException.Remote(MissingProperty, "sid")
   
         if (state.isEmpty) 
            throw new ServerException.Remote(MissingProperty, "state")

         val (sid,cid) = parseScid(scid.get)
         val ssn = lookupSession(scid.get).map(_._2)

         if (!ssn.isDefined)
            throw new ServerException.Remote(SessionExpired, "state")

         (ssn.get, schema.getState(state.get))
      }
      
      req.contentType match {
         case Some(ct) if ct.equalsIgnoreCase("text/plain") =>
            try {
               val (ssn, state) = parse(Json.parse(req.body.asText.get))
               var stateReq: CoreStateRequest = VariantServer.server.runtime.targetSessionForState(ssn.coreSession, state)
               val response = JsObject(Seq(
                  "session" -> JsString(ssn.coreSession.toJson())
               ))
              Ok(response.toString)
            }
            catch {
               case t: Throwable => ServerErrorRemote(JsonParseError).asResult(t.getMessage)
            }
         case _ => ServerErrorRemote(BadContentType).asResult()
      }
   }

   /**
    * Commit a state request.
    */
   def commit() = VariantAction { req =>

      def parse(json: JsValue): (ServerSession, State) = {
         
         val scid = (json \ "sid").asOpt[String]
         val state = (json \ "state").asOpt[String]
         
         if (scid.isEmpty)
            throw new ServerException.Remote(MissingProperty, "sid")
   
         if (state.isEmpty) 
            throw new ServerException.Remote(MissingProperty, "state")

         val (sid,cid) = parseScid(scid.get)
         val ssn = lookupSession(scid.get).map(_._2)

         if (!ssn.isDefined)
            throw new ServerException.Remote(SessionExpired, "state")

         (ssn.get, schema.getState(state.get))
      }
      
      req.contentType match {
         case Some(ct) if ct.equalsIgnoreCase("text/plain") =>
            try {
               val (ssn, state) = parse(Json.parse(req.body.asText.get))
               var stateReq: CoreStateRequest = VariantServer.server.runtime.targetSessionForState(ssn.coreSession, state)
               val response = JsObject(Seq(
                  "session" -> JsString(ssn.coreSession.toJson())
               ))
              Ok(response.toString)
            }
            catch {
               case t: Throwable => ServerErrorRemote(JsonParseError).asResult(t.getMessage)
            }
         case _ => ServerErrorRemote(BadContentType).asResult()
      }
   }

}
