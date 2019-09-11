package com.variant.server.akka

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import java.time.Instant
import akka.actor.Props
import akka.actor.actorRef2Scala
import com.variant.server.boot.VariantServer
import com.variant.server.api.FlushableTraceEvent
import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import com.variant.server.api.TraceEventFlusher
import com.variant.core.util.TimeUtils
import java.time.Duration

/**
 * Trace event flusher Actor. The actual work of delegating to the underlying client flusher.
 */
object FlusherActor {

   def props(flusher: TraceEventFlusher): Props = Props(new FlusherActor(flusher))
   
   val name = "FlusherActor"

   /**
    * Protocol.
    */
   final case class Flush(buff: Array[FlushableTraceEvent], size: Int)
       
}

class FlusherActor(flusher: TraceEventFlusher) extends Actor with LazyLogging {

   import FlusherActor._

   /**
    * Run the vacuum once and schedule next run after externally configured delay.
    */
   override def receive() = {

      case Flush(buff, size) =>

         val top = Instant.now
         logger.debug(s"About to flush ${size} events...")
         try {
            val start = Instant.now();
            flusher.flush(buff, size)
            logger.info(s"Flushed ${buff.size} event(s) in " + TimeUtils.formatDuration(Duration.between(start, Instant.now())))
         } catch {
            case t: Throwable => {
               logger.error("Unhandled exception (ignored)", t)
            }
         }   
   }

}
