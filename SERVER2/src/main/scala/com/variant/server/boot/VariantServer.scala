package com.variant.server.boot

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }

import com.typesafe.scalalogging.LazyLogging

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

/**
 * The Main class.
 * @author Igor
 */
object VariantServer extends App with LazyLogging {

   val name = getClass.getPackage.getImplementationTitle
   val version = getClass.getPackage.getImplementationVersion

   // set up ActorSystem and other dependencies here
   implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
   implicit val materializer: ActorMaterializer = ActorMaterializer()
   implicit val executionContext: ExecutionContext = system.dispatcher
   //#server-bootstrapping

   //val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

   //#http-server
   val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(Router.routs, "localhost", 8080)

   serverBinding.onComplete {
      case Success(binding) =>
         sys.addShutdownHook { shutdownHook(binding) }
         logger.debug("foo")
         println(s"Server online at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/" + name + version)
      case Failure(e) =>
         Console.err.println(s"Server could not start!")
         e.printStackTrace()
         system.terminate()
   }

   // Block until the server completes.
   Await.result(system.whenTerminated, Duration.Inf)

   def shutdownHook(binding: Http.ServerBinding) = {
      println(s"Server shutdown at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/")
   }
}
