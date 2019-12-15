package com.variant.server.impl

import com.variant.share.schema.State
import com.variant.share.schema.StateVariant
import com.variant.share.schema.Variation
import com.variant.share.schema.Variation.Experience
import com.variant.share.session.CoreStateRequest
import com.variant.server.api.Session
import com.variant.server.api.StateRequest

/**
 *
 */
object StateRequestImpl {

   def apply(session: Session, coreReq: CoreStateRequest) = new StateRequestImpl(session, coreReq)
}

/**
 *
 */
class StateRequestImpl(private val session: Session, private val coreReq: CoreStateRequest) extends StateRequest {

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
   override def getStatus: StateRequest.Status = StateRequest.Status.values()(coreReq.getStatus.ordinal())

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

   def setStatus(status: StateRequest.Status): Unit = coreReq.setStatus(CoreStateRequest.Status.values()(status.ordinal()))

}