package com.variant.server.boot

import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.Http
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import sun.misc.Signal
import sun.misc.SignalHandler

/**
 * Standalone server bootup, when we own the main().
 */
object Boot extends App with LazyLogging {

   val server = new VariantServerImpl

   // We shutdown server by sending it the INTERRUPT signal, which we catch right here.
   Signal.handle(new Signal("INT"), new SignalHandler() {
      def handle(sig: Signal) {
         server.shutdown()
      }
   })
   
   // We probably don't even need to do this because we only terminate
   // with a signal, which will trigger JVM shutdown.
   Await.result(server.actorSystem.whenTerminated, Duration.Inf)

}

