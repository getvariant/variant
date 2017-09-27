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

/**
 * All Variant controllers inherit from this.
 */
abstract class VariantController extends Controller {

   val connStore: ConnectionStore
   val ssnStore: SessionStore
   
   /**
    * An alias for the server
    */
   val server = VariantServer.instance
   
   /**
    * Find schema by name
    */
   def schema(name:String): Option[ServerSchema] = {
      server.schemata.get(name)
   }
}