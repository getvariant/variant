package com.variant.server.boot

import java.io.InputStream
import java.io.InputStreamReader

import scala.collection.JavaConverters._

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import com.variant.share.util.IoUtils
import com.variant.share.util.Tuples.Pair

/**
 * Variant Configuration. A Typesafe Config based implementation.
 * See https://github.com/typesafehub/config for details.
 *
 *
 * @author Igor Urisman
 */
object ConfigLoader extends LazyLogging {

   private val SYSPROP_CONFIG_RESOURCE = "variant.config.resource";
   private val SYSPROP_CONFIG_FILE = "variant.config.file";

   private val FORMAT_EXCEPTION = "Unable to open [%s]";
   private val FORMAT_RESOURCE_FOUND = "Found %s config resource [%s] as [%s]";
   private val FORMAT_RESOURCE_NOT_FOUND = "Could not find %s config resource [%s]";
   private val FORMAT_FILE_FOUND = "Found config file [%s]";

   /**
    * Load the configuration from the runtime environment. Both client and
    * server follow the same semantics:
    * 1. If /variant.config is present on the classpath, it is merged with the default to produce interim.
    * 2. If -Dvariant.config.resource given, it must exist and is merged with interim.
    * 3. if -Dvariant.config.file is given, it must exist and is merged with interim.
    * 4. It's a user error to provide both 2. and 3.
    *
    * @return an object of type {@link Config}.
    */
   def load(resourceName: String, defaultResourceName: String): Config = {

      // Check that we weren't given two overrides.
      val overrideResourceName = System.getProperty(SYSPROP_CONFIG_RESOURCE);
      val overrideFileName = System.getProperty(SYSPROP_CONFIG_FILE);

      if (overrideResourceName != null && overrideFileName != null) {
         throw ServerExceptionLocal(ServerMessageLocal.CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN)
      }

      // 0. Default config must exist.
      val defaultStream: Pair[InputStream, String] = try {
         IoUtils.openResourceAsStream(defaultResourceName)
      } catch {
         case t: Throwable =>
            throw ServerExceptionInternal(String.format(FORMAT_EXCEPTION, defaultResourceName), t)
      }

      if (defaultStream == null) {
         throw ServerExceptionInternal((String.format(FORMAT_RESOURCE_NOT_FOUND, "default", defaultResourceName)))
      }

      logger.debug(String.format(FORMAT_RESOURCE_FOUND, "default", defaultResourceName, defaultStream._2));

      val defaultConfig = ConfigFactory.parseReader(new InputStreamReader(defaultStream._1));

      // 1. /variant.conf, if exists.
      var interimConfig = defaultConfig;
      try {
         val res = IoUtils.openResourceAsStream(resourceName);
         if (res == null) {
            logger.info(String.format(FORMAT_RESOURCE_NOT_FOUND, "", resourceName))
         } else {
            logger.info(String.format(FORMAT_RESOURCE_FOUND, "", resourceName, res._2))
            interimConfig = ConfigFactory.parseReader(new InputStreamReader(res._1)).withFallback(defaultConfig)
         }
      } catch {
         case t: Exception =>
            throw new ServerExceptionInternal(String.format(FORMAT_EXCEPTION, resourceName), t);
      }

      var result = interimConfig;

      // 2. Override file may have been given as resource.
      if (overrideResourceName != null) {
         try {
            val res = IoUtils.openResourceAsStream(overrideResourceName);
            if (res == null) {
               throw ServerExceptionLocal(ServerMessageLocal.CONFIG_RESOURCE_NOT_FOUND, overrideResourceName);
            } else {
               logger.info(String.format(FORMAT_RESOURCE_FOUND, "", overrideResourceName, res._2()));
               result = ConfigFactory.parseReader(new InputStreamReader(res._1())).withFallback(interimConfig);
            }
         } catch {
            case _: Throwable =>
               throw ServerExceptionLocal(ServerMessageLocal.CONFIG_RESOURCE_NOT_FOUND, overrideResourceName);
         }
      } // 3. Override file may have been given as file
      else if (overrideFileName != null) {

         val is: InputStream = try {
            IoUtils.openFileAsStream(overrideFileName);
         } catch {
            case _: Throwable =>
               throw ServerExceptionLocal(ServerMessageLocal.CONFIG_FILE_NOT_FOUND, overrideFileName);
         }

         if (is == null) {
            throw new ServerExceptionLocal(ServerMessageLocal.CONFIG_FILE_NOT_FOUND, overrideFileName);
         }

         logger.info(String.format(FORMAT_FILE_FOUND, overrideFileName));
         result = ConfigFactory.parseReader(new InputStreamReader(is)).withFallback(interimConfig);

      }

      // This seems a hack but I can't find a way to do it cleanly.
      // Override anything in the result with system properties, if set.
      val sysPropsOverride = System.getProperties().asScala.filter {
         case (k, v) =>
            result.hasPath(k)
      }

      return ConfigFactory.parseMap(sysPropsOverride.asJava).withFallback(result).resolve();
   }

}
