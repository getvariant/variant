package com.variant.server.controller

import scala.collection.JavaConversions._
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
 *
abstract class AbstractVariantAction @Inject()
      (parser: BodyParsers.Default)
      (implicit ec: ExecutionContext,
       connStore: ConnectionStore) 
      extends ActionBuilderImpl(parser) with Results {
      
   private[this] val logger = Logger(this.getClass)
   
   def connection: Connection
   def preBlock[A]: (Request[A]) => Unit
   
   /**
    * Play's wrapper around the code in the concrete action. 
    */
   override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {

      
      val start = System.currentTimeMillis
      val req = request.method + " " + request.path      
      
      var future: Future[Result] = null
      
      if (VariantServer.instance.isUp) {
         // Delegate to the actual action
         try {
            preBlock(request)
            future = block(request)
         }
         catch {
            case sre: ServerException.Remote =>
               val result = ServerErrorRemote(sre.error).asResult(sre.args:_*)
                  .withHeaders(sre.getHeaders().entrySet().map{e => (e.getKey, e.getValue)}.toArray:_*)
               future = Future.successful(result)
            case t: Throwable => 
               logger.error("Unexpected Internal Error in [%s]".format(req), t);
               future = Future.successful(ServerErrorRemote(ServerError.InternalError).asResult(t.getMessage))            
         }
         
         logger.trace("Request [%s] completed in %s".format(req, TimeUtils.formatDuration(System.currentTimeMillis - start)))
         future
      }
      else {
         // The server has fatal errors and cannot service requests.
         Future.successful(ServiceUnavailable)
      }
   }
}
*/
/**
 * 
 *
class ConnectedAction @Inject()
      (parser: BodyParsers.Default)
      (implicit ec: ExecutionContext,
       connStore: ConnectionStore) 
      extends AbstractVariantAction(parser, ec, connStore) {

   private[this] var _conn = connStore.getOrBust(getConnIdOrBust(req))

         def connection = _conn

         
         override def preBlock[A]: (Request[A]) => Unit {
            val connId = req.headers.get(Constants.HTTP_HEADER_CONNID) match {
               case Some(cid) => cid
               case None => throw new ServerException.Remote(ServerError.ConnectionIdMissing)
            }

            _conn = connStore.getOrBust(connId)
         }
 }
*/