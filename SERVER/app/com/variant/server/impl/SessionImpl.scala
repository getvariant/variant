package com.variant.server.impl

import com.variant.core.TraceEvent
import com.variant.core.schema.State
import com.variant.core.schema.impl.StateImpl
import com.variant.core.schema.impl.TestImpl
import com.variant.core.session.CoreSession
import com.variant.core.session.CoreStateRequest
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.api.Session
import com.variant.server.api.StateRequest
import com.variant.server.event.FlushableTraceEventImpl
import com.variant.server.schema.SchemaGen
import com.variant.server.api.ServerException
import com.variant.core.impl.ServerError
import com.variant.core.schema.impl.StateVariantImpl
import com.variant.server.event.ServerTraceEvent
import play.api.Logger

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

   /**
    * New server session with nothing in it, but the SID - good for tests.
    *
   def empty(sid: String, schema: ServerSchema) = {
      new SessionImpl(new CoreSession(sid), new Connection(schema))
   }
*/
}

/**
 * Construct from session ID.
 */
class SessionImpl(val coreSession: CoreSession, val schemaGen: SchemaGen) extends Session {
   
   def this(json: String, schemaGen: SchemaGen) {
      this(CoreSession.fromJson(json, schemaGen), schemaGen)
   }
   
    private val logger = Logger(this.getClass)
    
   /*----------------------------------------------------------------------------------------*/
   /*                                        PUBLIC                                          */
   /*----------------------------------------------------------------------------------------*/

   override def getCreateDate = coreSession.createDate
   
   override def getDisqualifiedTests = coreSession.getDisqualifiedTests
   
   override def getId = coreSession.getId
         
   override def getStateRequest = {
      if (coreSession.getStateRequest == null) null 
      else StateRequestImpl(this, coreSession.getStateRequest)
   }
   
   override def getTraversedStates = coreSession.getTraversedStates
   
   override def getTraversedTests = coreSession.getTraversedTests
      
   override def clearAttribute(name: java.lang.String) = coreSession.clearAttribute(name)

   override def getAttribute(name: java.lang.String) = coreSession.getAttribute(name)

   override def setAttribute(name: java.lang.String, value: java.lang.String) = coreSession.setAttribute(name, value)
   
   override def getSchema = schemaGen
   
   /*----------------------------------------------------------------------------------------*/
   /*                                     PUBLIC EXT                                         */
   /*----------------------------------------------------------------------------------------*/
   
   def toJson = coreSession.toJson()

   def addTraversedState(state: StateImpl): Unit = coreSession.addTraversedState(state)
  
   def addDisqualifiedTest(test: TestImpl): Unit = coreSession.addDisqualifiedTest(test)

   def getTargetingStabile(): SessionScopedTargetingStabile = coreSession.getTargetingStabile()

   def setStateRequest(state: StateImpl, variant: StateVariantImpl) {
   
      if (coreSession.getStateRequest != null && !coreSession.getStateRequest.isCommitted) {
         throw new ServerException.Remote(ServerError.ACTIVE_REQUEST)                  
      }

      val coreReq = new CoreStateRequest(coreSession, state);
      coreReq.setResolvedStateVariant(variant)
      coreSession.setStateRequest(coreReq)
   }
   
   /*
    */
   def targetForState(state: State): StateRequest = {
      schemaGen.runtime.targetForState(this, state) 
      getStateRequest
   }

   /**
    * 
    */
	def triggerEvent(event: ServerTraceEvent) {
	   val flushableEvent = new FlushableTraceEventImpl(event, this);
		schemaGen.eventWriter.write(flushableEvent);
		logger.trace(s"Flushaed event ${flushableEvent}")
	}
	
	/*
	 */
	def targetingStabile = coreSession.getTargetingStabile
}