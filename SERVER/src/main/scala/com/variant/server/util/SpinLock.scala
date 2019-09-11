package com.variant.server.util

class SpinLock {
  
   def lock() = ???
   
   def unlock() = ???
   
   /**
    * Execute a block of code in isolation, protected by this spin lock.
    * Hides the lock/unlock details.
    */
   def synchronized(block: =>Unit) {
      lock()      
      try { 
         block 
      }
      finally { 
         unlock() 
      }
   }

}