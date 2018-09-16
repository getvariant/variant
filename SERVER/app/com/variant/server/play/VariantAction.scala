package com.variant.server.play

import play.api.mvc._
import com.variant.core.util.Constants._
import play.api.Logger
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerErrorRemote
import scala.concurrent.Future
import com.variant.server.boot.VariantServer
import com.variant.core.impl.ServerError
import com.variant.core.util.TimeUtils
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import javax.inject.Inject

/**
 * Superclass for all Variant actions.
 */
class VariantAction @Inject()
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
   extends ActionBuilderImpl(parser) with Results {
  
   private[this] val logger = Logger(this.getClass)
      
   /**
    * 
    */
   override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
   
      val start = System.currentTimeMillis
      val req = request.method + " " + request.path      
            
      var future: Future[Result] = null
      
      if (VariantServer.instance.isUp) {

         if (logger.isTraceEnabled) {
            logger.trace("Request [%s] with body:\n%s".format(req, request.body))
         }
         
         try {
            // Delegate to the concrete.
            block(request) 
         }
         catch {
            case e: ServerException.Remote  =>
      
               val msg = "Internal API error: %s".format(e.error.asMessage(e.args:_*))
               if (e.error.isInternal) logger.error(msg.toString(), e)
               else logger.debug(msg,e)
               
               val result = ServerErrorRemote(e.error).asResult(e.args:_*)
               Future.successful(result)
            case t: Throwable => 
               logger.error("Unexpected Internal Error in [%s]".format(req), t);
               Future.successful(ServerErrorRemote(ServerError.InternalError).asResult(t.getMessage))            
         }
         finally {
            if (logger.isTraceEnabled) {
               logger.trace {
                     // Probably doesn't account for a ton of play code.
                     "Request [%s] completed in %s".format(req, TimeUtils.formatDuration(System.currentTimeMillis - start)) 
               }
            }
         }
      }
      else {
         // The server has fatal errors and cannot service requests.
         Future.successful(ServiceUnavailable)
      }
   }
}
