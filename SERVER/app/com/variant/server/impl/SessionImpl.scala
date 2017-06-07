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
import com.variant.server.runtime.Runtime
import com.variant.server.conn.Connection
import com.variant.server.api.Session

/**
 * Server session enriches core session with server side functionality.
 * @author Igor
 */

object SessionImpl {
    
   /**
    * Server session from core session.
    */
   def apply(coreSession: CoreSession) = new SessionImpl(coreSession)

   /**
    * Server session deserialized from core session's JSON.
    */
   def apply(json: String) = new SessionImpl(json)

   /**
    * New server session with nothing in it, but the SID - good for tests.
    */
   def empty(sid: String) = new SessionImpl(new CoreSession(sid, VariantServer.server.schema.get))
}

/**
 * Construct from session ID.
 */
//class SessionImpl(val json: String) extends Session {
class SessionImpl(val coreSession: CoreSession) extends Session {
   
   def this(json: String) {
      this(CoreSession.fromJson(json, VariantServer.server.schema.get))
   }
   
   //val coreSession = CoreSession.fromJson(json, VariantServer.server.schema.get)
   val paramMap = mutable.HashMap[java.lang.String, java.lang.String]()
   
   /*----------------------------------------------------------------------------------------*/
   /*                                        PUBLIC                                          */
   /*----------------------------------------------------------------------------------------*/

   override def getCreateDate = coreSession.createDate
   
   override def getDisqualifiedTests = coreSession.getDisqualifiedTests
   
   override def getId = coreSession.getId
   
   override def getSchema  = coreSession.getSchema
      
   override def getStateRequest = StateRequestImpl(coreSession.getStateRequest)
   
   override def getTraversedStates = coreSession.getTraversedStates
   
   override def getTraversedTests = coreSession.getTraversedTests
      
   override def clearAttribute(name: java.lang.String): java.lang.String = paramMap.remove(name).getOrElse(null)

   override def getAttribute(name: java.lang.String): java.lang.String = paramMap.get(name).getOrElse(null)

   override def setAttribute(name: java.lang.String, value: java.lang.String): java.lang.String = paramMap.put(name, value).getOrElse(null)
   
   /*----------------------------------------------------------------------------------------*/
   /*                                     PUBLIC EXT                                         */
   /*----------------------------------------------------------------------------------------*/
   
   def toJson = coreSession.toJson()

   def addTraversedState(state: State): Unit = coreSession.addTraversedState(state)
  
   /*
    */
   def targetForState(state: State) = {
      VariantServer.server.runtime.targetSessionForState(this, state.asInstanceOf[StateImpl])   
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