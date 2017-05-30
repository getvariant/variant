package com.variant.server.controller

import play.api.mvc._
import scala.concurrent.Future
import com.variant.server.boot.VariantServer
import play.api.Logger
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorRemote
import com.variant.core.ServerError

/**
 * Common actions logic chains to concrete action.
 * All concrete actions must extend this.
 *  
 * @author Igor
 */
object VariantAction extends ActionBuilder[Request] with Results {
      
   private val logger = Logger(this.getClass)
   
   /**
    * Play's wrapper around the code in the concrete action. 
    */
   def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {

      if (!VariantServer.server.isUp) {
         // If the server didn't come up, regurn 503
         logger.warn("Server unavailable");
         Future.successful(ServiceUnavailable)
      }
      else {
         // Delegate to the actual action
         logger.trace("Delegated to concrete action");
         try {
            block(request)
         }
         catch {
            case sre: ServerException.Remote => 
               Future.successful(ServerErrorRemote(sre.error).asResult(sre.args:_*))
            case t: Throwable => 
               logger.error("Unexpected Internal Error", t);
               Future.successful(ServerErrorRemote(ServerError.InternalError).asResult(t.getMessage))
         }
      }
   }
    
}