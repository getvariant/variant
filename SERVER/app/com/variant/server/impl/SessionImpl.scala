package com.variant.server.impl

import scala.collection.JavaConverters._
import java.util.Optional
import com.variant.core.schema.State
import com.variant.core.schema.impl.StateImpl
import com.variant.core.schema.impl.StateVariantImpl
import com.variant.core.schema.impl.VariationImpl
import com.variant.core.session.CoreSession
import com.variant.core.session.CoreStateRequest
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.api.Session
import com.variant.server.api.StateRequest
import com.variant.server.schema.SchemaGen
import com.variant.server.util.JavaImplicits
import play.api.Logger
import com.variant.core.schema.StateVariant
import com.variant.server.api.TraceEvent

/**
 * Server session enriches core session with server side functionality.
 * @author Igor
 */

object SessionImpl {
    
   /**
    * Server session from core session.
    */
   def apply(coreSession: CoreSession, schemaGen: SchemaGen) = new SessionImpl(coreSession, schemaGen)

   /**
    * Server session deserialized from core session's JSON.
    */
   def apply(json: String, schemaGen: SchemaGen) = new SessionImpl(json, schemaGen)

   /**
    * New server session with nothing in it, but the SID.
    */
   def empty(sid: String, schemaGen: SchemaGen) = new SessionImpl(new CoreSession(sid), schemaGen)
}

/**
 * Construct from an object.
 */
class SessionImpl(val coreSession: CoreSession, val schemaGen: SchemaGen) extends Session {
   
	/**
	 * Construct via deserialization.
	 */
   def this(json: String, schemaGen: SchemaGen) {
      this(CoreSession.fromJson(json, schemaGen), schemaGen)
   }
   
    private val logger = Logger(this.getClass)
    
   /*----------------------------------------------------------------------------------------*/
   /*                                        PUBLIC                                          */
   /*----------------------------------------------------------------------------------------*/

   override def getTimestamp = coreSession.getTimestamp
   
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
		schemaGen.eventWriter.write(flushableEvent);
		logger.trace(s"Triggered event ${flushableEvent}")
	}
	
	/*
	 */
	def targetingStabile = coreSession.getTargetingStabile
	
}