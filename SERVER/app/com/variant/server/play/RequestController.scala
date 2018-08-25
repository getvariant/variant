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
import com.variant.core.StateRequestStatus
import com.variant.core.StateRequestStatus._
import com.variant.server.api.Session
import com.variant.core.session.CoreStateRequest
import play.api.mvc.AnyContent
import com.variant.server.impl.SessionImpl
import com.variant.server.impl.StateRequestImpl
import play.api.mvc.ControllerComponents
import com.variant.server.boot.VariantServer
import com.variant.core.impl.StateVisitedEvent
import com.variant.server.event.ServerTraceEvent
import com.variant.core.impl.ServerError

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
      
      if (ssn.getStateRequest != null && ssn.getStateRequest.getStatus == InProgress) {
         throw new ServerException.Remote(ACTIVE_REQUEST)                
      }
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
    * Commit or fail a state request. Trigger the implicit state visited event.
    */
   def commit() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerException.Remote(EmptyBody)   
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "sid")         
      }

      val status = {
         val ordinal = (bodyJson \ "status").asOpt[Int].getOrElse {
            throw new ServerException.Remote(MissingProperty, "status")
         }
         StateRequestStatus.values()(ordinal)
      }
      
      if (!status.isIn(Committed, Failed)) 
         throw new ServerException.Remote(InvalidRequestStatus, status.toString())
      
      val attrs = (bodyJson \ "attrs").asOpt[Map[String,String]].getOrElse {
         Map[String,String]()
      }
      

      val ssn = server.ssnStore.getOrBust(sid)
      val stateReq = ssn.getStateRequest.asInstanceOf[StateRequestImpl]
      
      if (stateReq.getStatus == Committed && status == Failed)
			throw new ServerException.Remote(ServerError.CANNOT_FAIL);
      
		else if (stateReq.getStatus == Failed && status == Committed)
			throw new ServerException.Remote(ServerError.CANNOT_COMMIT);
		
		else if (stateReq.getStatus == InProgress) {

         stateReq.asInstanceOf[StateRequestImpl].setStatus(status); 

         // Trigger state visited, but only if we have live experiences
         // at this state. As opposed to custom events, state visited events
         // cannot be orphan because we didn't really visit that state.
         if (!stateReq.getLiveExperiences().isEmpty()) {
            val sve = new StateVisitedEvent(stateReq.getState, status, attrs)                  
      	   ssn.triggerEvent(new ServerTraceEvent(sve));
         }
         
      }
      val response = JsObject(Seq(
         "session" -> JsString(ssn.coreSession.toJson)
      )).toString()
  
      Ok(response)
   }

}
