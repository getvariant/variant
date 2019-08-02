package com.variant.server.routes

import akka.http.scaladsl.server.ExceptionHandler
import com.variant.server.boot.ServerExceptionRemote
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging
import com.variant.core.error.ServerError
import com.variant.server.boot.ServerExceptionInternal
import akka.http.scaladsl.model.ContentTypes
import com.variant.core.error.UserError.Severity

object CustomExceptionHandler extends LazyLogging {

   def apply() = ExceptionHandler {

      // Remote user error.
      case ex: ServerExceptionRemote =>

         if (ex.error.isInternal()) {
            logger.error("Internal API Error", ex)
         }
         else logger.whenDebugEnabled {
            logger.debug(s"Remote server error: ${ex.getMessage}")
         }

         complete(HttpResponse(StatusCodes.BadRequest, entity = ex.toResponseEntity))

      // We barfed while carrying out a remote request.
      case ex: ServerExceptionInternal =>

         logger.whenDebugEnabled {
            logger.debug(s"Internal server error: ${ex.getMessage}")
         }

         complete(HttpResponse(StatusCodes.InternalServerError, entity = ex.toRemoteException.toResponseEntity))

      // Uncaught error -- should never happen.
      case t: Throwable =>

         extractUri { uri =>

            logger.error(s"Unexpected Internal Error in [${uri}]", t);
            complete(HttpResponse(StatusCodes.BadRequest, entity = ServerExceptionRemote(ServerError.InternalError, t.getMessage).toResponseEntity))
         }

   }
}

