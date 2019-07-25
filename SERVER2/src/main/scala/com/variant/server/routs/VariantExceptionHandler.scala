package com.variant.server.routs

import akka.http.scaladsl.server.ExceptionHandler
import com.variant.server.boot.ServerExceptionRemote
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging
import com.variant.core.error.ServerError
import com.variant.server.boot.ServerExceptionInternal
import akka.http.scaladsl.model.ContentTypes

object VariantExceptionHandler extends LazyLogging {

   def apply() = ExceptionHandler {

      // Remote user error.
      case e: ServerExceptionRemote =>

         logger.whenDebugEnabled {
            logger.debug(s"Remote server error: [${e.getMessage}]")
         }

         complete(HttpResponse(StatusCodes.BadRequest, entity = e.toResponseEntity))

      // We barfed while carrying out a remote request.
      case e: ServerExceptionInternal =>

         logger.whenDebugEnabled {
            logger.debug(s"Internal server error: [${e.getMessage}]")
         }

         complete(HttpResponse(StatusCodes.InternalServerError, entity = e.toRemoteException.toResponseEntity))

      // Uncaught error -- should never happen.
      case t: Throwable =>

         extractUri { uri =>

            logger.error(s"Unexpected Internal Error in [${uri}]", t);
            complete(HttpResponse(StatusCodes.BadRequest, entity = ServerExceptionRemote(ServerError.InternalError, t.getMessage).toResponseEntity))
         }

   }
}

