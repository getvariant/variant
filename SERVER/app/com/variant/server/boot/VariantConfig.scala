package com.variant.server.boot

import javax.inject.Inject
import play.api.Configuration
import com.variant.server.event.EventFlusherAppLogger

trait VariantConfigKey {
   val name: String
   val default: Any   
}

object VariantConfigKey {

   // Actual Keys
   lazy val EventFlusherClassName = new VariantConfigKey {
      val name = "variant.event.flusher.class.name"
      val default = classOf[EventFlusherAppLogger].getName
   }

   lazy val EventFlusherClassInit = new VariantConfigKey {
      val name = "variant.event.flusher.class.init"
      val default = "{}"
   }

   lazy val EventWriterPercentFull = new VariantConfigKey {
      val name = "event.writer.percent.full"
      val default = 50
   }

   lazy val EventWriterBufferSize = new VariantConfigKey {
      val name = "variant.event.writer.buffer.size"
      val default = 20000
   }

   lazy val EventWriterFlushMaxDelayMillis = new VariantConfigKey {
      val name = "variant.event.writer.flush.max.delay.millis"
      val default = 30000
   }

}
/**
 * Wrap Play provided applicaiton configuration in 
 * Variant specific adaptor.
 */
class VariantConfig @Inject() (playConfig: Configuration) {
  
   private def get(key: VariantConfigKey) = {
      playConfig.getString(key.name).getOrElse(key.default.toString())
   }
   
   def getString(key: VariantConfigKey) = get(key).asInstanceOf[String]
   def getInt(key: VariantConfigKey) = get(key).toInt
   def getLong(key: VariantConfigKey) = get(key).toLong
   
}
