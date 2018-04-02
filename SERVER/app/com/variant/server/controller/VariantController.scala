package com.variant.server.controller

import play.api.Logger
import play.api.mvc.Controller
import play.api.libs.json._
import com.variant.server.api.ServerException
import com.variant.core.ServerError._
import com.variant.server.conn.ConnectionStore
import com.variant.server.conn.Connection
import com.variant.server.impl.SessionImpl
import com.variant.server.boot.VariantServer
import com.variant.server.conn.SessionStore
import play.api.mvc.Request
import play.api.mvc.AnyContent
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.schema.ServerSchema
import com.variant.core.util.Constants

/**
 * All Variant controllers inherit from this.
 */
abstract class VariantController extends Controller {

   val connStore: ConnectionStore
   val ssnStore: SessionStore
   
   /**
    * An alias for the server
    */
   protected val server = VariantServer.instance
     
   /**
    * 
    */
   protected def getConnection(req: Request[AnyContent]) = req.headers.get(Constants.HTTP_HEADER_CONNID)
   
   /**
    * 
    */
   protected def getConnectionOrBust(req: Request[AnyContent]) = getConnection(req).getOrElse {
      throw new ServerException.Internal("Missing Connection ID header")
   }
   
   
}