package com.variant.server.conn

import com.variant.server.api.ConfigKeys._
import play.api.Logger
import scala.collection.concurrent.TrieMap
import com.variant.server.api.Session
import com.variant.server.impl.SessionImpl
import javax.inject.Singleton
import com.variant.server.boot.VariantServer
import com.variant.core.session.CoreSession
import com.variant.server.api.ServerException
import com.variant.core.ServerError
import javax.inject.Inject

/**
 * Instantiated by Play as an eager singleton in Module
 */
trait SessionStore {

   // Session timeout interval.
	val sessionTimeoutMillis = VariantServer.instance.config.getInt(SESSION_TIMEOUT) * 1000

   /**
    * Add or replace a session. 
    * If the session exists, the supplied connection ID must be
    * open and parallel to the original connection. 
    * Otherwise, the new session will be created in the supplied connection, 
    * so long as it's open. 
	 */
	def put(session: SessionImpl);
	
	/**
	 * Get session by session ID, if exists and current connection ID
	 * matches or is parallel to the original connection ID.
	 */
	def get(sid: String, cid: String): Option[SessionImpl]

	/**
	 * Get session by session ID, if exists and current connection ID
	 * matches or is parallel to the original connection ID,
	 * or throw session expired user error.
	 */
	def getOrBust(sid: String, cid: String): SessionImpl

	/**
   * Delete every sessions if predicate p applied to entry, is true.
	*/
   def deleteIf(p: (Entry) => Boolean): Unit
   
   /**
    * Store entry
    */
   class Entry (val session: SessionImpl) {
      
	   // Timestamp the session was last touched. Init with creation.
   	private var lastTouchTs = System.currentTimeMillis()
   	// Connection ID, which touched this session last. Init with creation.
   	// private var lastTouchConnId = session.connection.id
      
      /**
       * An entry is not dead if its session's connection is gone,
       * only if it has been idle longer than configured max idle interval.
       */
      def isExpired = {
         // session.asInstanceOf[SessionImpl].connection.isClosed ||
         sessionTimeoutMillis > 0 && (System.currentTimeMillis() - lastTouchTs) > sessionTimeoutMillis
      }
      
      /**
       */
      def touch() { 
         lastTouchTs = System.currentTimeMillis
      }
   }

}   

/**
 * 
 */
@Singleton
class SessionStoreImpl @Inject() (private val server: VariantServer) extends SessionStore {
   
   private val logger = Logger(this.getClass)	
	private val sessionMap = new TrieMap[String, Entry]();
   private val vacuumThread = new VacuumThread(server, this).start()

   /**
	 */
	override def put(session: SessionImpl) {

      sessionMap.get(session.getId) match { 
		
      // No current entry for this session id
		case None => sessionMap.put(session.getId, new Entry(session))
		
		// Have entry for this session id: only replace if not expired and open
		// by this or parallel connection.
		case Some(e) =>
		   if (e.isExpired || ! e.session.connection.isParallelTo(session.connection)) {
		      throw new ServerException.Remote(ServerError.SessionExpired, session.getId)
		   }
		   else   {
		      e.touch()
		      sessionMap.put(session.getId, new Entry(session))
		   }
		}
	}
	
	/**
	 */
	override def get(sid: String, cid: String): Option[SessionImpl] = {
	
		sessionMap.get(sid) match { 
		   
		case None => None
		case Some(e) =>
		   if (!e.isExpired && e.session.connection.id == cid) {
		      e.touch()
		      Some(e.session)
		   }
		   else {
		      None
		   }
		}
	}

	/**
	 */
	override def getOrBust(sid: String, cid: String): SessionImpl = {
      val result	= get(sid, cid).getOrElse {
         logger.debug(s"Not found session [${sid}]")      
         throw new ServerException.Remote(ServerError.SessionExpired, sid)
      }
      logger.debug(s"Found session [${sid}]")            
      result
	}
	
  /**
	*/
   def deleteIf(f: (Entry) => Boolean) {
      sessionMap.retain((id, entry) => !f(entry))
   }

}

/**
 * Background vacuum thread.
 * Wakes up every configurable interval and takes a pass over all sessions in the store,
 * deleting the expired ones.
 */
class VacuumThread(server: VariantServer, store: SessionStore) extends Thread {

   private val logger = Logger(this.getClass)
   private val vacuumingFrequencyMillis = server.config.getInt(SESSION_VACUUM_INTERVAL) * 1000
	setName("SsnVacThread");
   setDaemon(true); // Daemonize.


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
   			      logger.trace(String.format("Vacuumed expired session ID [%s]", entry.session.getId)) 
			         true
			      }
			      else false
			   }
							
				if (logger.isTraceEnabled) logger.trace(s"Vacuumed $count session(s)");
				else if (logger.isDebugEnabled && count > 0) logger.debug(s"Vacuumed $count session(s)");

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

