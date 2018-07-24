package com.variant.server.test.spec

import java.util.concurrent.Executors
import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.atomic.AtomicInteger
import org.scalatest.concurrent.ScalaFutures


class BaseSpecWithServerAsync extends EmbeddedServerSpec {
  
   implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))
   val taskCount = new AtomicInteger(0)
   
   var unexpectedException: Option[Throwable] = None
   
   /**
    * Execute a function on the pool
    */
   protected def async(block: => Unit) = {

      taskCount.addAndGet(1)

      val future = Future {
         try {
            block
         } catch {
            case t: Throwable => 
               unexpectedException = Some(t)
         } finally {
            taskCount.decrementAndGet()
         }
      }      
   }
      
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
      
      unexpectedException.foreach { t => 
         unexpectedException = None
         throw new Exception(s"Async block crashed: ${t.getMessage}", t) }
   }

   /**
    * Block for all functions to complete.
    * TODO: replace with java.util.concurrent.CountDownLatch
    */
   protected def joinAll() { joinAll(20000) }
}