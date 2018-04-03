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
import com.variant.core.ServerError

/**
 * All Variant controllers inherit from this.
 */
abstract class VariantController extends Controller {

   private val logger = Logger(this.getClass)	

   val connStore: ConnectionStore
   val ssnStore: SessionStore
   
   /**
    * An alias for the server
    */
   protected val server = VariantServer.instance
     
   /**
    * Parse body as JSON.
    */
   protected def getBody(req: Request[AnyContent]) = {
      val bodyText = req.body.asText.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }      
      Json.parse(bodyText)
   }
   
   /**
    * 
    */
   protected def getConnectionId(req: Request[AnyContent]): String = {
      req.headers.get(Constants.HTTP_HEADER_CONNID) match {
         case Some(cid) => cid
         case None => throw new ServerException.Remote(ServerError.ConnectionIdMissing)
      }
   }
}