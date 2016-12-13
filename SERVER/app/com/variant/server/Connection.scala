package com.variant.server

import com.variant.server.schema.ServerSchema
import com.variant.core.event.impl.util.VariantStringUtils
import play.api.libs.json._

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
    * Serialize as JSON. Schema source is already valid JSON. We avoid
    * extra deserialization/serialization of it by dropping it in as a
    * stirng substition.
    */
   def asJson = {
      JsObject(Seq(
         "id" -> JsString(id),
         "ts" -> JsNumber(timestamp),
         "schema" -> JsString(schema.source)
      )).toString  
   }
}