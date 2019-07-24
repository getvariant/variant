package com.variant.server.routs

import akka.http.scaladsl.server.ExceptionHandler
import com.variant.server.boot.ServerExceptionRemote
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.variant.server.boot.ServerMessageRemote
import com.typesafe.scalalogging.LazyLogging
import com.variant.core.error.ServerError

object VariantExceptionHandler extends LazyLogging {

   def apply() = ExceptionHandler {

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
}

