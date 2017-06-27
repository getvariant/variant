package com.variant.server.impl

import scala.collection.mutable
import com.variant.core.session.CoreSession
import com.variant.core.VariantEvent
import javax.inject.Inject
import com.variant.core.schema.Schema
import com.variant.server.event.FlushableEventImpl
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerSchema
import com.variant.core.schema.State
import com.variant.core.schema.impl.StateImpl
import com.variant.server.conn.Connection
import com.variant.server.api.Session
import com.variant.core.schema.Test
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.api.StateRequest
import com.variant.core.session.CoreStateRequest
import com.variant.core.schema.impl.TestImpl

/**
 * Server session enriches core session with server side functionality.
 * @author Igor
 */

object SessionImpl {
    
   /**
    * Server session from core session.
    */
   def apply(coreSession: CoreSession, conn: Connection) = new SessionImpl(coreSession, conn)

   /**
    * Server session deserialized from core session's JSON.
    */
   def apply(json: String, connection: Connection) = new SessionImpl(json, connection)

   /**
    * New server session with nothing in it, but the SID - good for tests.
    */
   def empty(sid: String) = {
      val conn = new Connection(VariantServer.server.schema.get)
      new SessionImpl(new CoreSession(sid, VariantServer.server.schema.get), conn)
   }
}

/**
 * Construct from session ID.
 */
class SessionImpl(var coreSession: CoreSession, val connection: Connection) extends Session {
   
   def this(json: String, connection: Connection) {
      this(CoreSession.fromJson(json, VariantServer.server.schema.get), connection)
   }

   /*----------------------------------------------------------------------------------------*/
   /*                                        PUBLIC                                          */
   /*----------------------------------------------------------------------------------------*/

   override def getCreateDate = coreSession.createDate
   
   override def getDisqualifiedTests = coreSession.getDisqualifiedTests
   
   override def getId = coreSession.getId
   
   override def getSchema  = coreSession.getSchema
      
   override def getStateRequest = StateRequestImpl(this, coreSession.getStateRequest)
   
   override def getTraversedStates = coreSession.getTraversedStates
   
   override def getTraversedTests = coreSession.getTraversedTests
      
   override def clearAttribute(name: java.lang.String) = coreSession.clearAttribute(name)

   override def getAttribute(name: java.lang.String) = coreSession.getAttribute(name)

   override def setAttribute(name: java.lang.String, value: java.lang.String) = coreSession.setAttribute(name, value)
   
   /*----------------------------------------------------------------------------------------*/
   /*                                     PUBLIC EXT                                         */
   /*----------------------------------------------------------------------------------------*/
   
   def toJson = coreSession.toJson()

   def addTraversedState(state: StateImpl): Unit = coreSession.addTraversedState(state)
  
   def addDisqualifiedTest(test: TestImpl): Unit = coreSession.addDisqualifiedTest(test)

   def getTargetingStabile(): SessionScopedTargetingStabile = coreSession.getTargetingStabile()
     
   def newStateRequest(state: StateImpl): StateRequestImpl = {
      coreSession.setStateRequest(new CoreStateRequest(coreSession, state))
      getStateRequest
   }
   
   /*
    */
   def targetForState(state: State): StateRequest = {
      VariantServer.server.runtime.targetForState(this, state) 
      this.getStateRequest()
   }

   /**
    * 
    */
	def triggerEvent(event: VariantEvent) {
		VariantServer.server.eventWriter.write(new FlushableEventImpl(event, coreSession));
	}
	
	/*
	 */
	def targetingStabile = coreSession.getTargetingStabile
}