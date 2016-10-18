import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._

/**
 * Get rid of the annoying HTML sent by the default error handler in test mode.
 */

@Singleton
class ErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]
  ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

   private val logger = Logger(this.getClass)	

   override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
      //logger.error("%s %s Threw an exception:".format(request.method, request.path), exception)
      Future.successful(InternalServerError)
  }

  override def onForbidden(request: RequestHeader, message: String) = {
    Future.successful(Forbidden(message))
  }
  
  override def onNotFound(request: RequestHeader, message: String) = {
     Future.successful(NotFound(message))
  }

  override def onBadRequest(request: RequestHeader, message: String) = {
    Future.successful(BadRequest(message))
  }
}
