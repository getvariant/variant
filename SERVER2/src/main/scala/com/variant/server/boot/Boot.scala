package com.variant.server.boot

import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.Http
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Standalone server bootup.
 */
object Boot extends App with LazyLogging {

   val server = new VariantServerImpl

   // We probably don't even need to do this because we only terminate
   // with a signal, which will trigger JVM shutdown.
   Await.result(server.actorSystem.whenTerminated, Duration.Inf)

}

