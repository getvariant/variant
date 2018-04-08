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
 */


abstract class VariantActionAbstract
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
   extends ActionBuilderImpl(parser) with Results {
  
      private[this] val logger = Logger(this.getClass)

      protected def preBlock[A](request: Request[A]): Unit
      
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

/**
 * An unconnected action. preBlock does nothing.
 */
class UnconnectedAction @Inject() 
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
extends VariantActionAbstract (parser) (ec) {
   
   override def preBlock[A](request: Request[A]) = {  }
 }

/**
 * A connected action. preBlock fetches the connection.
 */
class ConnectedAction @Inject() 
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
extends VariantActionAbstract (parser) (ec) {

   private[this] var _conn: Connection = _ 
    
   def connection = _conn
         
   override def preBlock[A](request: Request[A]) = {      
      
      _conn = request.headers.get(HTTP_HEADER_CONNID) match {
         case Some(cid) => VariantServer.instance.connectionStore.getOrBust(cid)
         case None => throw new ServerException.Remote(ServerError.ConnectionIdMissing)
      }
   }
 }

/*
case class VariantActionAbstract (action: Action[AnyContent]) extends Action[AnyContent] with Results {

   private[this] val logger = Logger(this.getClass)
   
   def preBlock[A]: (Request[A]) => Unit
   
   def apply(request: Request[AnyContent]): Future[Result] = {

      val start = System.currentTimeMillis
      val req = request.method + " " + request.path      
      
      var future: Future[Result] = null
      
      if (VariantServer.instance.isUp) {
         // Delegate to the actual action
         try {
            preBlock(request)
            future = action(request)
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

  override def parser = action.parser
  override def executionContext = action.executionContext
}
*/

/*
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
