package com.variant.server.akka

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

/**
 * The Main class.
 * @author Igor
 */
object ServerMain extends App {

   // set up ActorSystem and other dependencies here
   implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
   implicit val materializer: ActorMaterializer = ActorMaterializer()
   implicit val executionContext: ExecutionContext = system.dispatcher
   //#server-bootstrapping

   //val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

   //#main-class
   // from the UserRoutes trait
   // lazy val routes: Route = userRoutes
   //#main-class

   //#http-server
   val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(Router.routs, "localhost", 8080)

   serverBinding.onComplete {
      case Success(binding) =>
         sys.addShutdownHook { shutdownHook(binding) }

         println(s"Server online at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/")
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
