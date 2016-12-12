package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.session.SessionStore
import play.api.Logger
import com.variant.server.UserError
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerSchema
import com.variant.server.Connection
import com.variant.server.session.ConnectionStore

//@Singleton -- Is this for non-shared state controllers?
class ConnectionController @Inject() (store: ConnectionStore) extends Controller  {
   
      private val logger = Logger(this.getClass)	

   /**
    * GET a new connection to a schema.
    * test with:
curl -v -X GET http://localhost:9000/variant/session/SID
    */
   def get(name: String) = VariantAction {

         var result: Option[ServerSchema] = None
         VariantServer.server.schema.foreach {s => if (s.getName().equals(name)) result = Some(s)}
         result match {
            case Some(schema) => {
               logger.debug("Schema [%s] found".format(name))
               val conn = Connection(schema)
               
               if (store.put(conn)) {
                  Ok(conn.asJson)
               }
               else UserError.errors(UserError.TooManyConnections).asResult()
            }
            case None => {
               logger.debug("Schema [%s] not found".format(name))
               NotFound
            }
         }
   }
 
   def post(id: String) = VariantAction {
      NotImplemented
   }
 
   def delete(id: String) = VariantAction {
      NotImplemented
   }
}
