package com.variant.server.controller

import javax.inject.Inject
import scala.collection.JavaConversions._
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.conn.SessionStore
import play.api.Logger
import com.variant.core.ServerError._
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerSchema
import com.variant.server.conn.Connection
import com.variant.server.conn.ConnectionStore
import play.api.libs.json._
import play.api.mvc.Result
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.api.ServerException
import com.variant.core.schema.State
import com.variant.server.api.Session
import com.variant.core.session.CoreStateRequest
import play.api.mvc.AnyContent
import com.variant.server.impl.SessionImpl
import com.variant.server.impl.StateRequestImpl

//@Singleton -- Is this for non-shared state controllers?
class RequestController @Inject() (
      override val connStore: ConnectionStore, 
      override val ssnStore: SessionStore, 
      server: VariantServer) extends VariantController  {
   
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

      val bodyJson = req.body.asJson.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }      
      
      val sid = (bodyJson \ "sid").asOpt[String]
      val state = (bodyJson \ "state").asOpt[String]
      
      if (sid.isEmpty)
         throw new ServerException.Remote(MissingProperty, "sid")

      if (state.isEmpty) 
         throw new ServerException.Remote(MissingProperty, "state")

      val ssn = ssnStore.getOrBust(sid.get)

      VariantServer.server.runtime.targetForState(ssn, server.schema.get.getState(state.get))

      val response = JsObject(Seq(
         "session" -> JsString(ssn.asInstanceOf[SessionImpl].coreSession.toJson())
      ))
      Ok(response.toString)
   }

   /**
    * Commit a state request.
    * We override the default parser because Play sets it to ignore for the DELETE operation.
    * (More discussion: https://github.com/playframework/playframework/issues/4606)
    */
   def commit() = VariantAction { req =>

      val bodyJson = req.body.asJson.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }
   
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "sid")         
      }
      
      val ssn = ssnStore.getOrBust(sid)
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
	   	stateReq.asInstanceOf[StateRequestImpl].commit(); 
      }
      val response = JsObject(Seq(
         "session" -> JsString(ssn.coreSession.toJson)
      )).toString()
   
      Ok(response)
   }

}
