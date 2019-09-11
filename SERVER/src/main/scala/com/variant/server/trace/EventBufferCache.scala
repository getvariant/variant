package com.variant.server.trace

import java.util.concurrent.ConcurrentLinkedQueue

import scala.collection.mutable.ArrayBuffer

import com.variant.server.api.FlushableTraceEvent
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import java.time.Instant
import com.variant.server.util.SpinLock

/**
 * Trace event queue.
 * Event producers put events to this queue. `FlusherActor`s take events from this queue 
 * and pass them to `Flusher`s.
 * 
 *  The Underlying data structure is Java's concurrent infinitely growable ConcurrentLinkedQueue,
 *  whose size() method has linear complexity. Since we use size() quite a bit, we introduce the
 *  atomic counter to make the complexity of size() constant.
 * 
 */
class EventBufferCache(val bufferSize: Int, val buffers: Int) {

   private object Entry {
      val FREE = 0
      val CURRENT = 1
      val PENDING = 2
      val FLUSHING = 3
   }
   
   private class Entry {
  
      import Entry._
  
      var timestamp = Instant.now
      var status: Int = FREE
      val buffer = new Array[FlushableTraceEvent](bufferSize)
      val bufferIx = new AtomicInteger(0)     
   }

   // Cache entries
   private[this] val entryList = new Array[Entry](buffers)
   
   // Overflow buffer for events that came during switch to new buffer.
   private[this] val overflowBuffer = new Array[FlushableTraceEvent](Math.ceil(bufferSize.asInstanceOf[Double]/2).toInt)
   private[this] val overflowBufferIx = new AtomicInteger(0)
      
   // Index of the current entry in the entry list.
   private[this] var currentEntry: Option[Entry] = None
   
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
   
   /**
    * Find new current buffer. Must be thread safe because may be called by foreground threads 
    * and by agents via read().
    */
   private[this] def advanceCurrentBuffer() {
      
      // Switch to the next free buffer, if any.
      currBufferAdvanceSpinLock.synchronized {

         currentEntry = 
            entryList
               .filter(_.status == Entry.FREE)
               .reduceOption{ (x,y) => if (x.timestamp.isBefore(y.timestamp)) x else y }
         
      }
      
      // Ping F
   }
   val currBufferAdvanceSpinLock = new SpinLock
   
   /**
    * Add single event to the queue. Must be thread safe because called by foreground threads.
    */
   def write(event: FlushableTraceEvent) {
      
      currentEntry match {
         
         case Some(ce) =>
            // We got current entry in the buffer cache table.
            val buffIx = ce.bufferIx.incrementAndGet()
            if (buffIx < bufferSize) {
               // Current buffer has room. Insert there.
               ce.buffer(buffIx) = event
            }
            else if (buffIx == bufferSize) {
               // Current buffer just ran out of room and it's on us to try looking for new current buffer.
               /// Current Entry Switch
            }
            else {
               // Current buffer just ran out of room, and some one else is looking for the new current buffer.
               // Optimistically, try adding to the overflow buffer.
               val overflowBuffIx = overflowBufferIx.incrementAndGet()
               if (overflowBuffIx < overflowBuffer.size) {
                  // Overflow buffer has room
                  overflowBuffer(overflowBuffIx) = event
               }
               else {
                  // The overflow buffer also ran out of room
                  discard(event)
               }
            }
            
         case None =>
            // No current entry in the buffer cache table.  Try to make one.
            advanceCurrentBuffer()
            
            // If we were able to find a free buffer and make it current, reenter this method. In theory,
            // it's a race condition, but in practice it's not likely that the current buffer will get all filled
            // up underneath us.
            if (currentEntry.isDefined) write(event)
            else discard(event)
      }
   }
   
   /**
    * Read and delete from the queue at most given number of elements.
    * Return the number of events we've actually put in the buffer.
    */
   def read(buffer: Array[FlushableTraceEvent]): Int = {
   
      var counter = 0
      var next: FlushableTraceEvent = null
      do {
         next = queue.poll()
         if (next != null) {
            buffer(counter) = next
            counter += 1 
         }
      } while (counter < buffer.size && next != null)

         _size.addAndGet(-counter)
   }
 
}
