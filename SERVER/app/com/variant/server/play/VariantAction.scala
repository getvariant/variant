package com.variant.server.play

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import com.variant.core.impl.ServerError
import com.variant.core.util.TimeUtils
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.boot.VariantServer

import javax.inject.Inject
import play.api.Logger
import play.api.mvc._
import com.variant.server.impl.ConfigKeysSecret

/**
 * Superclass for all Variant actions.
 */
object VariantAction {

  /**
   * Compute if we should be timing requests only once
   */
  private val withTiming = {
     try { VariantApplicationLoader.config.getBoolean("variant.with.timing")}
     catch { case _:Throwable => false }
   }
}

class VariantAction @Inject()
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
   extends ActionBuilderImpl(parser) with Results {
  
   import VariantAction._
   
   private[this] val logger = Logger(this.getClass)
   
   /**
    * 
    */
   override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
   
      val start = System.currentTimeMillis
      val req = request.method + " " + request.path      
                  
      if (VariantServer.instance.isUp) {

         if (logger.isTraceEnabled) {
            logger.trace("Request [%s] with body:\n%s".format(req, request.body))
         }
         
         try {
            // Delegate to the concrete action and time for performance testing 
            val start = System.currentTimeMillis()

            block(request).map { res =>
              if (withTiming) res.withHeaders("Variant-Timer" -> String.valueOf((System.currentTimeMillis - start))) 
              else res
           }
         }
         catch {
            case e: ServerExceptionRemote  =>
      
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
