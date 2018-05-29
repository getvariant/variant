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
import com.variant.core.impl.ServerError
import com.variant.core.ConnectionStatus._
import javax.inject.Inject

/**
 * Instantiated by Play as an eager singleton in Module
 *
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
   

}   
*/
/**
 * 
 */
class SessionStore (private val server: VariantServer) {
   
   // Session timeout interval.
	val sessionTimeoutMillis = server.config.getInt(SESSION_TIMEOUT) * 1000

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

   private val logger = Logger(this.getClass)	
	private val sessionMap = new TrieMap[String, Entry]();

   /**
	 */
	def put(session: SessionImpl) {

      sessionMap.get(session.getId) match { 
		
      // No current entry for this session id
		case None => 
		   session.connection.status match {
		      case OPEN => {
		         sessionMap.put(session.getId, new Entry(session))
		         session.connection.schema.sessionCount.incrementAndGet
		      }
		      case DRAINING => throw new ServerException.Remote(ServerError.UnknownConnection, session.connection.id)
		      case _ => throw new ServerException.Remote(ServerError.InvalidConnectionStatus, session.connection.id)
		   }		   
		
      // Have entry for this session id:
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
	 * Retrieve a session by its ID as an Option.
	 * Requestor's connection must be parallel to the session's.
	 */
	def get(sid: String, conn: Connection): Option[SessionImpl] = {
	
		sessionMap.get(sid) match { 
		   
		case None => None
		case Some(e) =>
		   if (!e.isExpired && e.session.connection.isParallelTo(conn)) {
		      e.touch()
		      Some(e.session)
		   }
		   else {
		      None
		   }
		}
	}

	/**
	 * Retrieve a session by its ID, or throw SessionExpired if does not exist.
	 */
	def getOrBust(sid: String, conn: Connection): SessionImpl = {
      val result	= get(sid, conn).getOrElse {
         logger.debug(s"Not found session [${sid}]")      
         throw new ServerException.Remote(ServerError.SessionExpired, sid)
      }
      logger.debug(s"Found session [${sid}]")            
      result
	}
		
  /**
   * The single entry point in session clean out.
   * return the number of deleted sessions for reporting.
	*/
   def vacuum(): Int = {
      val toDelete = sessionMap.filter { _._2.isExpired }
      toDelete.values.foreach { entry =>
         sessionMap -= entry.session.getId
         entry.session.connection.schema.sessionCount.decrementAndGet()
         if (logger.isTraceEnabled)
	         logger.trace(s"Vacuumed expired session ID [${entry.session.getId}]")
      }
      return toDelete.size
   }

}

