package com.variant.server.test

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.Future
import scala.collection.mutable.ArrayBuffer


class BaseSpecWithServerAsync extends BaseSpecWithServer {
  
   private val pool = Executors.newFixedThreadPool(4)
   private val futures = ArrayBuffer[Future[_]]()
   
   /**
    * Execute a function on the pool
    */
   protected def async(bloc: => Unit) = {

      futures += pool.submit (
            new Runnable {
               def run() = bloc
            }
      )
   }

   /**
    * Block for all functions to complete.
    */
   protected def joinAll() = {
      futures.foreach(_.get) 
   }
}