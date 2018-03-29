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
class ConnectionController @Inject() (
      override val connStore: ConnectionStore, 
      override val ssnStore: SessionStore
      ) extends VariantController  {
   
   private val logger = Logger(this.getClass)	

   /**
    * POST
    * Open a new connection to a schema by name.
    */
   def open(name: String) = VariantAction {
      server.schemata.get(name) match {
        
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
    * DELETE
    * Close an existing connection.
    */
   def close(cid: String) = VariantAction {
      val conn = connStore.closeOrBust(cid)
      logger.info("Closed connection [%s] to schema [%s]".format(cid, conn.schema.getName))
      Ok
   }
   
   /**
    * GET
    * Keep-alive ping.
    * If connection has been closed by the server return
    *   Status: 400
    * Content: 702 Unknown Connection
    * If connection is alive, send list updates sessions:
    *   Status: 200
    *   Content: { "exp":[sid,sid...], "upd":[{<session1>},{<session2>}...]}
    */
   def ping(cid: String) = VariantAction {
      val conn = connStore.getOrBust(cid)
      logger.trace(s"Connection [${conn.id}] found")
      Ok
   }

}
