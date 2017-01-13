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
import com.variant.server.conn.Connection

/**
 * Server session enriches core session with server side functionality.
 * @author Igor
 */

object ServerSession {
    
   /**
    * Server session deserialized from core session's JSON.
    */
   def apply(json: String) = new ServerSession(json)
   
   /**
    * New server session with nothing in it, but the SID - good for tests.
    */
   def empty(sid: String) = new ServerSession(new CoreSession(sid, VariantServer.server.schema.get).toJson())
}

/**
 * Construct from session ID.
 */
class ServerSession (val json: String) {
   
   val coreSession = CoreSession.fromJson(json, VariantServer.server.schema.get)
   
  /*
   * Delegates to core methods
   */
   def createDate = coreSession.createDate
   
   def getDisqualifiedTests = coreSession.getDisqualifiedTests
   
   def getId = coreSession.getId
   
   def getSchema  = coreSession.getSchema
   
   def getStateRequest = coreSession.getStateRequest
   
   def getTraversedStates = coreSession.getTraversedStates
   
   def getTraversedTests = coreSession.getTraversedTests
   
   def toJson = coreSession.toJson()
   
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