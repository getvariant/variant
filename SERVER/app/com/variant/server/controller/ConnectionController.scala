package com.variant.server.controller

import com.variant.core.ServerError.TooManyConnections
import com.variant.core.ServerError.UnknownSchema
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.conn.Connection
import com.variant.server.conn.ConnectionStore
import com.variant.server.conn.SessionStore

import javax.inject.Inject
import play.api.Logger

//@Singleton -- Is this for non-shared state controllers?
class ConnectionController @Inject() (override val connStore: ConnectionStore, override val ssnStore: SessionStore) extends VariantController  {
   
   private val logger = Logger(this.getClass)	

   /**
    * POST a new connection to a schema.
    * test with:
curl -v -X POST http://localhost:9000/variant/connection/SCHEMA-NAME
    */
   def post(name: String) = VariantAction {
     
      schema(name) match {
        
         case Some(schema) => {
           
            logger.debug("Schema [%s] found".format(name))
            val conn = Connection(schema)
            
            if (connStore.put(conn)) {
               logger.info("Opened connection [%s] to schema [%s]".format(conn.id, name))
               Ok(conn.asJson)
            }
            else {
               logger.info("Unable to open connection to schema [%s]: connection table is full".format(name))
               ServerErrorRemote(TooManyConnections).asResult()
            }
         }
         case None => {
            logger.debug("Schema [%s] not found".format(name))
            ServerErrorRemote(UnknownSchema).asResult(name)
         }
      }
   }
  
   /**
    * Close a connection.
    * test with:
curl -v -X DELETE http://localhost:9000/variant/connection/CID
    */
   def delete(cid: String) = VariantAction {
      val conn = connStore.deleteOrBust(cid)
      logger.info("Closed connection [%s] to schema [%s]".format(cid, conn.schema.getName))
      Ok
   }
}
