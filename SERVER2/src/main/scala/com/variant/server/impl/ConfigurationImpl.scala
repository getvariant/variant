package com.variant.server.impl;

import scala.collection.JavaConverters._
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueType
import com.variant.core.util.immutable.ImmutableMap
import com.variant.server.api.Configuration
import com.variant.server.boot.ServerMessageLocal
import com.variant.server.boot.ServerExceptionLocal
import com.variant.server.util.JavaImplicits._

/**
 * Effective server configuration.
 */
class ConfigurationImpl(config: Config) extends Configuration with ConfigKeys {

   /**
    * String getter. Throws exception if not set or wrong type.
    * @param key
    * @return
    */
   private[this] def getString(key: String): String = {
      try {
         config.getString(key)
      } catch {

         case e: ConfigException.Missing =>
            throw new ServerExceptionLocal(ServerMessageLocal.CONFIG_PROPERTY_NOT_SET, key);

         case e: ConfigException.WrongType =>
            throw new ServerExceptionLocal(
               ServerMessageLocal.CONFIG_PROPERTY_WRONG_TYPE, key,
               ConfigValueType.STRING,
               config.getValue(key).valueType());
      }
   }

   /**
    * Get Optional raw config value.
    * @param key
    * @return
    */
   private[this] def getConfigValue(key: String): Option[ConfigValue] = {
      try {
         Some(config.getValue(key))
      } catch {
         case e: ConfigException.Missing => None

         case e: ConfigException.WrongType =>
            throw new ServerExceptionLocal(
               ServerMessageLocal.CONFIG_PROPERTY_WRONG_TYPE, key,
               ConfigValueType.OBJECT,
               config.getValue(key).valueType());
      }
   }

   /**
    * Int getter.
    * @param key
    * @return
    */
   private[this] def getInt(key: String): Integer = {
      try {
         config.getInt(key)
      } catch {
         case e: ConfigException.Missing =>
            throw new ServerExceptionLocal(ServerMessageLocal.CONFIG_PROPERTY_NOT_SET, key);

         case e: ConfigException.WrongType =>
            throw new ServerExceptionLocal(
               ServerMessageLocal.CONFIG_PROPERTY_WRONG_TYPE, key,
               ConfigValueType.NUMBER,
               config.getValue(key).valueType());
      }
   }

   /*--------------------------------------------------------------------------------*/
   /*                               PUBLIC INTERFACE                                 */
   /*--------------------------------------------------------------------------------*/

   override def httpPort: Int = getInt(HTTP_PORT)

   override def schemataDir: String = getString(SCHEMATA_DIR)

   override def sessionTimeout: Int = getInt(SESSION_TIMEOUT)

   override def sessionVacuumInterval: Int = getInt(SESSION_VACUUM_INTERVAL)

   override def defaultEventFlusherClassName: String = getString(EVENT_FLUSHER_CLASS_NAME)

   override def defaultEventFlusherClassInit: java.util.Optional[String] =
      getConfigValue(EVENT_FLUSHER_CLASS_INIT).map(v => v.render(ConfigRenderOptions.concise()))

   override def eventWriterBufferSize: Int = getInt(EVENT_WRITER_BUFFER_SIZE)

   override def eventWriterMaxDelay: Int = getInt(EVENT_WRITER_MAX_DELAY)

   /*--------------------------------------------------------------------------------*/
   /*                                  PUBLIC EXT                                    */
   /*--------------------------------------------------------------------------------*/

   def entrySet = config.entrySet()
}

