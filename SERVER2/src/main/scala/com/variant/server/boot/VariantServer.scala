package com.variant.server.boot

import com.variant.server.schema.Schemata
import scala.concurrent.Future
import scala.collection.mutable
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
import scala.util.Try
import com.variant.server.api.ServerException

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

   val bootExceptions = mutable.ArrayBuffer[ServerException]()

   /**
    * Server uptime == JVM uptime.
    */
   def uptime: java.time.Duration = java.time.Duration.ofMillis(java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime())
}

/**
 * Concrete implementation of VariantServer
 */
class VariantServerImpl(configOverrides: Map[String, String]) extends VariantServer with LazyLogging {

   /**
    * Nullary constructor means no config property overrides.
    */
   def this() { this(Map.empty[String, String]) }

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

   // Bootstrap external configuration first, before we can split into 2 parallel threads.
   override lazy val config = _config.get

   val _config: Option[Configuration] = Try[Configuration] {
      new ConfigurationImpl(ConfigLoader.load("/variant.conf", "/prod/variant-default.conf"));
   } match {
      case Success(config) => Some(config)
      case Failure(t) =>
         croak(t)
         None
   }

   if (_config.isDefined) {

      // To speedup server startup, we split it between two parallel threads.
      // Startup Thread 1: server binding.
      // TODO: I haven't found a way to pass `this` implicitly other than creating an implicit val
      implicit val _this = this
      val serverBindingTask: Future[Http.ServerBinding] = Http().bindAndHandle(new Router().routs, "localhost", config.getHttpPort)

      serverBindingTask.onComplete {

         case Success(binding) =>
            _isUp <= true
            _binding <= binding

         case Failure(_) =>
      }

      // Thread 2: Server init.
      val serverInitTask = Future {
         useSchemaDeployer(SchemaDeployer.fromFileSystem(this))
      }

      // When both futures have completed...
      Future.sequence(Seq(serverBindingTask, serverInitTask))
         .onComplete {
            case Success(_) =>
               if (_isUp.get) {
                  logger.info(ServerMessageLocal.SERVER_BOOT_OK.asMessage(
                     productName,
                     _binding.get.localAddress.getPort.asInstanceOf[Object],
                     TimeUtils.formatDuration(uptime)))

                  sys.addShutdownHook { farewell(_binding.get) }

               } else {
                  logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
               }
            case Failure(e) => croak(e)
         }
   }

   /**
    * Tests can override the default schema deployer to be able to deploy from a memory string.
    */
   def useSchemaDeployer(newDeployer: SchemaDeployer) = {
      _schemaDeployer = newDeployer
      _schemaDeployer.bootstrap()
   }

   /**
    * Croak with an expected error, which has been already reported.
    */
   def croak() {
      logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
      actorSystem.terminate()
   }

   /**
    * Croak with unexpected exception.
    */
   def croak(t: Throwable) {
      logger.error(t.getMessage, t)
      croak
   }

   /**
    * To be executed during JVM shutdown.
    */
   def farewell(binding: Http.ServerBinding) = {
      logger.info(ServerMessageLocal.SERVER_SHUTDOWN.asMessage(
         productName,
         String.valueOf(config.getHttpPort),
         TimeUtils.formatDuration(uptime)))
   }

}

