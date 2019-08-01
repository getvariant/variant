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
import akka.http.scaladsl.model.headers.ProductVersion
import akka.http.scaladsl.server.RequestContext

/**
 * HTTP request router.
 */
object Router {

   def apply(implicit server: VariantServer) = new Router

   private[Router] val exceptionHandler = CustomExceptionHandler()
   private[Router] val rejectionHandler = CustomRejectionHandler()
}

class Router(implicit server: VariantServer) extends LazyLogging {

   import Router._

   /**
    *  The routes served by our server.
    */
   def routes(implicit system: ActorSystem): Route = {

      // Override the default Server header.
      val akkaHttp = "akka-http/" + server.actorSystem.settings.config.getString("akka.http.version")
      val variant = "variant/" + server.productVersion._2
      respondWithHeaders(Server(s"${variant} - ${akkaHttp}")) {
         handleExceptions(exceptionHandler) {
            handleRejections(rejectionHandler) {

               concat(

                  // GET / - Health page
                  pathEndOrSingleSlash { RootRoute.root },

                  // GET /schema/:name
                  // Pings a schema so that the client can create a connection.
                  pathPrefix("schema") {
                     get {
                        path(Segment) { name => implicit ctx => ctx.complete(SchemaRoute.get(name)) }
                     }
                  },
                  pathPrefix("session" ~ !"-attr") {
                     concat(
                        // GET /session/:schema/:sid
                        // Get an existing session or send session expired error.
                        get {
                           path(Segment / Segment) { (schema, sid) => implicit ctx => ctx.complete(SessionRoute.get(schema, sid))
                           }
                        },
                        // POST /session/:schema/:sid
                        // Get an existing session or create a new one (with a different ID) if expired.
                        post {
                           path(Segment / Segment) { (schema, sid) =>
                              entity(as[String]) { body => implicit ctx =>
                                 ctx.complete(SessionRoute.getOrCreate(schema, sid, body))
                              }
                           }
                        } /*,
                        // Save an existing session. *** DO NOT USE. ***
                        // Only tests are allowed to use this in order to create a known session state.
                        put {
                           path(Segment / Segment) { (schema, sid) =>
                              entity(as[String]) { body => implicit ctx =>
                                 ctx.complete(SessionRoute.save(schema, sid, body))
                              }
                           }
                        }
                        *
                        */
                     )
                  },
                  pathPrefix("session-attr") {
                     entity(as[String]) { body =>
                        concat(
                           // GET /session/:schema/:sid - get an existing session or send session expired error.
                           put {
                              path(Segment / Segment) { (schema, sid) => implicit ctx =>
                                 ctx.complete(SessionRoute.putAttributes(schema, sid, body))
                              }
                           },
                           // Get an existing session or create a new one (with a different ID) if expired.
                           delete {
                              path(Segment / Segment) { (schema, sid) => implicit ctx =>
                                 ctx.complete(SessionRoute.deleteAttributes(schema, sid, body))
                              }
                           })
                     }
                  })
            }
         }
      }
   }

}
