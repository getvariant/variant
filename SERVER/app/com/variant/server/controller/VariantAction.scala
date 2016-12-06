package com.variant.server.controller

import play.api.mvc._
import scala.concurrent.Future
import com.variant.server.boot.VariantServer

/**
 * Common actions logic. All actions extends this.
 * @author Igor
 */
object VariantAction extends ActionBuilder[Request] with Results {
      
   def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {

      if (!VariantServer.server.isUp) {
         // If the server didn't come up, regurn 503
         Future.successful(ServiceUnavailable)
      }
      else {
         // Delegate to the actual action
         block(request)
      }
   }
}