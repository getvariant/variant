package com.variant.server.test.spec

import java.util.concurrent.Executors
import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.atomic.AtomicInteger
import org.scalatest.concurrent.ScalaFutures

trait Async extends EmbeddedServerSpec {

   implicit val ec = server.actorSystem.dispatcher

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
   protected def joinAll(timeout: Long = 20000) {
      var wated = 0
      while (taskCount.get > 0 && wated < timeout) {
         Thread.sleep(200)
         wated += 200
      }
      if (wated >= timeout) fail(s"Unexpected timeout waiting for background threads for $timeout millis")

      unexpectedException.foreach { t =>
         unexpectedException = None
         // sbt often swallaws the cause stack, so print it before rethrowing.
         t.printStackTrace(System.err)
         throw new Exception(s"Async block crashed: ${t.getMessage}", t)
      }
   }
}