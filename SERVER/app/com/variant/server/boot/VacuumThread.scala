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
				
				// Sessions sweep
				val deleteCount = server.ssnStore.deleteExpired()  
			   
				if (logger.isTraceEnabled) logger.trace(s"Vacuumed $deleteCount session(s)");
				else if (logger.isDebugEnabled && deleteCount > 0) logger.debug(s"Vacuumed $deleteCount session(s)");

				// Connections sweep
            server.connStore.deleteDisposable()

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
