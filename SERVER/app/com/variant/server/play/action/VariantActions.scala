package com.variant.server.play.action

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
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
import play.api.libs.typedmap.TypedKey

/**
 * Common actions logic chains to concrete action.
 * All concrete actions must extend this.
 *  
 * @author Igor
 */

/*
abstract class VariantActionAbstract
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
   extends ActionBuilderImpl(parser) with Results {
  
   private[this] val logger = Logger(this.getClass)
   
   // Subclasses can inject code to be run before and after the action block.
   protected def beforeBlock[A](request: Request[A]): Request[A] = {request}
   protected def afterBlock[A](request: Request[A]): Unit = {}

   // Request attribute key for Connection
   val ConnKey = TypedKey.apply[Connection]("connection")
   
   /**
    * 
    */
   override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
   
      val start = System.currentTimeMillis
      val req = request.method + " " + request.path      
            
      var future: Future[Result] = null
      var newRequest: Option[Request[A]] = None
      
      if (VariantServer.instance.isUp) {

      try {
            newRequest = Some(beforeBlock(request))

            // Delegate to the concrete action with the new request object
            // potentially containing the connection object that was looked
            // up based on the connection ID request header.
            future = block(newRequest.get) 
         }
         catch {
            case sre: ServerException.Remote =>
               val result = ServerErrorRemote(sre.error).asResult(sre.args:_*)
               future = Future.successful(result)
            case t: Throwable => 
               logger.error("Unexpected Internal Error in [%s]".format(req), t);
               future = Future.successful(ServerErrorRemote(ServerError.InternalError).asResult(t.getMessage))            
         }
         
         // Accumulate response headers
         val headers = mutable.ListBuffer[(String,String)]()
         newRequest.map { req =>
            req.attrs.get(ConnKey).map { conn =>
               headers += (HTTP_HEADER_CONN_STATUS -> conn.status.toString)
            }
         }
         
         logger.trace("Request [%s] completed in %s".format(req, TimeUtils.formatDuration(System.currentTimeMillis - start)))

         future.map(_.withHeaders(headers:_*))

      }
      else {
         // The server has fatal errors and cannot service requests.
         Future.successful(ServiceUnavailable)
      }
   }
}

class MyRequest(val conn: Option[Connection], val request: Request[AnyContent]) {}

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
 * Expects a connection ID in the request header.
 */
class ConnectedAction @Inject() 
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
extends VariantActionAbstract (parser) (ec) {

   /**
    * Get connection from request and add it on to the request.             
    */
   override def beforeBlock[A](request: Request[A]): Request[A] = {      
      
      val conn = request.headers.get(HTTP_HEADER_CONNID) match {
         case Some(cid) => VariantServer.instance.connectionStore.getOrBust(cid)
         case None => throw new ServerException.Remote(ServerError.ConnectionIdMissing)
      }
      
      request.addAttr(ConnKey, conn)
   }
   
 }
*/
