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

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    Future.successful(InternalServerError)
  }

  override def onForbidden(request: RequestHeader, message: String) = {
    Future.successful(Forbidden(message))
  }
  
  override def onNotFound(request: RequestHeader, message: String) = {
     Future.successful(NotFound(message))
  }

}
