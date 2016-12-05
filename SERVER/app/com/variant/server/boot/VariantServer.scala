package com.variant.server.boot

import java.time.Clock
import scala.concurrent.Future
import org.apache.commons.lang3.time.DurationFormatUtils
import com.variant.server.event.EventWriter
import javax.inject._
import javax.inject.Singleton
import play.api.Configuration
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.routing.Router
import com.variant.core.schema.Schema
import com.variant.server.schema.ServerSchema
import com.variant.core.VariantProperties
import play.api.Application
import com.variant.core.impl.UserHooker
import com.variant.server.schema.SchemaDeployerFromFS
import com.variant.server.schema.SchemaDeployer
import com.variant.server.runtime.Runtime
import com.variant.core.schema.ParserResponse
import com.variant.server.ServerErrorException
import com.variant.core.exception.RuntimeErrorException
import com.variant.core.exception.Error.Severity
import com.variant.core.exception.VariantRuntimeException

/**
 * Need a trait to make DI to work.
 */
trait VariantServer {
   
   val isUp: Boolean
   val properties: VariantProperties
   val startupErrorLog: List[ServerErrorException]
   val eventWriter: EventWriter
   def schema: Option[ServerSchema]
   def hooker: UserHooker
   def installSchemaDeployer(newDeployer: SchemaDeployer): Option[ParserResponse]
   def runtime: Runtime
}

/**
 * 
 */
object VariantServer {
   private [boot] var _instance: VariantServer = null
   def server = _instance
}

/**
 * Instantiated once by 
 */
@Singleton
class VariantServerImpl @Inject() (
      configuration: Configuration, 
      appLifecycle: ApplicationLifecycle
      //router: Provider[Router] DI craps out with circular dependency
      ) extends VariantServer {
   
	private val logger = Logger(this.getClass)
   
   private val now = System.currentTimeMillis;
   
	VariantServer._instance = this

	override val properties = new ServerPropertiesImpl(configuration)
   override val eventWriter = new EventWriter(properties)      
   override val hooker = new UserHooker()
   override val runtime = new Runtime(this)

	private var _isUp = true
	override lazy val isUp = _isUp
	
	private var _startupErrorLog = List[ServerErrorException]()
	override lazy val startupErrorLog = _startupErrorLog
	
	// Default schema deployer is from file system.
   private var _schemaDeployer: SchemaDeployer = null
   installSchemaDeployer(SchemaDeployer.fromFileSystem())
	   
	override def schema = _schemaDeployer.schema

	/**
	 * Override the default mutator for schema deployer because we need to do some housekeeping
	 * if a new deployer is installed.
	 */
   def installSchemaDeployer (newDeployer: SchemaDeployer): Option[ParserResponse] = {
	   try {
   	   _schemaDeployer = newDeployer
	      Some(_schemaDeployer.deploy)
	   }
	   catch {
	      case e: ServerErrorException => {
	         _startupErrorLog :+= e
	         None
	      }
	   }
	}

	// Flip isUp to false if we had errors.
	startupErrorLog.foreach {e => if (e.getSeverity.greaterOrEqual(Severity.ERROR)) _isUp = false}

	if (!isUp) {
		   logger.error(
            String.format("%s release %s. Failed to bootstrap due to following ERRORS:",
            SbtService.name,
            SbtService.version))
	}
	else if (!schema.isDefined) {
      logger.warn(
            String.format("%s release %s. Bootstrapped on %s in %s with WARNINGS:",
            SbtService.name,
            SbtService.version,
            configuration.getString("play.http.context").get,
   			DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")))
	}
	else {
      logger.info(
            String.format("%s release %s. Bootstrapped on %s in %s.",
            SbtService.name,
            SbtService.version,
            configuration.getString("play.http.context").get,
   			DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")))
	}
	
	// Log startup messages
	if (!isUp || !schema.isDefined) 
	   startupErrorLog.foreach {
	      e => e.getSeverity match {
	         case Severity.FATAL => {logger.error("FATAL: " + e.getMessage, e)}
	         case Severity.ERROR => {logger.error("ERROR: " + e.getMessage, e)}
	         case Severity.WARN => logger.warn(e.getMessage)
	         case _ => throw new VariantRuntimeException("Unexpected exception severity %s".format(e.getSeverity), e)
	      }
	   }
	
   /**
    * One time application shutdown.
    */
   def shutdown() {
      eventWriter.shutdown()
   }
   
   // When the application starts, register a stop hook with the
   // ApplicationLifecycle object. The code inside the stop hook will
   // be run when the application stops.
   appLifecycle.addStopHook { () =>
       shutdown()
       Future.successful(())
   }

}
