package com.variant.server.routs

import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.example.UserRegistryActor._
import akka.util.Timeout
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.server.Directive.addByNameNullaryApply
import com.variant.server.boot.VariantServer
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.ExceptionHandler
import com.variant.server.boot.ServerExceptionRemote
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCode
import com.variant.server.boot.ServerMessageRemote
import com.variant.core.error.ServerError

/**
 * HTTP request router.
 */
object Router {

   def apply(implicit server: VariantServer) = new Router
}

class Router(implicit server: VariantServer) extends LazyLogging {
   //#user-routes-class

   // we leave these abstract, since they will be provided by the App
   // implicit def system: ActorSystem

   //lazy val log = Logging(system, classOf[Router])

   // other dependencies that UserRoutes use
   //def userRegistryActor: ActorRef

   // Required by the `ask` (?) method below
   implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

   // Our custom rejection handler.
   implicit def rejectionHandler =
      RejectionHandler.newBuilder()
         // The default exception handler does this
         .handleNotFound { complete((NotFound)) }
         .result()

   // Our custom exception handler
   implicit def exceptionHandler: ExceptionHandler =

      ExceptionHandler {

         case e: ServerExceptionRemote =>

            val msg = "Internal API error: %s".format(e.error.asMessage(e.args: _*))

            if (e.error.isInternal) logger.error(msg.toString(), e)
            else logger.debug(msg, e)

            complete(HttpResponse(StatusCodes.BadRequest, entity = ServerMessageRemote(e.error).asResponseEntity(e.args: _*)))

         case t: Throwable =>

            extractUri { uri =>

               logger.error(s"Unexpected Internal Error in [${uri}]", t);
               complete(HttpResponse(StatusCodes.BadRequest, entity = ServerMessageRemote(ServerError.InternalError).asResponseEntity(t.getMessage)))
            }
      }

   /**
    *  The routs served by our server.
    *  TODO: move ActorSystem under server.
    */
   def routs(implicit system: ActorSystem): Route =

      // Health page
      pathEndOrSingleSlash { RootRoute.root }

   // GET /connection/:schema
   pathPrefix("connection") {
      concat(
         path(Segment) { schema =>
            concat(
               get { ctx =>
                  // Connect to a schema
                  //complete(ConnectionRoute.get(ctx, schema))
                  ctx.complete("foo")
               })
         })
   }
   //#all-routes
}
