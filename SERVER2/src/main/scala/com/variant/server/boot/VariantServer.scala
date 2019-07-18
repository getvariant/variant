package com.variant.server.boot

import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.typesafe.scalalogging.LazyLogging
import com.variant.core.util.TimeUtils
import com.variant.server.api.Configuration
import com.variant.server.api.ServerException
import com.variant.server.impl.ConfigurationImpl
import com.variant.server.routs.Router
import com.variant.server.schema.SchemaDeployer
import com.variant.server.schema.Schemata
import com.variant.server.util.OnceAssignable

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

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

   def isUp: Boolean

   /**
    * Shutdown server synchronously.
    */
   def shutdown(): Unit

   def uptime: java.time.Duration = java.time.Duration.ofMillis(java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime())

}

/**
 * Concrete implementation of VariantServer
 */
class VariantServerImpl(configOverrides: Map[String, Object]) extends VariantServer with LazyLogging {

   /**
    * Nullary constructor means no config property overrides.
    */
   def this() { this(Map.empty[String, Object]) }

   private val startupTimeoutSeconds = 10

   private[this] var _schemaDeployer: SchemaDeployer = _
   private[this] var binding: Option[Http.ServerBinding] = None
   private[this] var initialized: Option[Boolean] = None

   //val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

   override implicit val actorSystem: ActorSystem = ActorSystem("helloAkkaHttpServer")

   // set up ActorSystem and other dependencies here
   private implicit val materializer: ActorMaterializer = ActorMaterializer()
   private implicit val executionContext: ExecutionContext = actorSystem.dispatcher

   override lazy val schemata: Schemata = _schemaDeployer.schemata
   override def isUp = binding.isDefined && initialized.getOrElse(false)

   // Bootstrap external configuration first, before we can split into 2 parallel threads.
   override lazy val config = _config.get

   val _config: Option[ConfigurationImpl] = Try[ConfigurationImpl] {
      new ConfigurationImpl(ConfigLoader.load("/variant.conf", "/prod/variant-default.conf"), configOverrides.asJava)
   } match {
      case Success(conf) => Some(conf)
      case Failure(t) =>
         croak(t)
         None
   }

   if (_config.isDefined) {

      // If debug, echo all config params.
      logger.whenDebugEnabled {
         config.asMap().asScala.map { case (k, v) => logger.debug(s"${k} -> ${v}") }
      }

      // To speedup server startup, we split it between two parallel threads.
      // Startup Thread 1: server binding.
      // TODO: I haven't found a way to pass `this` implicitly other than creating an implicit val
      implicit val _this = this
      val serverBindingTask: Future[Http.ServerBinding] = Http().bindAndHandle(new Router().routs, "localhost", config.getHttpPort)

      serverBindingTask.onComplete {
         case Success(bndng) => binding = Some(bndng)
         case Failure(e) => croak(e)
      }

      // Thread 2: Server init.
      val serverInitTask = Future {
         useSchemaDeployer(SchemaDeployer.fromFileSystem(this))
      }

      serverInitTask.onComplete {
         case Success(_) => initialized = Some(true)
         case Failure(e) => croak(e)
      }

      // When both futures have completed...
      Await.result(Future.sequence(Seq(serverBindingTask, serverInitTask)), Duration.Inf)
      if (isUp) {
         logger.info(ServerMessageLocal.SERVER_BOOT_OK.asMessage(
            productName,
            binding.get.localAddress.getPort.asInstanceOf[Object],
            TimeUtils.formatDuration(uptime)))

      } else {
         logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))
      }

   }

   actorSystem.whenTerminated.andThen {
      case Success(_) =>
         logger.info(ServerMessageLocal.SERVER_SHUTDOWN.asMessage(
            productName,
            String.valueOf(config.getHttpPort),
            TimeUtils.formatDuration(uptime)))

      case Failure(e) =>
         logger.error("Unexpected exception thrown:", e)
   }

   override def shutdown() {
      binding.get.unbind()
      binding = None
      actorSystem.terminate()
      Await.result(actorSystem.whenTerminated, 2 seconds)
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

      actorSystem.terminate()
      actorSystem.whenTerminated.andThen {
         case Success(_) =>
            logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(productName))

         case Failure(e) =>
            logger.error("Unexpected exception thrown:", e)
      }
   }

   /**
    * Croak with unexpected exception.
    */
   def croak(t: Throwable) {
      logger.error(t.getMessage, t)
      croak
   }

}

