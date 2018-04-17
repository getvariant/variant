package com.variant.server.test.spec

import java.util.concurrent.Executors
import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.atomic.AtomicInteger


class BaseSpecWithServerAsync extends BaseSpecWithServer {
  
   implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

   // private val pool = Executors.newFixedThreadPool(4)
   //private val futures = ArrayBuffer[Future[_]]()
   val taskCount = new AtomicInteger(0)
   //private val futures = ArrayBuffer[Future[_]]()
   
   /**
    * Execute a function on the pool
    */
   protected def async(block: => Unit) = {

      taskCount.addAndGet(1)

      val future = Future {
         block
         taskCount.decrementAndGet()
      }
      
      future.onFailure {
        case t => {
           taskCount.decrementAndGet()
           throw new Exception(s"Async block crashed: ${t.getMessage}", t)
        }
      }

   }
   
   /**
    * Async doesnt work somehow, so this for now.
    *
   protected def async(bloc: => Unit) = bloc
   */
   
   /**
    * Block for all functions to complete.
    * TODO: replace with java.util.concurrent.CountDownLatch
    */
   protected def joinAll(timeout: Long) {
      var wated = 0
      while (taskCount.get > 0 && wated < timeout) {
         Thread.sleep(200)
         wated += 200
      }
      if (wated >= timeout) fail("Unexpected timeout waiting for background threads.")
   }

   /**
    * Block for all functions to complete.
    * TODO: replace with java.util.concurrent.CountDownLatch
    */
   protected def joinAll() { joinAll(20000) }
}