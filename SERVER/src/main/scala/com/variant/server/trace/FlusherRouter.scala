package com.variant.server.trace

import java.time.Duration
import java.time.Instant

import scala.concurrent.blocking

import com.typesafe.scalalogging.LazyLogging
import com.variant.share.util.TimeUtils
import com.variant.server.api.TraceEventFlusher
import com.variant.server.boot.ServerExceptionInternal

import akka.actor.Actor
import akka.actor.Props

/**
 * Trace event flusher Actor.
 */
object FlusherRouter extends LazyLogging {

   // Start the sole vacuum actor.
   def props(pool: FlusherThreadPool) = Props(new FlusherRouter(pool))
      
   /**
    * Protocol
    */
   final case class Flush(header: EventBufferCache.Header)

}

private class FlusherRouter(pool: FlusherThreadPool) extends Actor with LazyLogging {

   import FlusherRouter._

   /**
    * Process the flush msg.
    */
   override def receive() = {

      case Flush(header: EventBufferCache.Header) =>

         logger.debug(s"Received buffer $header for flushing")

         val flusher = header.flusherService.getFlusher // Just one for now.
         
         pool.submit {

            // We can trust bufferIx, so long as it's < max size.
            // Remember that header.bufferIx is the index of the last insertion, i.e. one less than length.
            val actualLength = (header.bufferIx.get + 1) min header.buffer.length

            // Consistency check.
            for (i <- 0 until actualLength) {
               if (header.buffer(i) == null) {
                  throw ServerExceptionInternal(s"Inconsitent buffer: null at index $i")
               }
            }

            val start = Instant.now
      
            logger.debug(s"About to flush ${actualLength} trace events")
            try {
               blocking {
                  flusher.flush(header.buffer, actualLength)
               }
               logger.info(s"Flushed ${actualLength} event(s) in " + TimeUtils.formatDuration(Duration.between(start, Instant.now())))
            } catch {
               case t: Throwable => {
                  logger.error("Ignored following unhandled exception", t)
               }
            } 

            header.status = EventBufferCache.HeaderStatus.FREE

            // We null out the array as extra precaution, in case next time
            // this buffer is flushed the client code in TraceEventFlusher.flush()
            // fails to honor the size parameter and attempts to flush all events.
            for (i <- 0 until header.buffer.length) header.buffer(i) = null

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
            logger.error("Ignored following unhandled exception", t)
         }
      } finally {
         header.status = EventBufferCache.HeaderStatus.FREE
         // We null out the array as extra precaution, in case next time
         // this buffer is flushed the client code in TraceEventFlusher.flush()
         // fails to honor the size parameter and attempts to flush all events.
         for (i <- 0 until header.buffer.length) header.buffer(i) = null
      }
   }

}
