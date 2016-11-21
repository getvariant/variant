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

/**
 * Need a trait to make DI to work.
 */
trait VariantServer {
   def schema: Option[ServerSchema]
   def properties: VariantProperties
   def eventWriter: EventWriter
   val hooker: UserHooker
}

/**
 * 
 */
object VariantServer {
   private[boot] var instance: VariantServer = null
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
   
	VariantServer.instance = this

	private val logger = Logger(this.getClass)
   
   private val now = System.currentTimeMillis;
      
	private[this] lazy val propertiesImpl = new ServerPropertiesImpl(configuration)
   private[this] lazy val eventWriterImpl = new EventWriter(propertiesImpl)
   private[this] var schemaDeployer: SchemaDeployer = null
   
   override def eventWriter = eventWriterImpl
   override def properties = propertiesImpl
   override val hooker = new UserHooker() 
   override def schema = schemaDeployer.schema
   
   /**
    * One time application bootup.
    */
   def boot() {

      /* Display routers on startup
      if (logger.isDebugEnabled) {
         val routeDocs = router.get.documentation
         if (routeDocs.isEmpty) 
            throw new Exception("No routes defined!")
         else
            routeDocs.map { r =>
               println("%-10s %-50s %s".format(r._1, r._2, r._3))
            }
      } */

      logger.info(
            String.format("%s release %s getvariant.com. Bootstrapped in %s. Listening on %s.",
            SbtService.name,
            SbtService.version,
				DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS"),
				configuration.getString("play.http.context").get))

		// File System schema service.
      setSchemaDeployer(SchemaDeployerFromFS())
   }

	/**
	 * Set schema service, which can be instantiated independently.
	 */
	def setSchemaDeployer(deployer: SchemaDeployer) {
	   schemaDeployer = deployer	   
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

   boot()
}
