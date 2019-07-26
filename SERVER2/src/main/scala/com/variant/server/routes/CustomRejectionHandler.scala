package com.variant.server.routes

import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._

object CustomRejectionHandler {

   def apply() = RejectionHandler.newBuilder()
      .handle {

         // Don't send 405 when an unsupported method is used with a mapped path.
         case MethodRejection(_) => complete(NotFound)
      }
      .result()
}