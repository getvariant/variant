package com.variant.server.trace

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import com.variant.server.api.Configuration
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure
import com.typesafe.scalalogging.LazyLogging
import com.variant.server.boot.ServerMessageLocal
import java.util.concurrent.Callable

/**
 * User supplied flushers are executed on this pool.
 * Since these flushers are most likely blocking, we don't want them on the reglar actor pool.
 */
class FlusherThreadPool(config: Configuration) extends LazyLogging {
  
   // The underlying fixed thread pool.
   private[this] val pool = {
      val size = Math.ceil(Runtime.getRuntime.availableProcessors * config.eventWriterFlushParallelism).toInt
      Executors.newFixedThreadPool(size)
   }
   
   /**
    * Keep track of the number of outstanding tasks and shutdown only when they're all done.
    */
   private[this] val taskCounter = new java.util.concurrent.atomic.AtomicInteger(0)

   /**
    * Submit a task to this pool.
    */
   def submit(block: =>Unit): Unit = {
      
      taskCounter.incrementAndGet
      
      pool.submit { 
         new Callable[Unit]() {
            
            override def call {   
               try {
                  block
               }
               catch {
                  case t: Throwable =>
                     logger.error(ServerMessageLocal.FLUSHER_CLIENT_ERROR.asMessage(), t)
               }
               finally {
                  taskCounter.decrementAndGet                  
               }
            }
         }
      }      
   }
   
   /**
    * Shutdown the pool after all queued tasks have run.
    */
   def shutdown(timeout: Int = 10000) {
      
      if (!pool.isShutdown()) {
       
         var waited = 0
         
         while (taskCounter.get > 0 && waited < timeout) {
            Thread.sleep(250)
            waited += 250
         }
         logger.debug(s"Wated: $waited, tasks remaining: ${taskCounter.get}") 
         
         if (waited >= timeout) {
            logger.error("Timeout waiting for all flusher tasks to complete after $timeout ms. Some events may have been lost.")
         }
         pool.shutdown() 
      }
   }
}