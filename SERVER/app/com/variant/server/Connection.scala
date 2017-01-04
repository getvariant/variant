package com.variant.server

import com.variant.server.schema.ServerSchema
import com.variant.core.util.VariantStringUtils
import play.api.libs.json._
import com.variant.server.boot.VariantServer

/**
 * Represents client connection
 *
 * @author Igor Urisman
 * @since 0.7
 */
object Connection {
   
   // Random used to gen random IDs.
   val random = new java.util.Random(System.currentTimeMillis())
   
   def apply(schema: ServerSchema) = new Connection(schema)
}

/**
 * 
 */
class Connection(val schema: ServerSchema) {
  
   import Connection._
   
   val id = VariantStringUtils.random64BitString(random)
   
   val timestamp = System.currentTimeMillis()
   
   /**
    * Serialize as JSON. Schema source is shipped over as a string
    * to be parsed by client.
    */
   def asJson = {
      JsObject(Seq(
         "id" -> JsString(id),
         "ssnto" -> JsNumber(VariantServer.server.config.getInt(ConfigKeys.SESSION_TIMEOUT)),
         "ts" -> JsNumber(timestamp),
         "schema" -> JsString(schema.source)
      )).toString  
   }
}