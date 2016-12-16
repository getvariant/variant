package com.variant.server.boot

import play.api.ApplicationLoader
import play.api.Configuration
import play.api.inject._
import play.api.inject.guice._
import play.api.Logger
import com.typesafe.config.ConfigFactory

/**
 * Inject Variant configuration into the application loader so that it has
 * the chance to override Play's application.conf settings.
 * This has to be configured in application.conf.
 */
class VariantApplicationLoader extends GuiceApplicationLoader() {
   
   private val logger = Logger(this.getClass)
   private val defaultConfigResourceName = "/com/variant/server/boot/variant-default.conf"

   override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
  
      logger.debug("Building Variant application loader.")
      
      //val extra = Configuration("variant.schemas.dir" -> "test-schemas")
      val variantDefault = ConfigFactory.parseResources("/com/variant/server/boot/variant-default.conf")
      val variantLive = VariantConfigFactory.load().withFallback(variantDefault)
      initialBuilder
         .in(context.environment)
         .loadConfig(Configuration(variantLive) ++ context.initialConfiguration)
         .overrides(overrides(context): _*)
  }
}