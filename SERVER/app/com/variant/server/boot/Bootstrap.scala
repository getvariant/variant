package com.variant.server.boot

import java.time.Clock
import scala.concurrent.Future
import org.apache.commons.lang3.time.DurationFormatUtils
import com.variant.core.impl.VariantComptime
import com.variant.core.impl.VariantCore
import com.variant.server.event.EventWriter
import javax.inject._
import javax.inject.Singleton
import play.api.Configuration
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.routing.Router

/**
 * Need a trait to make DI to work.
 */
trait Bootstrap {
   def core: VariantCore
   def config(): VariantConfig
   def eventWriter(): EventWriter
}

/**
 * Instantiated once by 
 */
@Singleton
class BootstrapImpl @Inject() (
      clock: Clock,
      configuration: Configuration, 
      appLifecycle: ApplicationLifecycle
      //router: Provider[Router]
      ) extends Bootstrap {

   private val logger = Logger(this.getClass)
   private val start = clock.instant
   private val now = System.currentTimeMillis;
   
   private lazy val coreImpl = {
      val core = new VariantCore();
		core.getComptime().registerComponent(VariantComptime.Component.SERVER, "0.6.3")
		core
	}
	private lazy val configImpl = new VariantConfig(configuration)
   private lazy val eventWriterImpl = new EventWriter(coreImpl, config)
   
   override def core() = coreImpl
   override def eventWriter() = eventWriterImpl
   override def config() = configImpl
   
   /**
    *  Boot code goes here.
    */
   def boot() {

	   // TODO: I keep running into a problem that routes aren't recoginzed under eclipse and I don't understand it.
	   // Fail fast if there are no routes. 
/*
      if (logger.isDebugEnabled) {
         val routeDocs = router.get.documentation
         if (routeDocs.isEmpty) 
            throw new Exception("No routes defined!")
         else
            routeDocs.map { r =>
               println("%-10s %-50s %s".format(r._1, r._2, r._3))
            }
      }
*/
      // This code is called when the application starts.
      val comptime = core.getComptime
      logger.info(String.format(
				"%s Release %s © 2015-16 getvariant.com. Bootstrapped in %s. Listening on %s", 
				comptime.getComponent(),
				comptime.getComponentVersion(),
				DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS"),
				configuration.getString("play.http.context").get));
   }

   /**
    *  Shutdown code
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

   boot();
   
}