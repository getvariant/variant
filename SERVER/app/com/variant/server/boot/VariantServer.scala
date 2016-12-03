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

/**
 * Need a trait to make DI to work.
 */
trait VariantServer {
   val properties: VariantProperties
   val eventWriter: EventWriter
   def schema: Option[ServerSchema]
   def hooker: UserHooker
   def installSchemaDeployer(newDeployer: SchemaDeployer): ParserResponse
   def runtime: Runtime
}

/**
 * 
 */
object VariantServer {
   private [boot] var instance: VariantServer = null
   def server = instance
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
   
	VariantServer.instance = this

	override val properties = new ServerPropertiesImpl(configuration)
   override val eventWriter = new EventWriter(properties)      
   override val hooker = new UserHooker()
   override val runtime = new Runtime(this)

	   // Default schema deployer is from file system.
   private var schemaDeployer: SchemaDeployer = null
   installSchemaDeployer(SchemaDeployer.fromFileSystem())
	   
	override def schema = schemaDeployer.schema
	/**
	 * Override the default mutator for schema deployer because we need to do some housekeeping
	 * if a new deployer is installed.
	 */
   def installSchemaDeployer (newDeployer: SchemaDeployer): ParserResponse = {
	   schemaDeployer = newDeployer
	   schemaDeployer.deploy
	}

   logger.info(
         String.format("%s release %s getvariant.com. Bootstrapped in %s. Listening on %s.",
         SbtService.name,
         SbtService.version,
			DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS"),
			configuration.getString("play.http.context").get))

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
