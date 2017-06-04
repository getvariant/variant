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
    * Server session deserialized from core session's JSON.
    */
   def apply(json: String) = new SessionImpl(json)
   
   /**
    * New server session with nothing in it, but the SID - good for tests.
    */
   def empty(sid: String) = new SessionImpl(new CoreSession(sid, VariantServer.server.schema.get).toJson())
}

/**
 * Construct from session ID.
 */
class SessionImpl(val json: String) extends Session {
   
   val coreSession = CoreSession.fromJson(json, VariantServer.server.schema.get)
   val paramMap = mutable.HashMap[java.lang.String, java.lang.String]()
   
   /*----------------------------------------------------------------------------------------*/
   /*                                        PUBLIC                                          */
   /*----------------------------------------------------------------------------------------*/

   override def getCreateDate = coreSession.createDate
   
   override def getDisqualifiedTests = coreSession.getDisqualifiedTests
   
   override def getId = coreSession.getId
      
   override def getStateRequest = StateRequestImpl(coreSession.getStateRequest)
   
   override def getTraversedStates = coreSession.getTraversedStates
   
   override def getTraversedTests = coreSession.getTraversedTests
      
   override def clearAttribute(name: java.lang.String): java.lang.String = paramMap.remove(name).getOrElse(null)

   override def getAttribute(name: java.lang.String): java.lang.String = paramMap.get(name).getOrElse(null)

   override def setAttribute(name: java.lang.String, value: java.lang.String): java.lang.String = paramMap.put(name, value).getOrElse(null)
   
   /*----------------------------------------------------------------------------------------*/
   /*                                     PUBLIC EXT                                         */
   /*----------------------------------------------------------------------------------------*/
   
   def getSchema  = coreSession.getSchema

   
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