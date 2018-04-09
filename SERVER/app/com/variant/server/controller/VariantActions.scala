package com.variant.server.controller

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import com.variant.core.ServerError
import com.variant.core.util.TimeUtils
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorRemote
import play.api.Logger
import play.api.mvc._
import com.variant.server.boot.VariantServer
import com.variant.core.util.Constants._
import com.variant.core.ConnectionStatus._
import javax.inject.Inject
import com.variant.server.conn.Connection
import com.variant.server.conn.ConnectionStore

/**
 * Common actions logic chains to concrete action.
 * All concrete actions must extend this.
 *  
 * @author Igor
 */


abstract class VariantActionAbstract
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
   extends ActionBuilderImpl(parser) with Results {
  
   private[this] val logger = Logger(this.getClass)

   // Subclasses can inject code to be run before and after the action block.
   protected def beforeBlock[A](request: Request[A]): Unit = {}

   protected def afterBlock[A](request: Request[A]): Unit = {}

   // Subclasses will add headers here.
   protected val headers = mutable.ListBuffer[(String,String)]()

   override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
   
      val start = System.currentTimeMillis
      val req = request.method + " " + request.path      
            
      var future: Future[Result] = null
      
      if (VariantServer.instance.isUp) {
         // Delegate to the actual action
         try {
            beforeBlock(request)
            future = block(request)
            afterBlock(request)
         }
         catch {
            case sre: ServerException.Remote =>
               val result = ServerErrorRemote(sre.error).asResult(sre.args:_*)
               future = Future.successful(result)
            case t: Throwable => 
               logger.error("Unexpected Internal Error in [%s]".format(req), t);
               future = Future.successful(ServerErrorRemote(ServerError.InternalError).asResult(t.getMessage))            
         }
         
         logger.trace("Request [%s] completed in %s".format(req, TimeUtils.formatDuration(System.currentTimeMillis - start)))

         // Attach headers, if any.
         future.map { res => res.withHeaders(headers:_*) }
      }
      else {
         // The server has fatal errors and cannot service requests.
         Future.successful(ServiceUnavailable)
      }
   }
}

/**
 * A disconnected action:
 * Starts and ends without a connection.
 */
class DisconnectedAction @Inject() 
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
extends VariantActionAbstract (parser) (ec) {}
            

/**
 * A connected action.
 * Starts and ends with a connection.
 */
class ConnectedAction @Inject() 
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
extends VariantActionAbstract (parser) (ec) {

   private[this] var _conn: Connection = _ 
    
   def connection = _conn
         
   override def beforeBlock[A](request: Request[A]) = {      
      
      _conn = request.headers.get(HTTP_HEADER_CONNID) match {
         case Some(cid) => VariantServer.instance.connectionStore.getOrBust(cid)
         case None => throw new ServerException.Remote(ServerError.ConnectionIdMissing)
      }
      
   }
   
   override def afterBlock[A](request: Request[A]) = {      
            
      headers += (HTTP_HEADER_CONN_STATUS -> _conn.status.toString())
   }

 }

