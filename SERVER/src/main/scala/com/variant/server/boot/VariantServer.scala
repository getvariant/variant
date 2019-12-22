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
import com.variant.share.util.TimeUtils
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
import play.api.libs.json._
import play.api.libs.json.Json.JsValueWrapper
import akka.actor.Actor
import com.variant.server.trace.FlusherRouter
import com.variant.server.trace.EventBufferCache
import java.util.concurrent.TimeoutException
import com.variant.server.build.BuildInfo

/**
 * The Main class.
 * @author Igor
 */
trait VariantServer {

   val config: Configuration

   val schemata: Schemata

   val ssnStore: SessionStore

   val actorSystem: ActorSystem

   val eventBufferCache: EventBufferCache

   val bootExceptions = mutable.ArrayBuffer[ServerException]()

   def isUp: Boolean

   /**
    * Shutdown server synchronously.
    */
   def shutdown(): Unit

   def uptime: Duration = Duration(java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime(), "millisecond")
}

/**
 * Server factories
 */
object VariantServer {

   val name = BuildInfo.product
   val version = BuildInfo.version
   
   class Builder {

      // TODO: these should only be accessible from VariantServerImpl,
      // but I don't know how to do that yet...
      var overrides: Map[String, _] = Map.empty
      var deletions: Seq[String] = Seq.empty
      var isHeadless: Boolean = false

      /**
       * Headless server has all the backend, but does not
       * attach to and listen on network port.
       */
      def headless(): Builder = {
         isHeadless = true
         this
      }

      def withConfiguration(overrides: Map[String, _]): Builder = {
         this.overrides ++= overrides;
         this
      }

      def withoutConfiguration(deletions: Seq[String]): Builder = {
         this.deletions ++= deletions
         this
      }

      def build(): VariantServer = {

         // Create actor system outside of the constructor so we can catch any uncaught exceptions here
         // and have the opportunity to terminate actor system.

         implicit val actorSystem: ActorSystem = ActorSystem("VariantServer")

         try {
            new VariantServerImpl(this)
         } catch {
            case t: Throwable =>
               actorSystem.terminate()
               throw t
         }
      }

      /**
       * Good for debugging.
       */
      override def toString = {
         Json.prettyPrint(
            Json.obj(
               "overrides" ->
                  Json.obj(overrides.map {
                     case (k, v) =>
                        val result: (String, JsValueWrapper) = k -> JsString(if (v == null) "null" else v.toString)
                        result
                  }.toSeq: _*),
               "deletions" -> deletions,
               "isHeadless" -> isHeadless))
      }

   }

   def builder = new Builder

}

/**
 * Concrete implementation of VariantServer with a private constructor.
 * The headless option is used by the tests, which are not interested in binding to the port.
 */
class VariantServerImpl(builder: VariantServer.Builder)(override implicit val actorSystem: ActorSystem) extends VariantServer with LazyLogging {

   import VariantServer._

   logger.debug("Building server from builder:\n" + builder)

   private val startupTimeoutSeconds = 10

   private[this] var _schemaDeployer: SchemaDeployer = _
   private[this] var binding: Option[Http.ServerBinding] = None
   private[this] var _eventBufferCache: EventBufferCache = _

   //
   // Attempt to load the external configuration first. Let it fail if a problem,
   // since there's no future before a config.
   override val config: ConfigurationImpl = try {
      val external = ConfigLoader.load("/variant.conf", "/prod/variant-default.conf")
      // Apply overrides.
      var withOverrides = ConfigFactory.parseMap(builder.overrides.asJava).withFallback(external)
      builder.deletions.foreach { key => withOverrides = withOverrides.withoutPath(key) }
      new ConfigurationImpl(withOverrides)
   } catch {
      // Give up. We can't do without a good configuration.
      case e: ServerException => {
         // Don't print stack trace in the server log if expected exception:
         // hopefully, the error message is enough to debug the problem.
         logger.error(e.getMessage)
         throw e
      }
      case t: Throwable => {
         // Badness. Print error stack to the log.
         logger.error(t.getMessage, t)
         throw t
      }
   }

   private implicit val materializer: ActorMaterializer = ActorMaterializer()
   private implicit val executionContext: ExecutionContext = actorSystem.dispatcher

   override lazy val schemata: Schemata = _schemaDeployer.schemata
   override val ssnStore = new SessionStore(this)
   override def isUp = (builder.isHeadless || binding.isDefined) && bootExceptions.size == 0
   override lazy val eventBufferCache = _eventBufferCache

   // If debug, echo all config params.
   logger.whenDebugEnabled {
      config.entrySet.toArray
         .sortWith(_.getKey < _.getKey)
         .foreach { e => logger.debug(s"${e.getKey} → ${e.getValue}") }
   }

   // To speedup server startup, we split it between two parallel threads.
   // Startup Thread 1: server binding.
   // TODO: I haven't found a way to pass `this` implicitly other than creating an implicit val
   implicit val _this = this

   // Thread 1: Bind to TCP port, unless headless is true.
   val serverBindingTask: Future[Http.ServerBinding] = {
      if (builder.isHeadless) Future.successful(null)
      else Http().bindAndHandle(new Router().routes, "localhost", config.httpPort)
   }

   // Thread 2: Server backend init.
   val serverInitTask = Future {
      _eventBufferCache = EventBufferCache(this)
      useSchemaDeployer(SchemaDeployer.fromFileSystem(this))
      VacuumActor.start(this)
   }
   // Block indefinitely for when both futures are completed...

   Try { Await.result(serverBindingTask, Duration.Inf) } match {
      case Success(b) => binding = if (builder.isHeadless) None else Some(b)
      case Failure(t) => croak(t)
   }

   Try { Await.result(serverInitTask, Duration.Inf) } match {
      case Success(_) =>
      case Failure(t) => croak(t)
   }

   if (isUp) {
      logger.info(ServerMessageLocal.SERVER_BOOT_OK.asMessage(
         s"${name} release ${version}",
         if (builder.isHeadless) "headless" else config.httpPort.toString,
         TimeUtils.formatDuration(java.time.Duration.ofMillis(uptime.toMillis))))
   } else {
      logger.error(ServerMessageLocal.SERVER_BOOT_FAILED.asMessage(s"${name} release ${version}"))
      bootExceptions.foreach(e => logger.error(e.getMessage, e))
      actorSystem.terminate()
   }

   /**
    * Synchronously shutdown the server.
    * By the time this call returns, all schemata have been undeployed and all pending
    * trace event flushed.
    */
   override def shutdown() {

      val start = System.currentTimeMillis
      logger.debug("Server shutdown sequence started")

      // No more client connections
      binding.map(_.unbind)
      binding = None

      logger.debug("Unbound from network port")

      // Undeploy all schemata. This will not drain sessions.
      schemata.undeployAll()

      logger.debug("All schemata undeployed")

      // Flush the event buffer cache.
      _eventBufferCache.shutdown()

      logger.debug("Buffer cache shutdown")

      val actorSystemTerminationFuture = actorSystem.whenTerminated.andThen {
         case Success(_) =>

            logger.info(ServerMessageLocal.SERVER_SHUTDOWN.asMessage(
               s"${name} release ${version}",
               if (builder.isHeadless) "headless" else config.httpPort.toString,
               TimeUtils.formatDuration(java.time.Duration.ofMillis(System.currentTimeMillis - start)),
               TimeUtils.formatDuration(java.time.Duration.ofMillis(uptime.toMillis))))

         case Failure(e) =>
            logger.error("Unexpected exception thrown:", e)
      }

      // Shutdown actor system and block until it's shutdown.
      actorSystem.terminate()
      try {
         Await.ready(actorSystemTerminationFuture, 10 seconds)
      }
      catch {
         case _: TimeoutException => 
            logger.error("Timed out waiting for the shutdown to complete")
         case e: Exception => 
            logger.error("Exception during server shutdown:", e)
      }
   }

   /**
    * Tests can override the default schema deployer to be able to deploy from a memory string.
    */
   def useSchemaDeployer(newDeployer: SchemaDeployer): Unit = {
      _schemaDeployer = newDeployer
      _schemaDeployer.bootstrap()
   }

   /**
    * Croak with unexpected exception.
    */
   def croak(t: Throwable) {
      t match {
         case se: ServerException => bootExceptions += se;
         case e: Throwable => bootExceptions += new ServerException("Uncaught exception: " + e.getMessage, e)
      }
   }

}

