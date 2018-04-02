package com.variant.server.controller

import play.api.mvc._
import scala.concurrent.Future
import com.variant.server.boot.VariantServer
import play.api.Logger
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorRemote
import com.variant.core.ServerError
import com.variant.core.util.TimeUtils
import com.variant.core.util.Constants

/**
 * Common actions logic chains to concrete action.
 * All concrete actions must extend this.
 *  
 * @author Igor
 */
object VariantAction extends ActionBuilder[Request] with Results {
      
   private[this] val logger = Logger(this.getClass)
   
   /**
    * Play's wrapper around the code in the concrete action. 
    */
   override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {

      val start = System.currentTimeMillis
      val req = request.method + " " + request.path      
      
      // Delegate to the actual action
      try {
         val future = block(request)
         logger.trace("Request [%s] completed in %s".format(req, TimeUtils.formatDuration(System.currentTimeMillis - start)))
         future
      }
      catch {
         case sre: ServerException.Remote => 
            Future.successful(ServerErrorRemote(sre.error).asResult(sre.args:_*))
         case t: Throwable => 
            logger.error("Unexpected Internal Error in [%s]".format(req), t);
            Future.successful(ServerErrorRemote(ServerError.InternalError).asResult(t.getMessage))            
      }
   }   
}