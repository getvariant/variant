package com.variant.server.conn

import com.variant.server.schema.ServerSchema
import com.variant.core.util.VariantStringUtils
import play.api.libs.json._
import com.variant.server.boot.VariantServer
import com.variant.server.api.ConfigKeys
import scala.math.BigDecimal.int2bigDecimal
import scala.math.BigDecimal.long2bigDecimal
import com.variant.server.api.Session

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
   
   val id = VariantStringUtils.random64BitString(random)   
   val timestamp = System.currentTimeMillis()
   
   // Each connection has its own session store - sessions created over this connection.
   // We need to be able to cascade an action on the connection, e.g. close, to all dependent sessions.
   private val ssnStore = new SessionStore()

   /**
    * Add session
    */
   def addSession(ssn: Session) {
      ssnStore.put(ssn)
   }
   
   /**
    * Lookup session.
    */
   def getSession(sid: String): Option[Session] = ssnStore.asSession(sid)

   /**
    * Lookup session as JSON string.
    *
   def getSessionJson(sid: String): Option[String] = ssnStore.asJson(sid)
*/
   /**
    * Invoke function on each session store entry in this connection.
    */
   def destroy() {
      ssnStore.destroy()
   }
   
   def deleteIf(f: (SessionStore.Entry) => Boolean) {
      ssnStore.deleteIf(f)   
   }
   
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