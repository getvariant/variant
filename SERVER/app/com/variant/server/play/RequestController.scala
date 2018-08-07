package com.variant.server.play

import javax.inject.Inject
import scala.collection.JavaConversions._
import play.api.mvc.Request
import com.variant.server.boot.SessionStore
import play.api.Logger
import com.variant.core.impl.ServerError._
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
import play.api.mvc.ControllerComponents
import com.variant.server.boot.VariantServer

//@Singleton -- Is this for non-shared state controllers?
class RequestController @Inject() (
      val action: VariantAction,
      val cc: ControllerComponents,
      val server: VariantServer
      ) extends VariantController(cc, server)  {
   
   private val logger = Logger(this.getClass)	
   
   /**
    * POST
    * Create state request by targeting a session.
    */
   def create() = action { req =>
      
      val bodyJson = getBody(req).getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "sid")         
      }
      val stateName = (bodyJson \ "state").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "state")         
      }

      val ssn = server.ssnStore.getOrBust(sid)
      val state = ssn.schemaGen.getState(stateName)

      if (state == null)
         throw new ServerException.Internal("State [%s] not in schema [%s]".format(stateName, ssn.schemaGen.getMeta.getName))
      
      ssn.schemaGen.runtime.targetForState(ssn, state)

      val response = JsObject(Seq(
         "session" -> JsString(ssn.asInstanceOf[SessionImpl].coreSession.toJson())
      ))
      
      Ok(response.toString)
   }

   /**
    * PUT
    * Commit a state request.
    */
   def commit() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerException.Remote(EmptyBody)   
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "sid")         
      }
      
      val ssn = server.ssnStore.getOrBust(sid)
      val stateReq = ssn.getStateRequest
      val sve = stateReq.getStateVisitedEvent
      
		// We won't have an event if nothing is instrumented on this state, or session
      // is disqualified for the test(s) instrumented on this state.
      if (sve != null) {
	      sve.setAttribute("$REQ_STATUS", ssn.getStateRequest.getStatus.name);
			// log all resolved state params as event params.
      	for ((key, value) <- ssn.getStateRequest.getResolvedParameters()) {
		      sve.setAttribute(key, value);				
	      }
   		// Trigger state visited event
	   	ssn.triggerEvent(sve);
      }

      // Actual commit.
      stateReq.asInstanceOf[StateRequestImpl].commit(); 

      val response = JsObject(Seq(
         "session" -> JsString(ssn.coreSession.toJson)
      )).toString()
   
      Ok(response)
   }

}
