package com.variant.server.boot

import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.Http
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import sun.misc.Signal
import sun.misc.SignalHandler
import scala.util.Try

/**
 * Standalone server entry point.
 */
object Boot extends App with LazyLogging {

   // All defaults build the regular production server.
   val server = VariantServer.builder.build

   // We shutdown server by sending it the INTERRUPT signal, which we catch right here.
   // TODO This is a rather ungraceful shutdown because we're not letting in-flight requests
   // to complete. See #253.
   Signal.handle(new Signal("INT"), new SignalHandler() {
      def handle(sig: Signal) {
         server.shutdown()
      }
   })

   // Need this?
   Await.result(server.actorSystem.whenTerminated, Duration.Inf)
}

