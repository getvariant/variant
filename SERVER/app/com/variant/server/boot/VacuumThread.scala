package com.variant.server.boot

import scala.collection.mutable
import play.api.Logger
import com.variant.core.ConnectionStatus._
import com.variant.server.api.ConfigKeys._
import com.variant.server.conn.Connection

/**
 * Background vacuum thread.
 * Wakes up every configurable interval and takes a pass over all sessions in the store,
 * deleting the expired ones.
 */
class VacuumThread(server: VariantServer) extends Thread {

   private val logger = Logger(this.getClass)
   
   private val vacuumingFrequencyMillis = server.config.getInt(SESSION_VACUUM_INTERVAL) * 1000
	setName("Vacuum");
   setDaemon(true);

	override def run() {

      logger.debug("Vacuum thread started")		
		var interrupted = false
		
		while (!interrupted) {			
			
			try {
				val now = System.currentTimeMillis();
				var count = 0;
				val connectionsToTry = new mutable.HashSet[Connection]()
				
				// Sessions sweep
				server.ssnStore.deleteIf { entry => 
			      if (entry.isExpired) {
			         count += 1
			         connectionsToTry += entry.session.connection
   			      logger.trace(String.format("Vacuumed expired session ID [%s]", entry.session.getId)) 
			         true
			      }
			      else false
			   }
							
				if (logger.isTraceEnabled) logger.trace(s"Vacuumed $count session(s)");
				else if (logger.isDebugEnabled && count > 0) logger.debug(s"Vacuumed $count session(s)");

				// Connections sweep
				connectionsToTry.foreach { conn => 
				   if (conn.isDisposable ) {
				      server.connStore.disposeOf(conn.id)
   			      logger.info(s"Disposed of ${conn.status} connection ID [${conn.id}]") 
				   }
				}
				
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
