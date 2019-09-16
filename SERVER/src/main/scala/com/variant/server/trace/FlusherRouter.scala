package com.variant.server.akka

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import java.time.Instant
import akka.actor.Props
import akka.actor.actorRef2Scala
import com.variant.server.boot.VariantServer
import com.variant.server.api.FlushableTraceEvent
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import com.variant.server.schema.ServerFlusherService
import com.variant.server.api.TraceEventFlusher
import akka.actor.ActorRef
import scala.concurrent.Future
import com.variant.server.trace.EventBufferCache
import com.variant.server.boot.ServerExceptionInternal
import com.variant.core.util.TimeUtils
import java.time.Duration
import scala.concurrent.ExecutionContext
import java.util.concurrent.ExecutorService
import akka.dispatch.ExecutorServiceFactory
import java.util.concurrent.Executors
import scala.util.{Success, Failure}
import com.variant.server.boot.ServerMessageLocal

/**
 * Trace event flusher Actor.
 */
object FlusherRouter {

   private[this] var _ref: ActorRef = _
   
      // Start the sole vacuum actor.
   def start(server: VariantServer) {
      _ref = server.actorSystem.actorOf(Props(new FlusherRouter(server)), name = "FlusherRouter")
   }

   def ref = _ref
   
   /**
    * Protocol
    */
   final case class Flush(header: EventBufferCache.Header)

}

private class FlusherRouter(server: VariantServer) extends Actor with LazyLogging {

   import FlusherRouter._

   val poolSize = Math.ceil(Runtime.getRuntime.availableProcessors * server.config.eventFlushParallelism).toInt
         
   implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(poolSize))

   /**
    * Process the flush msg.
    */
   override def receive() = {
            
      case Flush(header: EventBufferCache.Header) =>
         
         val flusher = header.flusherService.getFlusher  // Just one for now.
         
         Future {
            
            // We can trust bufferIx, so long as it's <= max siize.
            val actualLength = header.bufferIx.get min header.buffer.length 
            
            // Consistency check. 
            for (i <- 0 to actualLength) {
               if (header.buffer(i) == null) {
                  throw ServerExceptionInternal("Inconsitent buffer")
               }
            }
            
            doFlush(header, actualLength, flusher)
            
         } onComplete {
             
            case Success(_) =>  // AOK nothing todo.
               
            case Failure(t: Throwable) =>
               logger.error(ServerMessageLocal.FLUSHER_CLIENT_ERROR.asMessage(flusher.toString))
         }
         
   }
   
   /**
    * Flush the buffer pointed to by a given header.
    */
   private[this] def doFlush(header: EventBufferCache.Header, size: Int, flusher: TraceEventFlusher) {

      val start = Instant.now
      
      
      logger.debug(s"About to flush ${size} trace events")
      try {
               
         flusher.flush(header.buffer, size)
         
         logger.info(s"Flushed ${size} event(s) in " + TimeUtils.formatDuration(Duration.between(start, Instant.now())))
      } catch {
         case t: Throwable => {
            logger.error("Unhandled exception (ignored)", t)
         }
      }
      finally {
         header.status = EventBufferCache.HeaderStatus.FREE
         // We null out the array as extra precaution, in case next time
         // this buffer is flushed the client code in TraceEventFlusher.flush()
         // fails to honor the size parameter and attempts to flush all events.
         for (i <- 0 to header.buffer.length) header.buffer(i) = null
      }
   }

}
