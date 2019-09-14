package com.variant.server.trace

import java.util.concurrent.ConcurrentLinkedQueue

import scala.collection.mutable.ArrayBuffer

import com.variant.server.api.FlushableTraceEvent
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import java.time.Instant
import com.variant.server.util.SpinLock
import com.variant.server.api.TraceEventFlusher
import com.variant.server.schema.ServerFlusherService
import com.variant.server.impl.FlushableTraceEventImpl
import com.variant.server.boot.VariantServer
import akka.actor.ActorRef
import com.variant.server.akka.FlusherRouter

/**
 * Trace event buffer cache.
 *
 */
class EventBufferCache(server: VariantServer) {   

   val bufferSize = server.config.eventFlushSizse
   val buffers = Math.ceil(server.config.eventWriterBufferSize.asInstanceOf[Float]/bufferSize).toInt
   
   // Buffer header table (BHT)
   private[this] val headerTable = new Array[Header](buffers)

   /**
    * We were unable to find room for an event and need to discard it.
    * When this happens, we want to emit warning messages every once in a while.
    */
   private[this] def discard(event: FlushableTraceEvent) {
      if ...
   }
   private[this] val discardMessageeFrequncy = FiniteDuration(5, TimeUnit.MILLISECONDS)
   private[this] var lastDiscardMessageTimetamp = Instant.now
   private[this] val discardCount = new AtomicInteger(0) 
   
   private[this] val headerTableLatch = new SpinLock

   /**
    * Find new current buffer. Must be thread safe because may be called by foreground threads 
    * and by agents via read().
    */
   private[this] def findFreeBufferFor(event: FlushableTraceEventImpl): Boolean = {

      headerTableLatch.synchronized {
 
         // Confirm there's still no current buffer for the given flusher service.
         headerTable.find(head => head.status == HeaderStatus.CURRENT && head.flusherService == event.getFlusherService) match {
            
         case Some(head: Header) =>
            // The current buffer has just been created by another thread. Use it
            // NB: we are ignoring the extremely unlikely case that this new current buffer that was just created.
            // may already be full.
            head.buffer(head.bufferIx.incrementAndGet) = event;
            true
            
         case None =>
            // Indeed, no current buffer for the given flusher service. Look for a free buffer now
            // and if successful make in the new current buffer.
            
            headerTable.find(_.status == HeaderStatus.FREE) match {
               
               // We found a free buffer. Make it current, suitable for the event and store the event at slot 0.
               case Some(head) => 
                  head.status = HeaderStatus.CURRENT
                  head.buffer(0) = event
                  head.bufferIx.set(0)
                  head.flusherService = event.getFlusherService
                  true
                  
               case None =>   
                  false
            }         
         }
      }
   }
   
   /**
    * Add single event to the queue. Must be thread safe because called by foreground threads.
    */
   def write(event: FlushableTraceEventImpl) {

      headerTable.find {
         head => head.status == HeaderStatus.CURRENT && head.flusherService == event.getFlusherService 
      }
      match {
         
      case Some(head: Header) =>
         // We found the current header.
         val buffIx = head.bufferIx.incrementAndGet()
         if (buffIx < bufferSize) {
            // Current buffer has room, use it!
            head.buffer(buffIx) = event
         }
         else if (buffIx >= bufferSize) {

            // If we are first to overflow the current buffer, send it off to the flusher router.
            if (buffIx == bufferSize) {
               // Current buffer just ran out of room and it's on us to try looking for a new one.
               head.status = HeaderStatus.FLUSHING
               FlusherRouter.ref ! FlusherRouter.Flush(head)
            }

            // Try to find a new current buffer and if successful stick the event in there.
            if (!findFreeBufferFor(event)) {
               discard(event)
            }
            
         }
         
      case None =>
            // No current buffer.  
            // Try to find a new current buffer and if successful stick the event in there.
            if (!findFreeBufferFor(event)) {
               discard(event)
            }
      }
   }
   
   /**
    * Called by FlusherRouter actor whenever it determines that min flushing delay has been reached since the last flush.
    */
   def flushAll() {

      headerTableLatch.synchronized {
         
         headerTable.filter(_.status == HeaderStatus.CURRENT).foreach { head =>
            head.status = HeaderStatus.FLUSHING
            FlusherRouter.ref ! FlusherRouter.Flush(head)
         }
      }
   }
 
}

/**
 * Buffer header.
 */
object HeaderStatus {
   val FREE = 0
   val CURRENT = 1
   val FLUSHING = 2
}

class Header(bufferSize: Int) {
    
   var status: Int = HeaderStatus.FREE
   val buffer = new Array[FlushableTraceEvent](bufferSize)
   val bufferIx = new AtomicInteger(0)
   
   // Target flusher service which will be responsible for flushing this event
   var flusherService: ServerFlusherService = _
}
