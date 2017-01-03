package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.session.SessionStore
import play.api.Logger
import com.variant.server.boot.ServerErrorApi._
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerSchema
import com.variant.server.Connection
import com.variant.server.conn.ConnectionStore

//@Singleton -- Is this for non-shared state controllers?
class ConnectionController @Inject() (store: ConnectionStore) extends Controller  {
   
   private val logger = Logger(this.getClass)	

   /**
    * GET a new connection to a schema.
    * test with:
curl -v -X POST http://localhost:9000/variant/connection/SID
    */
   def post(name: String) = VariantAction {
         var result: Option[ServerSchema] = None
         VariantServer.server.schema.foreach {s => if (s.getName().equals(name)) result = Some(s)}
         result match {
            case Some(schema) => {
               logger.debug("Schema [%s] found".format(name))
               val conn = Connection(schema)
               
               if (store.put(conn)) {
                  logger.info("Opened connection [%s] to chema [%s]".format(conn.id, name))
                  Ok(conn.asJson)
               }
               else {
                  logger.info("Unable to open connection to chema [%s]: connection table is full".format(name))
                  TooManyConnections.asResult()
               }
            }
            case None => {
               logger.debug("Schema [%s] not found".format(name))
               UnknownSchema.asResult(name)
            }
         }
   }
  
   /**
    * Close a connection.
    * test with:
curl -v -X DELETE http://localhost:9000/variant/connection/SID
    */
   def delete(id: String) = VariantAction {
      val conn = store.remove(id)
      if (conn.isDefined) {
         logger.info("Closed connection [%s] to schema [%s]".format(id, conn.get.schema.getName()))
         Ok
      }
      else {
         logger.debug("Unable to close connection: schema ID [%s] does not exist".format(id)) 
         UnknownConnection.asResult(id)
      }
   }
}
