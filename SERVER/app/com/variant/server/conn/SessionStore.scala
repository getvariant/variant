package com.variant.server.conn

import com.variant.server.ConfigKeys._
import play.api.Logger
import scala.collection.concurrent.TrieMap
import com.variant.server.session.ServerSession

/**
 * @author Igor
 */
object SessionStore {
   
   class Entry (val json: String) {
      
      lazy val session: ServerSession = ServerSession(json)
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
	def put(sid: String, json: String) {
	   val result = new Entry(json)
	   cacheMap.put(result.session.getId, result)
	}
	
	/**
	 */
	def asSession(sid: String): Option[ServerSession] = {		
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
		   e.json
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

