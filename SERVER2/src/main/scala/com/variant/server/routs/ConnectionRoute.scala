package com.variant.server.routs

import com.variant.core.error.ServerError
import com.variant.server.boot.VariantServer
import scala.io.Source
import com.typesafe.scalalogging.LazyLogging
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.server.RequestContext
import com.variant.server.boot.ServerMessageRemote
import akka.http.scaladsl.model.HttpEntity
import play.api.libs.json._
import akka.http.scaladsl.server.Directives._
import com.variant.server.boot.ServerExceptionRemote
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ContentTypes

object ConnectionRoute extends VariantRoute with LazyLogging {

   /**
    * Ping a schema.
    * Called on VariantClient.connectTo(schema)
    */
   def get(req: RequestContext, name: String)(implicit server: VariantServer) = {

      server.schemata.get(name) match {

         case Some(schema) => {
            logger.debug("Schema [%s] found".format(name))

            val entity = JsObject(Seq(
               "ssnto" -> JsNumber(server.config.sessionTimeout)))

            complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, entity.toString())))
         }
         case None => {
            logger.debug("Schema [%s] not found".format(name))
            throw new ServerExceptionRemote(ServerError.UNKNOWN_SCHEMA, name)
         }
      }
   }

}
