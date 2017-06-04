package com.variant.server.conn

import com.variant.server.api.ConfigKeys._
import play.api.Logger
import scala.collection.concurrent.TrieMap
import com.variant.server.api.Session
import com.variant.server.impl.SessionImpl

/**
 * @author Igor
 */
object SessionStore {
   
   class Entry (val session: Session) {
      
   	private var lastTouchTs = System.currentTimeMillis();
      
      /**
       */
      def millisSinceLastTouch = System.currentTimeMillis() - lastTouchTs
      
      /**
       */
      def touch() {lastTouchTs = System.currentTimeMillis}
   }
   
}

class SessionStore() {


import SessionStore._
   
   private val logger = Logger(this.getClass)	
	private val cacheMap = new TrieMap[String, Entry]();


   /**
	 */
	def put(ssn: Session) {
	   cacheMap.put(ssn.getId, new Entry(ssn))
	}
	
	/**
	 */
	def asSession(sid: String): Option[Session] = {
		cacheMap.get(sid).map { e =>
		   e.touch()
		   e.session
		}
	}

	/**
	 */
	def asJson(sid: String): Option[String] = {		
		cacheMap.get(sid).map { e =>
		   e.touch()
		   e.session.asInstanceOf[SessionImpl].toJson
		}
	}

	/**
	 */
   def destroy() {
      cacheMap.clear()
   }

  /**
	*/
   def deleteIf(f: (Entry) => Boolean) {
      cacheMap.retain((id, entry) => !f(entry))
   }

}

