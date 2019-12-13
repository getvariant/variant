package com.variant.server.util

import java.util.concurrent.atomic.AtomicBoolean
import scala.util.Random
import com.typesafe.scalalogging.LazyLogging

/**
 * Spin lock with exponential backoff.
 */
object SpinLock {

   // Spin for up to this many nanoseconds
   val MIN_DELAY_NANOS = 1000
   // Then sleep for geometrically longer periods of time up to this many milliseconds
   val MAX_DELAY_MILLIS = 500

}
class SpinLock extends LazyLogging {

   import SpinLock._
   
   private[this] val state = new AtomicBoolean(false)
   private[this] val backoff = new ExpBackoff(MIN_DELAY_NANOS, MAX_DELAY_MILLIS);

   def lock() {
      val start = System.nanoTime
      var succeeded = false
      while (!succeeded) {
         while (state.get()) {};
         if (!state.getAndSet(true)) {
            succeeded = true
         } else if ((System.nanoTime - start) > MIN_DELAY_NANOS)  {
            backoff.backoff();
         }
      }
   }

   def unlock() {
      state.set(false);
   }

   /**
    * Execute a block of code in isolation, protected by this spin lock.
    * Hides the lock/unlock details.
    */
   def synchronized[T](block: => T): T = {
      lock()
      try {
         block
      } finally {
         unlock()
      }
   }

   /**
    * Computes the next backoff period and sleeps this thread for it.
    */
   class ExpBackoff(minDelayNanos: Long, maxDelayMillis: Long) {
      
      var delay: Long = (minDelayNanos / 1000000 * ( 1 + Random.nextFloat )).asInstanceOf[Long]
      
      def backoff() {
         logger.trace(s"Sleeping for $delay millis")
         Thread.sleep(delay)
         delay = Math.min(2 * delay, maxDelayMillis)
      }
   }
}