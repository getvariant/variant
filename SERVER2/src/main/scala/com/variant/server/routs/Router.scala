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

/**
 * HTTP request router.
 */
object Router {

   def apply(implicit server: VariantServer) = new Router
}

class Router(implicit server: VariantServer) {
   //#user-routes-class

   // we leave these abstract, since they will be provided by the App
   // implicit def system: ActorSystem

   //lazy val log = Logging(system, classOf[Router])

   // other dependencies that UserRoutes use
   //def userRegistryActor: ActorRef

   // Required by the `ask` (?) method below
   implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

   /**
    *  The routs served by our server.
    *  TODO: move ActorSystem under server.
    */
   def routs(implicit system: ActorSystem): Route =
      pathEndOrSingleSlash { Root.root }
   /*
    pathPrefix("users") {
      concat(
        //#users-get-delete
        pathEnd {
          concat(
            get {
              val users: Future[Users] =
                (userRegistryActor ? GetUsers).mapTo[Users]
              complete(users)
            },
            post {
              entity(as[User]) { user =>
                val userCreated: Future[ActionPerformed] =
                  (userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]
                onSuccess(userCreated) { performed =>
                  log.info("Created user [{}]: {}", user.name, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        //#users-get-post
        //#users-get-delete
        path(Segment) { name =>
          concat(
            get {
              //#retrieve-user-info
              val maybeUser: Future[Option[User]] =
                (userRegistryActor ? GetUser(name)).mapTo[Option[User]]
              rejectEmptyResponse {
                complete(maybeUser)
              }
              //#retrieve-user-info
            },
            delete {
              //#users-delete-logic
              val userDeleted: Future[ActionPerformed] =
                (userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]
              onSuccess(userDeleted) { performed =>
                log.info("Deleted user [{}]: {}", name, performed.description)
                complete((StatusCodes.OK, performed))
              }
              //#users-delete-logic
            })
        })
      //#users-get-delete
    }
  //#all-routes
   *
   */
}
