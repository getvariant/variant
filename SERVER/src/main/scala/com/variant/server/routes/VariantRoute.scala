package com.variant.server.routes

import akka.http.scaladsl.server.RequestContext
import play.api.libs.json._
import akka.http.scaladsl.model.ContentTypes
import com.variant.server.boot.ServerExceptionRemote
import com.variant.share.error.ServerError
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.variant.server.boot.VariantServer
import com.variant.server.impl.SessionImpl
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import scala.concurrent.Await
import scala.concurrent.duration._
import com.variant.server.boot.ServerExceptionInternal
import com.typesafe.scalalogging.LazyLogging

trait VariantRoute extends LazyLogging {

   /**
    * Nullary action.
    */
   def action(block: => HttpResponse)(implicit server: VariantServer): HttpResponse = {
      if (server.isUp) block
      else HttpResponse(StatusCodes.ServiceUnavailable)
   }

   /**
    * Action that expects a request body.
    */
   def action(block: (JsValue) => HttpResponse)(implicit server: VariantServer, ctx: RequestContext): HttpResponse = {

      if (server.isUp) {

         // We extract the body of the request instead of relying on a standard extractor so we can delay extraction
         // until after we check if server is up.
         implicit val system = server.actorSystem
         implicit val materializer = ActorMaterializer()

         // We're ignoring exceptions here, which should be okay, as they will be caught in the exception handler
         // and there's no reason to intercept them here.
         val body = Await.result(Unmarshal(ctx.request).to[String], 1.second)
         logger.trace(s"Executing route ${ctx.request.method} ${ctx.request.uri} with body:\n${body}")

         block(parse(body))
      } else {
         HttpResponse(StatusCodes.ServiceUnavailable)
      }
   }

   /**
    * Extract body from request context
    *
    * def body(implicit system: ActorSystem, reqCtx: RequestContext): JsValue = {
    *
    * if (reqCtx.request.entity.getContentType != ContentTypes.`application/json`) {
    * throw ServerExceptionRemote(ServerError.BadContentType)
    * }
    *
    * implicit val materializer = ActorMaterializer
    * Json.parse("blah")
    * }
    */

   /**
    * Extract the session from the session store, making sure that
    * if session exists, its schema gen must match the schema name.
    */
   def getSession(schemaName: String, sid: String)(implicit server: VariantServer): Option[SessionImpl] = {

      server.ssnStore.get(sid) map { ssn =>
         server.schemata.get(schemaName) match {
            case Some(schema) =>
               // Schema in request exists, but does not match the one to which this session is connected.
               if (ssn.schemaGen.getMeta.getName != schemaName)
                  throw new ServerExceptionRemote(ServerError.WRONG_CONNECTION, schemaName)
            case None =>
               // Schema in request does not exist. Give the same error.
               throw new ServerExceptionRemote(ServerError.WRONG_CONNECTION, schemaName)
         }
         ssn
      }
   }

   /**
    * Build standard session response used by most APIs.
    */
   def stdSessionResponse(session: SessionImpl): HttpResponse = {

      // If we're here, we have the session
      val entity = JsObject(Seq(
         "session" -> JsString(session.toJson),
         "schema" -> JsObject(Seq(
            "id" -> JsString(session.schemaGen.id),
            "src" -> JsString(session.schemaGen.source)))))

      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, entity.toString()))
   }

   /**
    *  Parse request body
    */
   def parse(body: String) = {

      if (body == null || body.size == 0)
         throw ServerExceptionRemote(ServerError.EmptyBody)

      Try[JsValue] { Json.parse(body) } match {
         case Success(json) => json
         case Failure(t) => throw ServerExceptionRemote(ServerError.JsonParseError, t.getMessage)
      }
   }

}