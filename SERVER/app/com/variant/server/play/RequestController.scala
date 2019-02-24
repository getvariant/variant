package com.variant.server.play

import scala.collection.JavaConverters.mapAsJavaMapConverter

import com.variant.core.StateRequestStatus
import com.variant.core.StateRequestStatus.Committed
import com.variant.core.StateRequestStatus.Failed
import com.variant.core.StateRequestStatus.InProgress
import com.variant.core.impl.ServerError
import com.variant.core.impl.ServerError.ACTIVE_REQUEST
import com.variant.core.impl.ServerError.EmptyBody
import com.variant.core.impl.ServerError.InvalidRequestStatus
import com.variant.core.impl.ServerError.MissingProperty
import com.variant.core.impl.StateVisitedEvent
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.boot.ServerExceptionInternal
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.boot.VariantServer
import com.variant.server.event.ServerTraceEvent
import com.variant.server.impl.SessionImpl
import com.variant.server.impl.StateRequestImpl
import com.variant.server.util.JavaImplicits.оptional2Option

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.ControllerComponents

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
         throw new ServerExceptionRemote(EmptyBody)
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")         
      }
      val stateName = (bodyJson \ "state").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "state")         
      }

      val stabile = (bodyJson \ "stab").asOpt[List[String]]

      val ssn = server.ssnStore.getOrBust(sid)
      
      // It's an error to request another state, before committing/failing current request.
      //val req = Option(ssn.getStateRequest.orElse(null))
      ssn.getStateRequest.foreach { req =>
         if (req.getStatus == InProgress)
            throw new ServerExceptionRemote(ACTIVE_REQUEST)                
      }
      
      val state = ssn.schemaGen.getState(stateName)
      if (!state.isDefined)
         throw new ServerExceptionInternal("State [%s] not in schema [%s]".format(stateName, ssn.schemaGen.getMeta.getName))
      
      // If stabile was sent, process it, discarding elements not in the schema,
      // and add to the session's stabile. This should have been the first state request in the life
      // of a session and here we were sent the content of the foreground targeting tracker.
      if (stabile.isDefined) {
         val ssts = new SessionScopedTargetingStabile()
         stabile.get.foreach { e =>
            val tokens = e.split("\\.");
            ssn.schemaGen.getVariation(tokens(0)).foreach { v => 
               v.getExperience(tokens(1)).foreach { ssts.add(_) }
            }
         }
         
         if (ssts.size > 0) ssn.setTargetingStabile(ssts)
      }
      
      ssn.schemaGen.runtime.targetForState(ssn, state.get)

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
         throw new ServerExceptionRemote(EmptyBody)   
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")         
      }

      val status = {
         val ordinal = (bodyJson \ "status").asOpt[Int].getOrElse {
            throw new ServerExceptionRemote(MissingProperty, "status")
         }
         StateRequestStatus.values()(ordinal)
      }
      
      if (!status.isIn(Committed, Failed)) 
         throw new ServerExceptionRemote(InvalidRequestStatus, status.toString())
      
      val attrs = (bodyJson \ "attrs").asOpt[Map[String,String]].getOrElse {
         Map[String,String]()
      }
      

      val ssn = server.ssnStore.getOrBust(sid)
      // Ok to assume we always have the request?
      val stateReq = ssn.getStateRequest.get.asInstanceOf[StateRequestImpl]
      
      if (stateReq.getStatus == Committed && status == Failed)
			throw new ServerExceptionRemote(ServerError.CANNOT_FAIL);
      
		else if (stateReq.getStatus == Failed && status == Committed)
			throw new ServerExceptionRemote(ServerError.CANNOT_COMMIT);
		
		else if (stateReq.getStatus == InProgress) {

         stateReq.asInstanceOf[StateRequestImpl].setStatus(status); 

         // Trigger state visited event, but only if we have live experiences
         // at this state. As opposed to custom events, state visited events
         // cannot be orphan because we didn't really visit that state.
         if (!stateReq.getLiveExperiences().isEmpty()) {
            val sve = new StateVisitedEvent(stateReq.getState, status, attrs.asJava)                  
      	   ssn.triggerEvent(new ServerTraceEvent(sve));
         }
         
      }
      val response = JsObject(Seq(
         "session" -> JsString(ssn.coreSession.toJson)
      )).toString()
  
      Ok(response)
   }

}
