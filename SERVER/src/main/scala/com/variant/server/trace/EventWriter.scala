package com.variant.server.impl
/*
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue
import com.variant.server.api.FlushableTraceEvent
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerFlusherService
import java.time.Instant
import java.time.Duration
import com.variant.core.util.TimeUtils
import com.typesafe.scalalogging.LazyLogging
import akka.event.jul.Logger

class TraceEventWriter(private val flushService: ServerFlusherService)(implicit server: VariantServer)
   extends LazyLogging with ConfigKeys {

   private val config = server.config

   val flusher = flushService.getFlusher

   // The underlying buffer is a non-blocking, unbounded queue. We will enforce the soft upper bound,
   // refusing inserts that will put the queue size over the limit, but not worrying about
   // a possible overage due to concurrency.
   private val bufferQueue = new ConcurrentLinkedQueue[FlushableTraceEvent]()

   // The flusher thread.
   private val flusherThread = new FlusherThread();
   // Not a daemon. Intercept interrupt and flush the buffer before exiting.
   flusherThread.setDaemon(false)
   flusherThread.setName("Event Flusher For Schema " + flushService.getSchema.getMeta.getName)
   flusherThread.start()

   //---------------------------------------------------------------------------------------------//
   //                                          PUBLIC                                             //
   //---------------------------------------------------------------------------------------------//

   logger.debug("Event writer started.");

   /**
    * Flush this writer immediately, without waiting for the internal flush.
    */
   def flush() = {
      bufferQueue.synchronized {
         flusherThread.flush()
      }
   }

   /**
    * Shutdown this event writer.
    * Cannot be used after this.
    */
   def shutdown() {
      // Destroy the flusher thread.
      // TODO this should be an actor
      flusherThread.interrupt()

      // One last flush
      flush()

      flusher.destroy()

      logger.debug("Event writer shutdown.");
   }

   /**
    * Write collection of events to the queue.  This method never blocks:
    * if there's no room on the queue to hold all the events, write as many as we can in the
    * order of the collection's iterator and ignore the rest. Log ERROR dropped events.
    *
    * @param event decoratedEvent
    *
    * @return number of elements actually written.
    */
   def write(event: FlushableTraceEvent) {

      // We don't worry about possible concurrent writes because the underlying
      // queue implementation is thread safe and unbound.  It's okay to temporarily
      // go over the maxQueueSize due to concurrency, so long as we eventually shrink back.
      // But we won't go over it knowingly.
      val currentSize = bufferQueue.size();
      var dropCount = 0

      if (currentSize < maxBufferSize) {
         bufferQueue.add(event);
      } else {
         val msg = "Dropped %d event(s) due to buffer overflow. Consider increasing %s system property (current value %d)"
         logger.trace(msg.format(1, EVENT_WRITER_BUFFER_SIZE, maxBufferSize))
         dropCount += 1
         if (dropCount % 1000 == 0)
            logger.info(msg.format(1000, EVENT_WRITER_BUFFER_SIZE, maxBufferSize))
      }

      // Block momentarily to wake up the flusher thread if the queue has reached the full size.
      bufferQueue.synchronized {
         if (currentSize >= fullSize) bufferQueue.notify();
      }

   }

   /**
    * Flusher thread.
    * Removes events from the queue and flushes them to an event persistence interface.
    *
    * @author Igor.
    *
    */
   class FlusherThread extends Thread {

      override def run() {

         var interruptedExceptionThrown = false;
         var timeToGo = false;
         while (!timeToGo) {

            try {
               // We were either woken up because the queue is over pctFull,
               // or we timed out waiting. Flush either way, event if under pctEmpty,
               // and the keep flushing until under pctEmpty.
               do {
                  flush();
               } while (!isInterrupted() && bufferQueue.size() >= emptySize);

               // Block until the queue is over pctFull again, but with timeout.
               if (!isInterrupted()) {
                  bufferQueue.synchronized {
                     bufferQueue.wait(maxDelayMillis);
                  }
               }
            } catch {
               case _: InterruptedException => interruptedExceptionThrown = true
               case t: Throwable => {
                  logger.error("bufferQueue = " + bufferQueue);
                  logger.error("Unhandled exception in event writer", t)
               }
            }

            if (interruptedExceptionThrown || isInterrupted()) {
               try {
                  flush();
               } catch {
                  case t: Throwable => logger.error("Unhandled exception in event flusher", t);
               }
               timeToGo = true
            }
         }
      }

      /**
       * Flush the entire buffer.
       */
      def flush() {

         var events = new LinkedList[FlushableTraceEvent]();
         while (bufferQueue != null && !bufferQueue.isEmpty()) events.add(bufferQueue.poll());
         logger.debug(s"About to flush ${events.size} events")
         if (!events.isEmpty()) {
            var start = Instant.now();
            flusher.flush(events);
            logger.info("Flushed " + events.size() + " event(s) in " + TimeUtils.formatDuration(Duration.between(start, Instant.now())))
         }
      }
   }

}
*/ 