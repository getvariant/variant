package com.variant.server.play.action

import javax.inject.Inject
import play.api.mvc._
import scala.concurrent.ExecutionContext
import com.variant.core.util.Constants._
import com.variant.server.boot.VariantServer
import com.variant.server.api.ServerException
import com.variant.core.ServerError
import com.variant.core.ConnectionStatus._
/**
 * A privileged action.
 * Connected action whose connection status must be OPEN.
 * Otherwise, throw UnknownConnection
 */
class PrivilegedAction @Inject() 
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
extends ConnectedAction (parser) (ec) {

   /**
    * Get connection from request and add it on to the request.             
    */
   override def beforeBlock[A](request: Request[A]): Request[A] = {      

      val req = super.beforeBlock(request)
      val conn = req.attrs.get(ConnKey).get
      if (conn.status != OPEN)
         throw new ServerException.Remote(ServerError.UnknownConnection, conn.id)
      req
   }
   
 }
