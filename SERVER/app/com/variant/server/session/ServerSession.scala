package com.variant.server.session

import com.variant.core.CoreSession
import com.variant.core.event.VariantEvent
import javax.inject.Inject
import com.variant.core.schema.Schema
import com.variant.server.event.FlushableEventImpl
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerSchema
import com.variant.core.session.CoreSessionImpl
import com.variant.core.schema.State
import com.variant.core.schema.impl.StateImpl
import com.variant.server.runtime.VariantRuntime

/**
 * Server session enriches core session with server side functionality.
 * @author Igor
 */

object ServerSession {
  
   def apply(sid: String) = new ServerSession(sid)
  
   def fromJson(json: String) = {
      val coreSession = CoreSessionImpl.fromJson(json, VariantServer.server.schema.get)
      new ServerSession(coreSession)
   }
}

/**
 * Construct from session ID.
 */
class ServerSession (private val sid: String) extends CoreSession {
   
   /**
    * Construct from already constructed core session object.
    */
   def this(coreSession: CoreSession) = {
      this("")
      this.coreSessionImpl = coreSession.asInstanceOf[CoreSessionImpl]
   }

   /**
    * Tests will need access to the core session.
    */
   var coreSessionImpl = new CoreSessionImpl(sid, VariantServer.server.schema.get)
 
  /*
   * Delegates to core methods
   */

   override def creationTimestamp = coreSessionImpl.creationTimestamp
   
   override def getDisqualifiedTests = coreSessionImpl.getDisqualifiedTests
   
   override def getId = coreSessionImpl.getId
   
   override def getSchema  = coreSessionImpl.getSchema
   
   override def getStateRequest = coreSessionImpl.getStateRequest
   
   override def getTraversedStates = coreSessionImpl.getTraversedStates
   
   override def getTraversedTests = coreSessionImpl.getTraversedTests
   
   /*
    * Server only functionality
    */
   
   def targetForState(state: State) = {
      VariantServer.server.runtime.targetSessionForState(coreSessionImpl, state.asInstanceOf[StateImpl])   
   }
   
	def triggerEvent(event: VariantEvent) {
		if (event == null) throw new IllegalArgumentException("Event cannot be null");		
		VariantServer.server.eventWriter.write(new FlushableEventImpl(event, coreSessionImpl));
	}
	
	/*
	 * Expose targeting stabile for tests.
	 */
	def targetingStabile = coreSessionImpl.getTargetingStabile
}