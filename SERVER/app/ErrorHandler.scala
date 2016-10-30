import javax.inject._
import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._
import play.api.http.HttpErrorHandler

/**
 * Custom error handler
 */

@Singleton
class ErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]
  ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

   private val logger = Logger(this.getClass)	

   /**
    * Do not generate the silly "Action Not Found" html page.
    */
   override def onNotFound(request: RequestHeader, message: String) = {
      Future.successful(NotFound(message))
   }

}

/*
@Singleton
class ErrorHandler extends HttpErrorHandler {

   private val logger = Logger(this.getClass)
   
   def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
      //logger.debug("Returning error to client: [%d] [%s]".format(statusCode, message))
      Future.successful({
         println("******************************** client " + statusCode)
         Status(statusCode)(message)
      })
   }

   def onServerError(request: RequestHeader, exception: Throwable) = {
      //logger.error("Unhandled Server Error", exception)
      Future.successful({
         println("******************************** server " + exception.getMessage)
         InternalServerError("A server error occurred: " + exception.getMessage)
      })
   }
}
*/