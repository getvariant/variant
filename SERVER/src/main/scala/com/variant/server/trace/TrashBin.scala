package com.variant.server.trace

import scala.concurrent.duration.FiniteDuration
import com.variant.server.api.FlushableTraceEvent
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit
import com.typesafe.scalalogging.LazyLogging
import java.time.Instant
import com.variant.server.boot.VariantServer
import com.variant.server.boot.ServerMessageLocal

/**
 * This is where trace events, for which we were unable to find room in the
 * buffer cache, come to die. When this happens, we want to emit warning messages
 * every once in a while, as given by the reportFrequency param.
 */
private[trace] class TrashBin(reportFrequency: FiniteDuration)(implicit server: VariantServer) extends LazyLogging {

   private[this] val trashCount = new AtomicInteger(0)

   /*
    * Periodically, report the trashed event count to the log.
    * This is a tiny task, so we ran it on the actorSystem's EC.
    */
   server.actorSystem.scheduler.schedule(
      initialDelay = reportFrequency,
      interval = reportFrequency) {

      // Atomically, get current value and reset to 0
      val currTrashCount = trashCount.getAndAccumulate(0, (_, _) => 0)

      if (currTrashCount > 0) {
         logger.warn(ServerMessageLocal.TRASHED_EVENTS_COUNT.asMessage(currTrashCount.toString, reportFrequency.toString))
      }

   }(server.actorSystem.dispatcher)

   /**
    * Trash an event.
    */
   def trash(event: FlushableTraceEvent) {
      trashCount.incrementAndGet
   }
}
