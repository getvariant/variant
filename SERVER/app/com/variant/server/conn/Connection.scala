package com.variant.server.conn

import com.variant.server.schema.ServerSchema
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.boot.VariantServer
import com.variant.server.api.ConfigKeys
import scala.math.BigDecimal.int2bigDecimal
import scala.math.BigDecimal.long2bigDecimal
import com.variant.server.api.Session
import com.variant.core.ConnectionStatus._
import play.api.Logger

/**
 * Represents client connection
 *
 * @author Igor Urisman
 * @since 0.7
 */
object Connection {
  
   def apply(schema: ServerSchema) = new Connection(schema)

   // Random used to gen random IDs.
   val random = new java.util.Random(System.currentTimeMillis())
       
}

/**
 * 
 */
class Connection(val schema: ServerSchema) {
  
   import Connection._

   private[this] val logger = Logger(this.getClass)

   private[this] var _status = OPEN
   
   val id = StringUtils.random64BitString(random)   
   val timestamp = System.currentTimeMillis()
      
   /**
    * Is this connection closed?
    */
   def status = _status

   /**
    * Two connections are parallel if they name the same schema ID.
    */
   def isParallelTo(other: Connection) = {
      other.schema.getId == this.schema.getId   
   }
   
   /**
    * Set connection to draining mode.
    */
   def drain() {
      _status = DRAINING
      logger.info(s"Draining connection ID [${id}] to schema [${schema.getName}]")
   }
   /**
    * Close connection.
    */
   def close() {
      _status = CLOSED_BY_CLIENT;
      logger.debug(s"Put connection ID [${id}] to schema [${schema.getName}] into CLOSED_BY_CLIENT mode")
   }

   def isDisposable = {
      Seq(DRAINING, CLOSED_BY_CLIENT).contains(_status) &&
      VariantServer.instance.ssnStore.sessionCount(this) == 0
   }
   
   /**
    * Serialize as JSON. Schema source is shipped over as a string
    * to be parsed by client.
    */
   def asJson = {
      JsObject(Seq(
         "id" -> JsString(id),
         "ssnto" -> JsNumber(VariantServer.instance.config.getInt(ConfigKeys.SESSION_TIMEOUT)),
         "ts" -> JsNumber(timestamp),
         "schema" -> JsObject(Seq(
               "id" -> JsString(schema.getId()),
               "src" -> JsString(schema.source)
         ))
      )).toString  
   }
}