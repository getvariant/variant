package com.variant.server.trace

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.collection.mutable.ListBuffer

import com.variant.server.api.FlushableTraceEvent
import com.variant.server.boot.VariantServer
import com.variant.server.impl.FlushableTraceEventImpl
import com.variant.server.schema.ServerFlusherService
import com.variant.server.util.SpinLock

import com.typesafe.scalalogging.LazyLogging
import com.variant.server.boot.ServerExceptionInternal
import akka.util.Timeout
import scala.concurrent.Await
import com.variant.server.boot.ServerMessageLocal
import com.variant.server.boot.ServerExceptionLocal

object EventBufferCache {

   class Header(bufferSize: Int) {
      var status: Int = HeaderStatus.FREE
      var timestamp: Long = 0
      val buffer = new Array[FlushableTraceEvent](bufferSize)
      val bufferIx = new AtomicInteger(0)

      // Target flusher service which will be responsible for flushing this event
      var flusherService: ServerFlusherService = _

      override def toString() =
         s"Header(status=$status, timstamp=$timestamp, buffer=Array($bufferSize), bufferIx=${bufferIx.get})"
   }

   object HeaderStatus {
      val FREE = 0
      val CURRENT = 1
      val FLUSHING = 2
   }

   def apply(server: VariantServer) = new EventBufferCache()(server)
}

/**
 * Trace event buffer cache. Top level class for all tracing related objects.
 *
 */
class EventBufferCache(implicit server: VariantServer) extends LazyLogging {

   import EventBufferCache._

   // Public for tests
   val bufferSize = server.config.eventWriterFlushSize
   val buffers = Math.ceil(server.config.eventWriterBufferSize.asInstanceOf[Float] / bufferSize).toInt

   // Buffer header table (BHT)
   private[this] val headerTable = Array.fill(buffers) { new Header(bufferSize) }

   // Spin lock guarding th eheader table.
   private[this] val headerTableLatch = new SpinLock
   // The trash can where discarded events go to die
   private[this] val trashBin = new TrashBin(FiniteDuration(5, TimeUnit.SECONDS))

   // Schedule a force flush of those buffers whose age exceeds the
   // configurable max delay value.
   val maxDelayMillis = server.config.eventWriterMaxDelay * 1000

   // Schedule flush due to max delay time.
   server.actorSystem.scheduler.schedule(
      initialDelay = FiniteDuration(maxDelayMillis, MILLISECONDS),
      interval = FiniteDuration(maxDelayMillis, MILLISECONDS)) {
         flushOlderThan(maxDelayMillis)
      }(server.actorSystem.dispatcher);

   // Dedicated pool for long-running blocking flushing tasks.
   val flusherThreadPool = new FlusherThreadPool(server.config)

   val flusherRouterActor = server.actorSystem.actorOf(
      FlusherRouter.props(flusherThreadPool), name = "FlusherRouter")

   /**
    * Find new current buffer. Must be thread safe because may be called by foreground threads
    * and by agents via read().
    */
   private[this] def findFreeBufferFor(event: FlushableTraceEventImpl): Boolean = {

      // If we find the buffer, it goes here.
      var foundHeader: Option[Header] = None
      var createdHere = false

      val result = headerTableLatch.synchronized {

         // Confirm there's still no current buffer for the given flusher service.
         headerTable.find { head =>
            head.status == HeaderStatus.CURRENT && head.flusherService == event.getFlusherService
         } match {
            case Some(head: Header) =>
               // The current buffer has just been created by another thread. Use it.
               val ix = head.bufferIx.incrementAndGet
               if (ix >= bufferSize) throw ServerExceptionInternal("Buffer overflow")
               head.buffer(ix) = event
               foundHeader = Some(head)
               true

            case None =>
               // Indeed, no current buffer for the given flusher service. Look for a free buffer now
               // and if successful make in the new current buffer.

               headerTable.find(_.status == HeaderStatus.FREE) match {
                  case Some(head) =>
                     // We found a free buffer. Make it current, suitable for the event and store the event at slot 0.
                     head.status = HeaderStatus.CURRENT
                     head.timestamp = System.currentTimeMillis
                     head.buffer(0) = event
                     head.bufferIx.set(0)
                     head.flusherService = event.getFlusherService
                     foundHeader = Some(head)
                     createdHere = true
                     true

                  case None =>
                     false
               }
         }
      }

      // Log outside the critical section protected by spin lock.
      foundHeader match {
         case Some(head) =>
            if (createdHere)
               logger.trace(s"Added event ${event} to free buffer ${head} at index 0")
            else
               logger.trace(s"Added event ${event} to just current buffer ${head} at index 0")

         case None =>
            logger.trace("Failed to find a free buffer")
      }

      result
   }

   /**
    * Flush all current buffers older than some duration. Used for
    * 1) Enforce the minimum delay.
    * 2) The side-effect of above is that no buffer will be permanently clogged
    *    if some schema stopped generating trace.
    * 3) Flushing all the buffers on server shutdown.
    * 4) Caller may block on the future if it wishes.
    */
   private def flushOlderThan(ageMillis: Long) {

      logger.debug(s"Flusning all buffers older than $ageMillis millis")

      val minTimestamp = System.currentTimeMillis() - ageMillis

      // To reduce the size of the critical section, protected by the spin lock,
      // we collect the buffers in a list so as to message the fluser router
      // outside of the critical section.
      val flushableList = ListBuffer[Header]()

      headerTableLatch.synchronized {

         headerTable
            .filter { header =>
               header.status == HeaderStatus.CURRENT && header.timestamp < minTimestamp
            }
            .foreach { header =>
               header.status = HeaderStatus.FLUSHING
               flushableList += header
            }
      }

      flushableList.foreach { head => flusherRouterActor ! FlusherRouter.Flush(head) }

   }

   /**
    * Add single event to the queue. Must be thread safe because called by foreground threads.
    */
   def write(event: FlushableTraceEventImpl) {

      headerTable.find { head =>
         head.status == HeaderStatus.CURRENT && head.flusherService == event.getFlusherService
      } match {

         case Some(head: Header) =>
            // We found the current header.
            val buffIx = head.bufferIx.incrementAndGet()
            if (buffIx < bufferSize) {
               // Current buffer has room, use it!
               logger.trace(s"Added event ${event} to current buffer ${head} at index ${buffIx}")
               head.buffer(buffIx) = event

               // If we just used the last slot, send buffer off to the flusher router.
               if (buffIx == bufferSize - 1) {
                  // Current buffer just ran out of room and it's on us to try looking for a new one.
                  head.status = HeaderStatus.FLUSHING
                  flusherRouterActor ! FlusherRouter.Flush(head)
                  logger.trace(s"Scheduled buffer $head for flushing")
               }

            } else {
               // There was no room in this buffer, even though it was current when we checked.
               // Try to find a new current buffer and if successful stick the event in there.
               if (!findFreeBufferFor(event)) trashBin.trash(event)
            }

         case None =>
            // No current buffer.
            // Try to find a new current buffer and if successful stick the event in there.
            if (!findFreeBufferFor(event)) {
               trashBin.trash(event)
            }
      }
   }

   /**
    * Number of events in the cache that have not yet been flushed,
    * i.e. those contained in current and flushing buffers. Tests only.
    */
   def size: Integer = {
      headerTable.filter(_.status != HeaderStatus.FREE).map(_.bufferIx.get + 1).fold(0)(_ + _)
   }

   /**
    * Synchronously shutdown this buffer cache.
    * The actor system is assumed to still be around, but all the schemata undeployed,
    * i.e. it's safe to assume no new trace events will be written.
    * When this call returns, all pending events are flushed and the fluser thread pool is shutdown.
    * Not thread safe.
    */
   def shutdown(timeout: Duration = Duration(30, SECONDS)) {

      flushOlderThan(0)
      // block for flushing buffers.
      val start = System.currentTimeMillis
      var timedOut = false
      while (!timedOut && headerTable.exists(_.status != HeaderStatus.FREE)) {
         Thread.sleep(250)
         timedOut = (System.currentTimeMillis - start) >= timeout.toMillis
      }

      flusherThreadPool.shutdown()

      if (timedOut) {
         throw ServerExceptionLocal(ServerMessageLocal.EVENT_BUFFER_CACHE_SHUTDOWN_TIMEOUT, timeout.toMillis.toString)
      }

   }

}

