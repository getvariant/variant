package com.variant.server.controller

import javax.inject.Inject
import scala.collection.JavaConversions._
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
import play.api.mvc.AnyContent

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

      def parse(json: JsValue): (Connection, ServerSession, State) = {
         
         val scid = (json \ "sid").asOpt[String]
         val state = (json \ "state").asOpt[String]
         
         if (scid.isEmpty)
            throw new ServerException.Remote(MissingProperty, "sid")
   
         if (state.isEmpty) 
            throw new ServerException.Remote(MissingProperty, "state")

      val (cid, sid) = parseScid(scid.get)
      val result = lookupSession(scid.get)
      
      if (result.isDefined) {
         val (conn, ssn) = result.get
         logger.debug(s"Found session [$sid]")
         (result.get._1, result.get._2, schema.getState(state.get))
      }
      else
         throw new ServerException.Remote(SessionExpired)

      }
      
      req.contentType match {
         case Some(ct) if ct.equalsIgnoreCase("text/plain") =>
            try {
               val (conn, ssn, state) = parse(Json.parse(req.body.asText.get))
               var stateReq: CoreStateRequest = VariantServer.server.runtime.targetSessionForState(ssn.coreSession, state)
               conn.addSession(ssn)
               val response = JsObject(Seq(
                  "session" -> JsString(ssn.coreSession.toJson())
               ))
              Ok(response.toString)
            }
            catch {
               case e: ServerException => throw e
               case t: Throwable => ServerErrorRemote(JsonParseError).asResult(t.getMessage)
            }
         case _ => ServerErrorRemote(BadContentType).asResult()
      }
   }

   /**
    * Commit a state request.
    * We override the default parser because Play sets it to ignore for the DELETE operation.
    * (More discussion: https://github.com/playframework/playframework/issues/4606)
    */
   def commit() = VariantAction(parse.text(4896)) { req =>

         val (conn, ssn) = parseBody(req.body)
         val stateReq = ssn.getStateRequest
	      val sve = stateReq.getStateVisitedEvent
         
   		// We won't have an event if nothing is instrumented on this state
	      if (sve != null) {
		      sve.getParameterMap().put("$REQ_STATUS", ssn.getStateRequest.getStatus.name);
   			// log all resolved state params as event params.
	      	for ((key, value) <- ssn.getStateRequest.getResolvedParameters()) {
			      sve.getParameterMap().put(key, value);				
		      }
	   		// Trigger state visited event
   	   	ssn.triggerEvent(sve);
   	   	stateReq.commit(); 
   	   	conn.addSession(ssn)
         }
         val response = JsObject(Seq(
            "session" -> JsString(ssn.coreSession.toJson())
         )).toString()
        Ok(response)
   }

}
