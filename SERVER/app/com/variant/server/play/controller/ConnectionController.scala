package com.variant.server.play.controller

import com.variant.core.ServerError._
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.conn.Connection
import com.variant.server.conn.ConnectionStore
import com.variant.server.conn.SessionStore
import javax.inject.Inject
import play.api.Logger
import com.variant.server.api.ServerException
import com.variant.core.util.Constants._
import com.variant.core.ConnectionStatus._
import play.api.mvc.ControllerComponents
import com.variant.server.play.action.ConnectedAction
import com.variant.server.play.action.DisconnectedAction
import com.variant.server.boot.VariantServer

class ConnectionController @Inject() (
      val connectedAction: ConnectedAction,
      val disconnectedAction: DisconnectedAction,
      val cc: ControllerComponents,
      val server: VariantServer
      ) extends VariantController(cc, server)  {
   
   private val logger = Logger(this.getClass)	

   /**
    * POST
    * Open a new connection to a schema by name.
    * The only call that does not have the connection ID header,
    * because it hasn't yet been generated by this call.
    */
   def open(name: String) = disconnectedAction { req =>
            
      server.schemata.get(name) match {
        
         case Some(schema) => {
           
            logger.debug("Schema [%s] found".format(name))
            val conn = Connection(schema)
            
            if (server.connStore.put(conn)) {
               logger.info("Opened connection [%s] to schema [%s]".format(conn.id, name))
               Ok(conn.asJson).withHeaders(HTTP_HEADER_CONN_STATUS -> OPEN.toString())
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
    * Close an existing connection, as requested by client.
    */
   def close() = connectedAction { req =>

      val conn = req.attrs.get(connectedAction.ConnKey).get
      server.connStore.closeOrBust(conn.id)
      logger.info("Closed connection [%s] to schema [%s]".format(conn.id, conn.schema.getName))
      Ok
   }
   
   /*******************************\
    * GET
    * Check-alive ping. NOT CURRENTLY USED.
    * If connection has been closed by the server return
    *   Status: 400
    * Content: 702 Unknown Connection
    * If connection is alive, no content is returned:
    *   Status: 200
    *   Content: none
    *
   def ping(cid: String) = variantAction {
      //val conn = connStore.getOrBust(cid)
      //logger.trace(s"Connection [${conn.id}] found")
      NotImplemented
   }
   ********************************/
}
