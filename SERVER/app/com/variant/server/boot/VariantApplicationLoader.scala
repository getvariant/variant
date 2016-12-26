package com.variant.server.boot

import play.api.ApplicationLoader
import play.api.Configuration
import play.api.inject._
import play.api.inject.guice._
import play.api.Logger

import com.typesafe.config.ConfigFactory
import com.variant.core.util.VariantConfigLoader

import java.io.InputStreamReader

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
   val config = new VariantConfigLoader("/variant.conf", "/com/variant/server/boot/variant-default.conf").load();
}

/**
 * This is only triggered in run, not in test.
 */
class VariantApplicationLoader extends GuiceApplicationLoader() {

   import VariantApplicationLoader._

   private val logger = Logger(this.getClass)
   private val extra = new Configuration(config)
   
   /**
    * Override builder() with Variant configuration.
    */
   override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
  
      logger.debug("Building Variant application in " + context.environment)
      
      initialBuilder
         .in(context.environment)
         .loadConfig(context.initialConfiguration ++ extra)
         .overrides(overrides(context): _*)
  }
}
