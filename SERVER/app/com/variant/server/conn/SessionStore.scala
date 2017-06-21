package com.variant.server.conn

import com.variant.server.api.ConfigKeys._
import play.api.Logger
import scala.collection.concurrent.TrieMap
import com.variant.server.api.Session
import com.variant.server.impl.SessionImpl
import javax.inject.Singleton
import com.variant.server.boot.VariantServer

/**
 * 
 */
trait SessionStore {

   /**
    * Add or replace a session.
	 */
	def put(ssn: SessionImpl);
	
	/**
	 * Get session by session ID
	 */
	def get(sid: String): Option[SessionImpl]

  /**
   * Delete every sessions if predicate p applied to entry, is true.
	*/
   def deleteIf(p: (Entry) => Boolean): Unit
   
   /**
    * Store entry
    */
   class Entry (val session: SessionImpl) {
      
   	private var lastTouchTs = System.currentTimeMillis();
      private val sessionTimeoutMillis = VariantServer.server.config.getInt(SESSION_TIMEOUT) * 1000
      
      /**
       * An entry is dead if its session's connection is gone
       * or it has been idle longer than configured max idle interval.
       */
      def isExpired = {
         session.asInstanceOf[SessionImpl].connection.isClosed ||
         sessionTimeoutMillis > 0 && (System.currentTimeMillis() - lastTouchTs) > sessionTimeoutMillis
      }
      
      /**
       */
      def touch() { lastTouchTs = System.currentTimeMillis }
   }

}   
   
/**
 * @author Igor
 */
object SessionStoreImpl {
      
}

/**
 * 
 */
@Singleton
class SessionStoreImpl() extends SessionStore {


import SessionStoreImpl._
   
   private val logger = Logger(this.getClass)	
	private val sessionMap = new TrieMap[String, Entry]();
   private val vacuumThread = new VacuumThread(this).start()

   /**
	 */
	override def put(ssn: SessionImpl) {
	   sessionMap.put(ssn.getId, new Entry(ssn))
	}
	
	/**
	 */
	override def get(sid: String): Option[SessionImpl] = {
	
		sessionMap.get(sid).flatMap { e =>
		   
		   if (e.isExpired) None
		   else {
		      e.touch()
		      Some(e.session)
		   }
		}
	}

  /**
	*/
   def deleteIf(f: (Entry) => Boolean) {
      sessionMap.retain((id, entry) => !f(entry))
   }

}

/**
 * Background vacuum thread disposes of expired session entries.
 */
class VacuumThread(store: SessionStore) extends Thread {

   private val logger = Logger(this.getClass)
   private val vacuumingFrequencyMillis = VariantServer.server.config.getInt(SESSION_STORE_VACUUM_INTERVAL) * 1000
	setName("SsnVacThread");
   setDaemon(true); // JVM will kill it when on non-daemon threads exit.


	override def run() {

      logger.debug(s"Vacuum thread $getName started")		
		var interrupted = false
		
		while (!interrupted) {			
			
			try {
				val now = System.currentTimeMillis();
				var count = 0;
				
				store.deleteIf { entry => 
			      if (entry.isExpired) {
			         count += 1
   			      logger.trace(String.format("Vacuumed expired session ID [%s]", entry.session.getId));
			         true
			      }
			      else false
			   }
							
				if (logger.isDebugEnabled && count > 0) logger.debug(s"Vacuumed $count session(s)");
				if (logger.isTraceEnabled) logger.trace(s"Vacuumed $count session(s)");

				Thread.sleep(vacuumingFrequencyMillis)
				logger.trace("Vacuum thread woke up after %s millis".format(System.currentTimeMillis() - now))
			}
			catch {
			   case _: InterruptedException => interrupted = true;
			   case t: Throwable =>	logger.error("Unexpected exception in vacuuming thread.", t);
			}
			
			if (interrupted || isInterrupted()) {
				logger.debug("Vacuum thread " + Thread.currentThread().getName() + " interrupted and exited.");
				interrupted = true;
			}
		}
	}
}

