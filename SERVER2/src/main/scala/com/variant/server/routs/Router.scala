package com.variant.server.routs

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directive.addByNameNullaryApply
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.variant.server.boot.VariantServer
import scala.concurrent.duration._

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

   // Our implicit custom rejection handler.
   implicit def rejectionHandler =
      RejectionHandler.newBuilder()
         // The default exception handler does this
         .handleNotFound { complete((NotFound)) }
         .result()

   // Our implicit custom exception handler
   implicit def eh = VariantExceptionHandler()

   /**
    *  The routs served by our server.
    *  TODO: move ActorSystem under server.
    */
   def routs(implicit system: ActorSystem): Route =

      concat(

         // GET / - Health page
         pathEndOrSingleSlash { RootRoute.root },

         // GET /connection/:schema - pings a schema so that the client can create a connection.
         pathPrefix("connection") {
            concat(
               path(Segment) { schema =>
                  get { implicit ctx => ctx.complete(ConnectionRoute.get(schema)) }
               })
         })
   //#all-routes
}
