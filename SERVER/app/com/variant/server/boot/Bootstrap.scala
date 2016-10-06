package com.variant.server.boot

import java.time.Clock
import javax.inject._
import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future
import play.api.Configuration
import play.api.Logger
import com.variant.core.impl.VariantCore
import com.variant.core.impl.VariantComptime
import com.variant.core.VariantCorePropertyKeys.Key
import com.variant.server.ServerPropertyKeys
import org.apache.commons.lang3.time.DurationFormatUtils

/**
 * Need a trait to make DI to work.
 */
trait Bootstrap {
   def core(): VariantCore
   def config(): Configuration
}

/**
 * Instantiated once by 
 */
@Singleton
class BootstrapImpl @Inject() (
      clock: Clock,
      config: Configuration, 
      appLifecycle: ApplicationLifecycle
      ) extends Bootstrap {

   private val logger = Logger(this.getClass)
   private val start = clock.instant
   private val now = System.currentTimeMillis;
   private lazy val coreLib = CoreBoot.initCore
   
   override def core() = coreLib
   override def config() = config
   
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
				config.getString("variant.server.context").get));
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