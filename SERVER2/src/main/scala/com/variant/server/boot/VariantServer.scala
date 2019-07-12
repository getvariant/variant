package com.variant.server.boot

import java.time.{ Duration => JavaDuration }
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }

import com.typesafe.scalalogging.LazyLogging

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.variant.core.util.TimeUtils
import java.lang.management.ManagementFactory
import com.variant.server.impl.ConfigurationImpl

/**
 * The Main class.
 * @author Igor
 */
object VariantServer extends App with LazyLogging {

   private val productName = "Variant AIM Server release " + getClass.getPackage.getImplementationVersion

   // Bootstrap external configuration.
   val config = new ConfigurationImpl(ConfigLoader.load("/variant.conf", "/variant-default.conf"));

   // set up ActorSystem and other dependencies here
   implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
   implicit val materializer: ActorMaterializer = ActorMaterializer()
   implicit val executionContext: ExecutionContext = system.dispatcher

   //val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

   //#http-server
   val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(Router.routs, "localhost", 8080)

   serverBinding.onComplete {

      case Success(binding) =>
         sys.addShutdownHook { shutdownHook(binding) }
         logger.debug("foo")
         logger.info(ServerMessageLocal.SERVER_BOOT_OK.asMessage(
            productName,
            binding.localAddress.getPort.asInstanceOf[Object],
            TimeUtils.formatDuration(JavaDuration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()))))

      case Failure(e) =>
         logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
         logger.error(e.getMessage, e)
         system.terminate()
   }

   // Block until the server completes.
   Await.result(system.whenTerminated, Duration.Inf)

   def shutdownHook(binding: Http.ServerBinding) = {
      println(s"Server shutdown at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/")
   }
}
