package com.variant.server.routes

import com.typesafe.scalalogging.LazyLogging
import com.variant.server.boot.VariantServer

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers.Server
import com.variant.server.boot.ServerExceptionRemote
import akka.http.scaladsl.model.HttpResponse
import com.variant.core.error.ServerError
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.MethodRejection

/**
 * HTTP request router.
 */
object Router {

   def apply(implicit server: VariantServer) = new Router
}

class Router(implicit server: VariantServer) extends LazyLogging {

   // we leave these abstract, since they will be provided by the App
   // implicit def system: ActorSystem

   /**
    *  The routes served by our server.
    */
   def routes(implicit system: ActorSystem): Route = {

      // Override the default Server header.
      respondWithHeaders(Server(server.productVersion)) {
         handleExceptions(CustomExceptionHandler()) {
            //handleRejections(CustomRejectionHandler()) {
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
            //}
         }
      }
   }

}
