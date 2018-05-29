package com.variant.server.play.action

import javax.inject.Inject
import play.api.mvc._
import scala.concurrent.ExecutionContext
import com.variant.core.util.Constants._
import com.variant.server.boot.VariantServer
import com.variant.server.api.ServerException
import com.variant.core.impl.ServerError._
import com.variant.core.ConnectionStatus._
/**
 * A connected action.
 * Expects a connection ID in the request header.
 */
class ConnectedAction @Inject() 
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
extends AbstractAction (parser) (ec) {

   /**
    * Get connection from request and add it on to the request.             
    */
   override def beforeBlock[A](request: Request[A]): Request[A] = {      
      
      val conn = request.headers.get(HTTP_HEADER_CONNID) match {
         case Some(cid) => VariantServer.instance.connStore.getOrBust(cid)
         case None => throw new ServerException.Remote(ConnectionIdMissing)
      }

      if (conn.status == CLOSED_BY_CLIENT)
         throw new ServerException.Remote(UnknownConnection, conn.id)

      if (!Seq(OPEN, DRAINING).contains(conn.status))
         throw new ServerException.Internal(s"Illegal connection status [${conn.status}]")

      request.addAttr(ConnKey, conn)
   }
   
 }
