package com.variant.server.session

import com.variant.core.session.CoreSession
import com.variant.core.VariantEvent
import javax.inject.Inject
import com.variant.core.schema.Schema
import com.variant.server.event.FlushableEventImpl
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerSchema
import com.variant.core.schema.State
import com.variant.core.schema.impl.StateImpl
import com.variant.server.runtime.Runtime

/**
 * Server session enriches core session with server side functionality.
 * @author Igor
 */

object ServerSession {
  
   def apply(sid: String) = new ServerSession(sid)
  
   def fromJson(json: String) = {
      val coreSession = CoreSession.fromJson(json, VariantServer.server.schema.get)
      new ServerSession(coreSession)
   }
}

/**
 * Construct from session ID.
 */
class ServerSession (private val sid: String) {
   
   /**
    * Construct from already constructed core session object.
    */
   def this(coreSession: CoreSession) = {
      this("")
      this.coreSession = coreSession.asInstanceOf[CoreSession]
   }

   /**
    * Tests will need access to the core session.
    */
   var coreSession = new CoreSession(sid, VariantServer.server.schema.get)
 
  /*
   * Delegates to core methods
   */
   def creationTimestamp = coreSession.creationTimestamp
   
   def getDisqualifiedTests = coreSession.getDisqualifiedTests
   
   def getId = coreSession.getId
   
   def getSchema  = coreSession.getSchema
   
   def getStateRequest = coreSession.getStateRequest
   
   def getTraversedStates = coreSession.getTraversedStates
   
   def getTraversedTests = coreSession.getTraversedTests
   
   /*
    * Server only functionality
    */
   def targetForState(state: State) = {
      VariantServer.server.runtime.targetSessionForState(coreSession, state.asInstanceOf[StateImpl])   
   }
   
	def triggerEvent(event: VariantEvent) {
		VariantServer.server.eventWriter.write(new FlushableEventImpl(event, coreSession));
	}
	
	/*
	 * Expose targeting stabile for tests.
	 */
	def targetingStabile = coreSession.getTargetingStabile
}