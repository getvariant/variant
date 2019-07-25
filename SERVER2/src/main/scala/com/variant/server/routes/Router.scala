package com.variant.server.routes

import com.typesafe.scalalogging.LazyLogging
import com.variant.server.boot.VariantServer

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers.Server
import com.variant.server.boot.ServerExceptionRemote
import akka.http.scaladsl.model.HttpResponse
import com.variant.core.error.ServerError
import akka.http.scaladsl.server.ExceptionHandler

/**
 * HTTP request router.
 */
object Router {

   def apply(implicit server: VariantServer) = new Router
}

class Router(implicit server: VariantServer) extends LazyLogging {

   // we leave these abstract, since they will be provided by the App
   // implicit def system: ActorSystem

   // Our implicit custom rejection handler.
   implicit def rejectionHandler =
      RejectionHandler.newBuilder()
         // The default exception handler does this
         .handleNotFound { complete((NotFound)) }
         .result()

   /* Our implicit custom exception handler
   val eh: ExceptionHandler = ExceptionHandler {

      case e: ServerExceptionRemote =>

         val msg = "Internal API error: %s".format(e.error.asMessage(e.args: _*))
         println("****************** 1")
         if (e.error.isInternal) logger.error(msg.toString(), e)
         else logger.debug(msg, e)

         complete(HttpResponse(StatusCodes.BadRequest, entity = ServerMessageRemote(e.error).asResponseEntity(e.args: _*)))

      case t: Throwable =>
         println("****************** 2")

         extractUri { uri =>

            logger.error(s"Unexpected Internal Error in [${uri}]", t);
            complete(HttpResponse(StatusCodes.BadRequest, entity = ServerMessageRemote(ServerError.InternalError).asResponseEntity(t.getMessage)))
         }
   }
*/
   /**
    *  The routes served by our server.
    */
   def routes(implicit system: ActorSystem): Route = {

      // Override the default Server header.
      //respondWithHeaders(Server(server.productVersion)) {
      handleExceptions(VariantExceptionHandler()) {
         concat(

            // GET / - Health page
            pathEndOrSingleSlash { RootRoute.root },

            // GET /schema/:name - pings a schema so that the client can create a connection.
            pathPrefix("schema") {
               concat(
                  path(Segment) { name =>
                     get { implicit ctx => ctx.complete(SchemaRoute.get(name)) }
                  })
            })
      }
      //}
   }

}
