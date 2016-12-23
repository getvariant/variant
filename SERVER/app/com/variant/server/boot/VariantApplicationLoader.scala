package com.variant.server.boot

import play.api.ApplicationLoader
import play.api.Configuration
import play.api.inject._
import play.api.inject.guice._
import play.api.Logger

import com.typesafe.config.ConfigFactory
import com.variant.core.util.VariantConfigFactory;

import java.io.InputStreamReader

/**
 * Inject Variant configuration into the application loader so that it has
 * the chance to override Play's application.conf settings.
 * This has to be configured in application.conf.
 */

/**
 * Statically accessible configuration logic that is accessible in any mode.
 */
object VariantApplicationLoader {
   
   private val defaultConfResourceName = "/com/variant/server/boot/variant-default.conf";
   private val logger = Logger(this.getClass)
   
   def config = {
      
      val defaultStream = getClass().getResourceAsStream(defaultConfResourceName)

      if (defaultStream == null) {
         logger.warn("Could NOT find default config resource [%s]".format(defaultConfResourceName))
         VariantConfigFactory.load()
      }
      else {
         logger.debug("Found default config resource [%s]".format(defaultConfResourceName))
         val variantDefault = ConfigFactory.parseReader(new InputStreamReader(defaultStream))
         VariantConfigFactory.load().withFallback(variantDefault)     
      }
   }
}

/**
 * This is only triggered in run, not in test.
 */
class VariantApplicationLoader extends GuiceApplicationLoader() {
   

import VariantApplicationLoader._

   val extra = new Configuration(config)
   
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
