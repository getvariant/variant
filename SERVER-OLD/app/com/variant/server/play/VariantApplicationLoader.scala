package com.variant.server.play

import play.api.ApplicationLoader
import play.api.Configuration
import play.api.inject._
import play.api.inject.guice._
import play.api.Logger

import com.variant.server.boot.ConfigLoader;

/**
 * Inject Variant configuration into the application loader so that it has
 * the chance to override Play's application.conf settings.
 * This has to be configured in application.conf.
 * We need the config to also be available statically to the tests.
 */

/**
 * Statically accessible configuration logic that is accessible in any mode.
 */
object VariantApplicationLoader {
   
	// Normally, this gets loaded once, at build time.
   lazy val config = loadConfig

   // Tests that need config reloaded should use this.
   def loadConfig = {
   	ConfigLoader.load("/variant.conf", "/com/variant/server/boot/variant-default.conf");
   }
}

/**
 * This is only triggered in run, not in test.
 */
class VariantApplicationLoader extends GuiceApplicationLoader() {

   import VariantApplicationLoader._

   private[this] val logger = Logger(this.getClass)   
   
   /**
    * Override builder() with Variant configuration.
    */
   override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
  
      logger.debug("Building Variant application in " + context.environment.mode)
      
      // Variant config
      val extra = new Configuration(config)
      initialBuilder
         .in(context.environment)
         .loadConfig(context.initialConfiguration ++ extra)
         .overrides(overrides(context): _*)
   }
   
}
