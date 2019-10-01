package com.variant.server.util

import java.util.concurrent.atomic.AtomicBoolean
import scala.util.Random
import com.typesafe.scalalogging.LazyLogging

/**
 * Spin lock with geometric backoff.
 */
object SpinLock {

   // Spin for up to this many nanoseconds
   val MIN_DELAY_NANOS = 1000
   // Then sleep for geometrically longer periods of time up to this many milliseconds
   val MAX_DELAY_MILLIS = 500

}
class SpinLock extends LazyLogging {

   import SpinLock._
   
   val state = new AtomicBoolean(false)

   def lock() {
      val backoff = new Backoff(MIN_DELAY_NANOS, MAX_DELAY_MILLIS);
      while (true) {
         while (state.get()) {};
         if (!state.getAndSet(true)) {
            return;
         } else {
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

   class Backoff(minDelayNanos: Long, maxDelayMillis: Long) {
      var delay: Long = (minDelayNanos * 1000000 * ( 1 + Random.nextFloat )).asInstanceOf[Long]
      println
      def backoff() {
         logger.trace(s"Sleeping for $delay millis")
         Thread.sleep(delay)
         delay = Math.min(2 * delay, maxDelayMillis)
      }
   }
}