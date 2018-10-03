package com.variant.server.impl

import com.variant.core.schema.State
import com.variant.server.api.StateRequest
import com.variant.core.session.CoreStateRequest
import java.util.Date
import com.variant.core.schema.Variation
import com.variant.core.schema.Variation.Experience
import com.variant.core.schema.StateVariant
import com.variant.core.TraceEvent
import com.variant.core.StateRequestStatus
import com.variant.core.schema.impl.StateVariantImpl
import com.variant.server.api.Session
import com.variant.server.event.ServerTraceEvent
import com.variant.core.impl.StateVisitedEvent

/**
 * 
 */
object StateRequestImpl {
   
   def apply(session: Session, coreReq:CoreStateRequest) = new StateRequestImpl(session, coreReq)
}

/**
 * 
 */
class StateRequestImpl(private val session: Session, private val coreReq:CoreStateRequest) extends StateRequest {
  
   /*----------------------------------------------------------------------------------------*/
   /*                                        PUBLIC                                          */
   /*----------------------------------------------------------------------------------------*/

   /**
	 */
	override def getSession: Session = session

	/**
	 */
	override def getState: State = coreReq.getState

	/**
	 */
	override def getStatus: StateRequestStatus = coreReq.getStatus

	/**
	 */
	override def getLiveExperiences: java.util.Set[Experience] = coreReq.getLiveExperiences

	/**
	 */
	override def getLiveExperience(variation: Variation): java.util.Optional[Experience] = coreReq.getLiveExperience(variation)

	/**
    */
	override def getResolvedStateVariant: java.util.Optional[StateVariant] = coreReq.getResolvedStateVariant
	
	/**
	 */	
	override def getResolvedParameters: java.util.Map[java.lang.String, java.lang.String] = coreReq.getResolvedParameters
   
   /*----------------------------------------------------------------------------------------*/
   /*                                     PUBLIC EXT                                         */
   /*----------------------------------------------------------------------------------------*/
	
   def setResolvedStateVariant(variant: java.util.Optional[StateVariant]): Unit = coreReq.setResolvedStateVariant(variant)
         
   def setStatus(status: StateRequestStatus): Unit = coreReq.setStatus(status)

}