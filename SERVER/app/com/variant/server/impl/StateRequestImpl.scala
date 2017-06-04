package com.variant.server.impl

import com.variant.core.schema.State
import com.variant.server.api.StateRequest
import com.variant.core.session.CoreStateRequest
import java.util.Date
import com.variant.core.StateRequestStatus
import com.variant.core.schema.Test.Experience
import com.variant.core.schema.Test
import com.variant.core.schema.StateVariant
import com.variant.core.VariantEvent

/**
 * 
 */
object StateRequestImpl {
   
   def apply(coreReq:CoreStateRequest) = new StateRequestImpl(coreReq)
}

/**
 * 
 */
class StateRequestImpl(private val coreReq:CoreStateRequest) extends StateRequest {
  
   /*----------------------------------------------------------------------------------------*/
   /*                                        PUBLIC                                          */
   /*----------------------------------------------------------------------------------------*/

   /**
	 */
	override def getState(): State = coreReq.getState

	/**
	 */
	override def createDate(): Date = coreReq.createDate

	/**
	 */
	override def isCommitted(): Boolean = coreReq.isCommitted

	/**
	 */
	override def getStatus(): StateRequestStatus = coreReq.getStatus

	/**
	 */
	override def getLiveExperiences(): java.util.Set[Experience] = coreReq.getLiveExperiences

	/**
	 */
	override def getLiveExperience(test: Test): Experience = coreReq.getLiveExperience(test)

	/**
    */
	override def getResolvedStateVariant(): StateVariant = coreReq.getResolvedStateVariant
	
	/**
	 */	
	override def getResolvedParameters(): java.util.Map[java.lang.String, java.lang.String] = coreReq.getResolvedParameters

   /**
    * 
    */
   override def getStateVisitedEvent(): VariantEvent = coreReq.getStateVisitedEvent 
   
   /*----------------------------------------------------------------------------------------*/
   /*                                     PUBLIC EXT                                         */
   /*----------------------------------------------------------------------------------------*/

   def commit(): Unit = coreReq.commit()

}