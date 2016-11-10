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
import com.variant.server.schema.SchemaService

/**
 * Need a trait to make DI to work.
 */
trait VariantServer {
   def schema(): ServerSchema
   def properties(): VariantProperties
   def eventWriter(): EventWriter
}

/**
 * 
 */
object VariantServer {
   var instanceImpl: VariantServer = null
   def instance: VariantServer = instanceImpl
}

/**
 * Instantiated once by 
 */
@Singleton
class VariantServerImpl @Inject() (
      clock: Clock,
      configuration: Configuration, 
      appLifecycle: ApplicationLifecycle
      //router: Provider[Router] DE craps out
      ) extends VariantServer {

   private val logger = Logger(this.getClass)
   
   private val start = clock.instant
   private val now = System.currentTimeMillis;
      
	private[this] lazy val propertiesImpl = new ServerPropertiesImpl(configuration)
   private[this] lazy val eventWriterImpl = new EventWriter(propertiesImpl)
   private[this] var schemaImpl: Schema = null
   
   override def eventWriter = eventWriterImpl
   override def properties = propertiesImpl
   override def schema = null // need to get schema.
   
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

      val hooker = new UserHooker()
	   val schemaService = SchemaService(hooker, properties)
	   	   
		VariantServer.instanceImpl = this
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
