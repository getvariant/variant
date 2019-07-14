package com.variant.server.boot

import java.time.{ Duration => JavaDuration }
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import com.typesafe.scalalogging.LazyLogging

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.variant.core.util.TimeUtils
import java.lang.management.ManagementFactory
import com.variant.server.impl.ConfigurationImpl
import com.variant.server.schema.SchemaDeployer
import com.variant.server.util.OnceAssignable

/**
 * The Main class.
 * @author Igor
 */
object VariantServer extends App with LazyLogging {

   private val productName = "Variant AIM Server release " + getClass.getPackage.getImplementationVersion

   private val startupTimeoutSeconds = 10

   private var _schemaDeployer: SchemaDeployer = _
   private val _isUp = OnceAssignable(false)
   private val _binding = OnceAssignable[Http.ServerBinding]

   // Bootstrap external configuration.
   val config = new ConfigurationImpl(ConfigLoader.load("/variant.conf", "/variant-default.conf"));

   // set up ActorSystem and other dependencies here
   implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
   implicit val materializer: ActorMaterializer = ActorMaterializer()
   implicit val executionContext: ExecutionContext = system.dispatcher

   //val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

   // Server binding thread.
   val serverBindingTask: Future[Http.ServerBinding] = Http().bindAndHandle(Router.routs, "localhost", config.getHttpPort)

   serverBindingTask.onComplete {

      case Success(binding) =>
         sys.addShutdownHook { shutdownHook(binding) }
         _isUp <= true
         _binding <= binding
      case Failure(e) =>
         logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
         logger.error(e.getMessage, e)
         system.terminate()
   }

   // Server Init thread
   val serverInitTask = Future {
      useSchemaDeployer(SchemaDeployer.fromFileSystem())
   }

   serverInitTask.onComplete {

      case Success(binding) =>
      case Failure(e) =>
         logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
         logger.error(e.getMessage, e)
         system.terminate()
   }

   // Block Until both boot tasks complete.
   Await.result(serverBindingTask, Duration(startupTimeoutSeconds, "sec"))
   Await.result(serverInitTask, Duration(startupTimeoutSeconds, "sec"))

   if (_isUp.get) {
      logger.info(ServerMessageLocal.SERVER_BOOT_OK.asMessage(
         productName,
         _binding.get.localAddress.getPort.asInstanceOf[Object],
         TimeUtils.formatDuration(JavaDuration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()))))
   } else {
      logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
   }
   // Block until the server completes.
   Await.result(system.whenTerminated, Duration.Inf)

   // Server quit

   /**
    * Tests can override the default schema deployer to be able to deploy from a memory string.
    */
   def useSchemaDeployer(newDeployer: SchemaDeployer) = {
      _schemaDeployer = newDeployer
      _schemaDeployer.bootstrap()
   }

   def shutdownHook(binding: Http.ServerBinding) = {
      logger.info(ServerMessageLocal.SERVER_SHUTDOWN.asMessage(
         productName,
         String.valueOf(config.getHttpPort),
         TimeUtils.formatDuration(JavaDuration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()))))
   }

}
