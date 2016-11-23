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
 * 
 */
class ServerSession (private val sid: String) extends CoreSession {
   
   def this(coreSession: CoreSession) = {
      this("")
      this.coreSession = coreSession.asInstanceOf[CoreSessionImpl]
   }
   /*
    * Delegates to core methods
    */
   private var coreSession = new CoreSessionImpl(sid, VariantServer.server.schema.get)
 
   override def creationTimestamp = coreSession.creationTimestamp
   
   override def getDisqualifiedTests = coreSession.getDisqualifiedTests
   
   override def getId = coreSession.getId
   
   override def getSchema  = coreSession.getSchema
   
   override def getStateRequest = coreSession.getStateRequest
   
   override def getTraversedStates = coreSession.getTraversedStates
   
   override def getTraversedTests = coreSession.getTraversedTests
   
   /*
    * Server only functionality
    */
   
   def targetForState(state: State) = {
      VariantServer.server.runtime.targetSessionForState(coreSession, state.asInstanceOf[StateImpl])   
   }
   
	def triggerEvent(event: VariantEvent) {
		if (event == null) throw new IllegalArgumentException("Event cannot be null");		
		VariantServer.server.eventWriter.write(new FlushableEventImpl(event, coreSession));
	}
}