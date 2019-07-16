package com.variant.server.boot

import com.variant.server.schema.Schemata
import scala.concurrent.Future
import akka.http.scaladsl.Http
import com.variant.server.api.Configuration
import scala.util.{ Failure, Success }
import com.typesafe.scalalogging.LazyLogging
import com.variant.server.schema.SchemaDeployer
import com.variant.server.util.OnceAssignable
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext
import com.variant.server.impl.ConfigurationImpl
import com.variant.server.routs.Router
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import com.variant.core.util.TimeUtils
import java.nio.file.Paths

/**
 * The Main class.
 * @author Igor
 */
trait VariantServer {

   // TODO Need to get this from sbt
   val productName = "Variant AIM Server release 0.10.1"

   val config: Configuration

   val schemata: Schemata

   val actorSystem: ActorSystem

   /**
    * Server uptime == JVM uptime.
    */
   val uptime: java.time.Duration = java.time.Duration.ofMillis(java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime())
}

/**
 * Concrete implementation of VariantServer
 */
class VariantServerImpl extends VariantServer with LazyLogging {

   private val startupTimeoutSeconds = 10

   private var _schemaDeployer: SchemaDeployer = _
   private val _isUp = OnceAssignable(false)
   private val _binding = OnceAssignable[Http.ServerBinding]

   //val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

   override implicit val actorSystem: ActorSystem = ActorSystem("helloAkkaHttpServer")

   // set up ActorSystem and other dependencies here
   private implicit val materializer: ActorMaterializer = ActorMaterializer()
   private implicit val executionContext: ExecutionContext = actorSystem.dispatcher

   override lazy val schemata: Schemata = _schemaDeployer.schemata

   // Bootstrap external configuration.
   val config = new ConfigurationImpl(ConfigLoader.load("/variant.conf", "/prod/variant-default.conf"));

   // To speedup server startup, we split it between two parallel threads.
   // Startup Thread 1: server binding.
   // TODO: I haven't found a way to pass `this` implicitly other than creating an implicit val
   implicit val _this = this
   val serverBindingTask: Future[Http.ServerBinding] = Http().bindAndHandle(new Router().routs, "localhost", config.getHttpPort)

   serverBindingTask.onComplete {

      case Success(binding) =>
         sys.addShutdownHook { shutdownHook(binding) }
         _isUp <= true
         _binding <= binding
      case Failure(e) =>
         logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
         logger.error(e.getMessage, e)
         actorSystem.terminate()
   }

   // Thread 2: Server init.
   val serverInitTask = Future {
      useSchemaDeployer(SchemaDeployer.fromFileSystem(this))
   }

   serverInitTask.onComplete {

      case Success(binding) =>
      case Failure(e) =>
         logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
         logger.error(e.getMessage, e)
         actorSystem.terminate()
   }

   // Block Until both boot tasks complete.
   Await.result(serverBindingTask, Duration(startupTimeoutSeconds, "sec"))
   Await.result(serverInitTask, Duration(startupTimeoutSeconds, "sec"))

   if (_isUp.get) {
      logger.info(ServerMessageLocal.SERVER_BOOT_OK.asMessage(
         productName,
         _binding.get.localAddress.getPort.asInstanceOf[Object],
         TimeUtils.formatDuration(uptime)))
   } else {
      logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
   }

   /**
    * Tests can override the default schema deployer to be able to deploy from a memory string.
    */
   def useSchemaDeployer(newDeployer: SchemaDeployer) = {
      _schemaDeployer = newDeployer
      _schemaDeployer.bootstrap()
   }

   /**
    * To be executed during JVM shutdown.
    */
   def shutdownHook(binding: Http.ServerBinding) = {
      logger.info(ServerMessageLocal.SERVER_SHUTDOWN.asMessage(
         productName,
         String.valueOf(config.getHttpPort),
         TimeUtils.formatDuration(uptime)))
   }

}

