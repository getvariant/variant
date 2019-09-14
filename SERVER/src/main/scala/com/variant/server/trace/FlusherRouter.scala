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
import com.variant.server.trace.FlusherActor
import scala.concurrent.Future

/**
 * Trace event flusher Actor.
 */
object FlusherRouter {

   def props(service: ServerFlusherService): Props = {
      
      Props(new FlusherActor(server))
   }   
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

   final object FlushAll

}

private class FlusherRouter 
   (flusherService: ServerFlusherService) 
   (impilcit server: VariantServer) extends Actor with LazyLogging {

   import FlusherAdminActor._

   // Event bufferFlush each time there's flushSize worth of events available,
   // but not less frequently than configurable max delay
   val maxDelayMillis = server.config.eventWriterMaxDelay * 1000
   
   //val 
   /**
    * Event buffer cache will send us buffers as soon as they fill up.
    * But in order to accommodate the max delay config parameter,
    * we schedule additional flushes here.
    */
   override def preStart() = {
      
      // Create the flusher actors
      flusherActors(1) = flusherService.getFlusher
      
      implicit val ec = server.actorSystem.dispatcher
      context.system.scheduler.schedule(
            initialDelay = FiniteDuration(maxDelayMillis, MILLISECONDS),
            interval = FiniteDuration(maxDelayMillis, MILLISECONDS)) 
         {
            self ! FlushAll
         }
   }

   /**
    * Process the flush msg.
    */
   override def receive() = {

      case Flush(header: BufferCache.Header) =>
         
         Future {

         } 
         
      cashe FlushAll => 
         flusherService.
   }

   /**
    * We're going down. Flush what's still on the queue.
    */
   override def postStop() {
      flush()      
   }
   
   /**
    * Flush the buffer pointed to by a given header.
    */
   private[this] def doFlush(header: BufferCache.Header, flusher: TraceEventFlusher) {

      val start = Instant.now
            
      logger.debug(s"About to flush ${size} events...")
      try {
         
         // We can trust the buffer index, so long as it's < buff size.
         val len = header.bufferIx.get min bufferSize
      
         // Consistency check. 
         for (i <- 0 to len) {
            if (header.buffer(i) == null)
               throw ServerExceptionInternal("Inconsitent buffer")
            }
         }

         flusher.flush(header.buffer, len)
         logger.info(s"Flushed ${len} event(s) in " + TimeUtils.formatDuration(Duration.between(start, Instant.now())))
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
         for (i <- 0 to head.buffer.length) head.buffer(i) = null
      }
   }

}
