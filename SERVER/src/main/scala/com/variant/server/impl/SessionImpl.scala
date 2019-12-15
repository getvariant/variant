package com.variant.server.impl

import scala.collection.JavaConverters._
import java.util.Optional
import com.variant.share.schema.State
import com.variant.share.schema.impl.StateImpl
import com.variant.share.schema.impl.StateVariantImpl
import com.variant.share.schema.impl.VariationImpl
import com.variant.share.session.CoreSession
import com.variant.share.session.CoreStateRequest
import com.variant.share.session.SessionScopedTargetingStabile
import com.variant.server.api.Session
import com.variant.server.api.StateRequest
import com.variant.server.schema.SchemaGen
import com.variant.server.util.JavaImplicits
import com.variant.share.schema.StateVariant
import com.variant.server.api.TraceEvent
import com.variant.server.boot.VariantServer
import com.typesafe.scalalogging.LazyLogging

/**
 * Server session enriches core session with server side functionality.
 * @author Igor
 */

object SessionImpl {

   /**
    * Server session from core session.
    */
   def apply(coreSession: CoreSession, schemaGen: SchemaGen)(implicit server: VariantServer) = new SessionImpl(coreSession, schemaGen)

   /**
    * Server session deserialized from core session's JSON.
    */
   def apply(json: String, schemaGen: SchemaGen)(implicit server: VariantServer) = new SessionImpl(json, schemaGen)

   /**
    * New server session with nothing in it, but the SID.
    */
   def empty(sid: String, schemaGen: SchemaGen)(implicit server: VariantServer) = new SessionImpl(new CoreSession(sid), schemaGen)
}

/**
 * Construct from an object.
 */
class SessionImpl(val coreSession: CoreSession, val schemaGen: SchemaGen)(implicit server: VariantServer) extends Session with LazyLogging {

   /**
    * Construct via deserialization.
    */
   def this(json: String, schemaGen: SchemaGen)(implicit server: VariantServer) {

      this(CoreSession.fromJson(json, schemaGen), schemaGen)
   }

   /*----------------------------------------------------------------------------------------*/
   /*                                        PUBLIC                                          */
   /*----------------------------------------------------------------------------------------*/

   override def getTimestamp = coreSession.getTimestamp

   override def getConfiguration = server.config

   override def getDisqualifiedVariations = coreSession.getDisqualifiedVariations

   override def getId = coreSession.getId

   override def getStateRequest: Optional[StateRequest] = {
      if (coreSession.getStateRequest.isPresent) Optional.of(StateRequestImpl(this, coreSession.getStateRequest.get))
      else Optional.empty[StateRequest]
   }

   override def getTraversedStates = coreSession.getTraversedStates

   override def getTraversedVariations = coreSession.getTraversedVariations

   override def getAttributes() = coreSession.getAttributes

   override def getSchema = schemaGen

   /*----------------------------------------------------------------------------------------*/
   /*                                     PUBLIC EXT                                         */
   /*----------------------------------------------------------------------------------------*/

   def toJson = coreSession.toJson()

   def addTraversedState(state: StateImpl) {
      coreSession.addTraversedState(state)
   }

   def addDisqualifiedTest(variation: VariationImpl) {
      coreSession.addDisqualifiedTest(variation)
   }

   def setTargetingStabile(stabile: SessionScopedTargetingStabile) {
      coreSession.setTargetingStabile(stabile)
   }

   def getTargetingStabile() = coreSession.getTargetingStabile()

   def setStateRequest(state: StateImpl, variant: Optional[StateVariant]) {

      val coreReq = new CoreStateRequest(coreSession, state);
      coreReq.setResolvedStateVariant(variant)
      coreSession.setStateRequest(coreReq)
   }

   /*
    */
   def targetForState(state: State): StateRequest = {
      schemaGen.runtime.targetForState(this, state)
      getStateRequest.get
   }

   /**
    */
   def triggerEvent(event: TraceEvent) {
      val flushableEvent = new FlushableTraceEventImpl(event, this);
      logger.trace(s"Triggered event ${flushableEvent}")
      server.eventBufferCache.write(flushableEvent);
   }

   /*
	 */
   def targetingStabile = coreSession.getTargetingStabile

}