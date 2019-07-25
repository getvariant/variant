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
import com.variant.server.routes.Router
import com.variant.server.schema.SchemaDeployer
import com.variant.server.schema.Schemata

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import akka.http.scaladsl.model.headers.ProductVersion
import akka.http.scaladsl.settings.ServerSettings

/**
 * The Main class.
 * @author Igor
 */
trait VariantServer {

   // TODO Need to get this from sbt
   val productVersion = new ProductVersion("Variant", "0.10.1", "Variant AIM Server")

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
 * Server factories
 */
object VariantServer {

   class Builder {

      private[this] var overrides: Map[String, _] = Map.empty
      private[this] var deletions: Seq[String] = Seq.empty
      private[this] var headless: Boolean = false

      def headless(): Builder = {
         this.headless = true
         this
      }

      def withOverrides(overrides: Map[String, _]): Builder = {
         this.overrides = overrides;
         this
      }

      def withDeletions(deletions: Seq[String]): Builder = {
         this.deletions = deletions
         this
      }

      def build(): VariantServer = {
         new VariantServerImpl(headless, overrides, deletions)
      }
   }

   def builder = new Builder
}

/**
 * Concrete implementation of VariantServer with a private constructor.
 * The headless option is used by the tests, which are not interested in binding to the port.
 */
private class VariantServerImpl(headless: Boolean, overrides: Map[String, _], deletions: Seq[String]) extends VariantServer with LazyLogging {

   private val startupTimeoutSeconds = 10

   private[this] var _schemaDeployer: SchemaDeployer = _
   private[this] var binding: Option[Http.ServerBinding] = None

   //val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

   override implicit val actorSystem: ActorSystem = ActorSystem("VariantServer")

   // set up ActorSystem and other dependencies here
   private implicit val materializer: ActorMaterializer = ActorMaterializer()
   private implicit val executionContext: ExecutionContext = actorSystem.dispatcher

   override lazy val schemata: Schemata = _schemaDeployer.schemata

   override def isUp = (headless || binding.isDefined) && bootExceptions.size == 0

   // Bootstrap external configuration first, before we can split into 2 parallel threads.
   override lazy val config = _config.get

   //Attempt to load the external configuration.
   val _config: Option[ConfigurationImpl] = Try[Config] {
      ConfigLoader.load("/variant.conf", "/prod/variant-default.conf")
   } match {

      case Success(conf) =>
         // Have external config. Apply overrides.
         var finalConfig = ConfigFactory.parseMap(overrides.asJava).withFallback(conf)
         deletions.foreach { key => finalConfig = finalConfig.withoutPath(key) }
         Some(new ConfigurationImpl(finalConfig))

      case Failure(t) => croak(t); None
   }

   if (_config.isDefined) {

      // If debug, echo all config params.
      logger.whenDebugEnabled {
         config.entrySet.forEach { e => logger.debug(s"${e.getKey} → ${e.getValue}") }
      }

      // To speedup server startup, we split it between two parallel threads.
      // Startup Thread 1: server binding.
      // TODO: I haven't found a way to pass `this` implicitly other than creating an implicit val
      implicit val _this = this

      // Thread 1: Bind to TCP port, unless headless is true.
      val serverBindingTask: Future[Http.ServerBinding] = {
         if (headless) Future.successful(null)
         else Http().bindAndHandle(new Router().routes, "localhost", config.httpPort)
      }

      // Thread 2: Server backend init.
      val serverInitTask = Future {
         useSchemaDeployer(SchemaDeployer.fromFileSystem(this))
      }
      // Block indefinitely for when both futures are completed...

      Try { Await.result(serverBindingTask, Duration.Inf) } match {
         case Success(b) => binding = if (headless) None else Some(b)
         case Failure(t) => croak(t)
      }

      Try { Await.result(serverInitTask, Duration.Inf) } match {
         case Success(_) =>
         case Failure(t) => croak(t)
      }

      if (isUp) {
         logger.info(ServerMessageLocal.SERVER_BOOT_OK.asMessage(
            s"${productVersion.comment} release ${productVersion.version}",
            config.httpPort.toString,
            TimeUtils.formatDuration(uptime)))
      } else {
         logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(s"${productVersion.comment} release ${productVersion.version}"))
         bootExceptions.foreach(e => logger.error(e.getMessage, e))
         actorSystem.terminate()
      }

   }

   actorSystem.whenTerminated.andThen {
      case Success(_) =>
         logger.info(ServerMessageLocal.SERVER_SHUTDOWN.asMessage(
            s"${productVersion.comment} release ${productVersion.version}",
            config.httpPort.toString,
            TimeUtils.formatDuration(uptime)))

      case Failure(e) =>
         logger.error("Unexpected exception thrown:", e)
   }

   override def shutdown() {
      binding.map(_.unbind)
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
    * Croak with unexpected exception.
    */
   def croak(t: Throwable) {
      t match {
         case se: ServerException =>
            bootExceptions += se;
         case e: Throwable => bootExceptions += new ServerException("Uncaught exception: " + e.getMessage, e)
      }
   }

}

