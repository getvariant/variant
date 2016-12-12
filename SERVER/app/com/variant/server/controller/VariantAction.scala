package com.variant.server.controller

import play.api.mvc._
import scala.concurrent.Future
import com.variant.server.boot.VariantServer
import play.api.Logger

/**
 * Common actions logic chains to concrete action.
 * All concrete actions must extend this.
 *  
 * @author Igor
 */
object VariantAction extends ActionBuilder[Request] with Results {
      
   private val logger = Logger(this.getClass)
   
   def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {

      if (!VariantServer.server.isUp) {
         // If the server didn't come up, regurn 503
         logger.trace("Server unavailable");
         Future.successful(ServiceUnavailable)
      }
      else {
         // Delegate to the actual action
         logger.trace("Delegated to concrete action");
         block(request)
      }
   }
}