package com.variant.server.util

class SpinLock {

   def lock() {
      println("*** implement me ***")
   }

   def unlock() {
      println("*** implement me ***")
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

}