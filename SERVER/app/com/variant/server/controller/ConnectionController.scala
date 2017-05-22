package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.conn.SessionStore
import play.api.Logger
import com.variant.core.ServerError._
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerSchema
import com.variant.server.conn.Connection
import com.variant.server.conn.ConnectionStore

//@Singleton -- Is this for non-shared state controllers?
class ConnectionController @Inject() (store: ConnectionStore) extends Controller  {
   
   private val logger = Logger(this.getClass)	

   /**
    * POST a new connection to a schema.
    * test with:
curl -v -X POST http://localhost:9000/variant/connection/SCHEMA-NAME
    */
   def post(name: String) = VariantAction {
      var result: Option[ServerSchema] = None
      VariantServer.server.schema.foreach {s => if (s.getName().equals(name)) result = Some(s)}
      result match {
         case Some(schema) => {
            logger.debug("Schema [%s] found".format(name))
            val conn = Connection(schema)
            
            if (store.put(conn)) {
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
      val conn = store.remove(cid)
      if (conn.isDefined) {
         logger.info("Closed connection [%s] to schema [%s]".format(cid, conn.get.schema.getName()))
         Ok
      }
      else {
         logger.debug("Unable to close connection: schema ID [%s] does not exist".format(cid)) 
         ServerErrorRemote(UnknownConnection).asResult(cid)
      }
   }
}
