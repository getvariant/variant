package com.variant.server.conn;

import java.util.Map
import java.util.Set

import com.variant.server.api.ConfigKeys._

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Logger

import com.variant.server.boot.VariantServer

import scala.collection.concurrent.TrieMap

import com.typesafe.config.Config

/**
 * Connection Store.
 * Connections are not expired, unless explicitely closed by the client.
 * Only a configurable max number of them is accepted.
 * 
 * @author Igor
 *
 */
trait ConnectionStore {

   /**
    * Save a connection in this store.
    * Can be retrieved by its ID.
    * Returns true if stored or false if connection table is full.
    */
	def put(conn: Connection): Boolean
		
	/**
	 * Retrieve a connection from this store by its ID.
	 */
	def get(id: String) : Option[Connection]
	
	/**
	 * Delete a connection from this store by its ID.
	 */
	def remove(id: String): Option[Connection]
	
}

@Singleton
class ConnectionStoreImpl @Inject() (server: VariantServer) extends ConnectionStore {

   private val logger = Logger(this.getClass)
	private lazy val maxSize = server.config.getInt(MAX_CONCURRENT_CONNECTIONS)
   private val connMap = new TrieMap[String, Connection]()
   private val vacuumThread = new VacuumThread(connMap, server.config).start()
   
	/**
	 */
	override def put(conn: Connection): Boolean = {
      if (connMap.size >= maxSize) {
         false
      }
      else {
         connMap.put(conn.id, conn)
         true
      }
   }
	
	/**
	 */
   override def get(id: String): Option[Connection] = {
      connMap.get(id)
	}
   
   /**
    */
   override def remove(id: String): Option[Connection] = {
      val result = connMap.remove(id)
      result.foreach {_.destroy()}
      result
   }

}

/**
 * Background vacuum thread disposes of expired session entries.
 */
class VacuumThread(connMap: TrieMap[String, Connection], config: Config) extends Thread {

   private val logger = Logger(this.getClass)
   private val sessionTimeoutMillis = config.getInt(SESSION_TIMEOUT) * 1000
   private val vacuumingFrequencyMillis = config.getInt(SESSION_STORE_VACUUM_INTERVAL) * 1000
	setName("VariantSessionVacuum");
   setDaemon(true);


	override def run() {

      logger.debug(s"Vacuum thread $getName started")		
		var interrupted = false
		
		while (true) {			
			
			try {
				val now = System.currentTimeMillis();
				var count = 0;
				
				for ((id, conn) <- connMap) {
				   conn.deleteIf { entry => 
				      if (sessionTimeoutMillis > 0 && entry.millisSinceLastTouch > sessionTimeoutMillis) {
				         count += 1
      			      logger.trace(String.format("Vacuumed expired session ID [%s]", entry.session.getId));
				         true
				      }
				      else false
				   }
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
				connMap.clear()
				return;
			}
		}
	}
}
