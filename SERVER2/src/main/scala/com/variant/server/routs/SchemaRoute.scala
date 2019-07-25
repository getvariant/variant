package com.variant.server.routs

import com.variant.core.error.ServerError
import com.variant.server.boot.VariantServer
import scala.io.Source
import com.typesafe.scalalogging.LazyLogging
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.model.HttpEntity
import play.api.libs.json._
import akka.http.scaladsl.server.Directives._
import com.variant.server.boot.ServerExceptionRemote
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ContentTypes

object SchemaRoute extends VariantRoute with LazyLogging {

   /**
    * Ping a schema by name. Called on VariantClient.connectTo(schema).
    * Our connections are stateless on the server, so the client simply pings to ensure the schema exists.
    */
   def get(name: String)(implicit server: VariantServer, ctx: RequestContext): HttpResponse = {

      server.schemata.get(name) match {

         case Some(schema) => {
            logger.debug("Schema [%s] found".format(name))

            val entity = JsObject(Seq(
               "ssnto" -> JsNumber(server.config.sessionTimeout)))

            HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, entity.toString()))
         }
         case None => {
            logger.debug("Schema [%s] not found".format(name))
            throw new ServerExceptionRemote(ServerError.UNKNOWN_SCHEMA, name)
         }
      }
   }

}
