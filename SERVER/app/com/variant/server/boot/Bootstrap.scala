package com.variant.server.boot

import java.time.Clock

import scala.concurrent.Future

import org.apache.commons.lang3.time.DurationFormatUtils

import com.variant.core.impl.VariantComptime
import com.variant.core.impl.VariantCore
import com.variant.server.event.EventWriter

import javax.inject.Inject
import javax.inject.Singleton
import play.api.Configuration
import play.api.Logger
import play.api.inject.ApplicationLifecycle

/**
 * Need a trait to make DI to work.
 */
trait Bootstrap {
   def core(): VariantCore
   def config(): Configuration
   val eventWriter: EventWriter
}

/**
 * Instantiated once by 
 */
@Singleton
class BootstrapImpl @Inject() (
      clock: Clock,
      configuration: Configuration, 
      appLifecycle: ApplicationLifecycle
      ) extends Bootstrap {

   private val logger = Logger(this.getClass)
   private val start = clock.instant
   private val now = System.currentTimeMillis;
   private val variantConfig = new VariantConfig(configuration)
   
   private lazy val coreImpl = {
      val core = new VariantCore();
		core.getComptime().registerComponent(VariantComptime.Component.SERVER, "0.6.3")
		core
	}

   override val eventWriter = new EventWriter(coreImpl, variantConfig)
   
   override def core() = coreImpl
   override def config() = configuration
//   override val eventWriter() = eventWriterImpl
   
   /**
    *  Boot code goes here.
    */
   def boot() {
      // This code is called when the application starts.
      val comptime = core.getComptime
      logger.info(String.format(
				"%s Release %s © 2015-16 getvariant.com. Bootstrapped in %s. Listening on %s/", 
				comptime.getComponent(),
				comptime.getComponentVersion(),
				DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS"),
				config.getString("play.http.context").get));
   }

   /**
    *  Shutdown code
    */
   def shutdown() {
      
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