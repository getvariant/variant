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

/**
 * Trace event flusher Actor.
 */
object FlusherAdminActor {

   def props(service: ServerFlusherService): Props = {
      
      Props(new FlusherActor(server))
   }
   
   val name = "FlusherAdminActor"

   /*
    * SENT BY Event queue's write() method if there's at least flush size worth of events on the queue. 
    * PROCESS This actor flushes as many flush size fulls as available. Ignored if there are fewer than
    *         flush size full of events available.
    */
   final case class Flush(buff: Array[FlushableTraceEvent], size: Int)
      
}

class FlusherAdminActor
   (flusherService: ServerFlusherService) 
   (implicit server: VariantServer) extends Actor with LazyLogging {

   import FlusherAdminActor._

   // Event bufferFlush each time there's flushSize worth of events available,
   // but not less frequently than configurable max delay
   val maxDelayMillis = server.config.eventWriterMaxDelay * 1000

   val flusherActors = new Array[ActorRef](1)
   
   /**
    * Event buffer cache will send us pending buffers as soon as they become
    * available, but in order to accommodate the max delay config parameter,
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
            self ! FlushNow
         }
   }

   /**
    * Process the flush msg. For now, we simply fire-and-forget to the only FlusherActor
    * we've created. Eventually, we'll support parallel flushers so we'll have multiple
    * child actors and we'll have to load-balance between them. 
    */
   override def receive() = {

      case Flush(buff, size) =>
         
         
      
   }

   /**
    * We're going down. Flush what's still on the queue.
    */
   override def postStop() {
      flush()      
   }
   
   /**
    * Flush the entire buffer.
    */
   private[this] def flush() {

      val size = eventQueue.read(flushBuffer)
            
      logger.debug(s"About to flush ${size} events...")
      if (size > 0) {
         var start = Instant.now();
         flusher.flush(flushBuffer.slice(0, size))
         logger.info(s"Flushed ${size} event(s) in " + TimeUtils.formatDuration(Duration.between(start, Instant.now())))
      }
   }

}
