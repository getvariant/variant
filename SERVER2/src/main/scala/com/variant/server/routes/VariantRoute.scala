package com.variant.server.routes

import akka.http.scaladsl.server.RequestContext
import play.api.libs.json._
import akka.http.scaladsl.model.ContentTypes
import com.variant.server.boot.ServerExceptionRemote
import com.variant.core.error.ServerError
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.variant.server.boot.VariantServer
import com.variant.server.impl.SessionImpl
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes

trait VariantRoute {

   /**
    * Extract body from request context
    */
   def body(implicit system: ActorSystem, reqCtx: RequestContext): JsValue = {

      val req = reqCtx.request
      if (req.entity.getContentType != ContentTypes.`application/json`) {
         throw ServerExceptionRemote(ServerError.BadContentType)
      }

      implicit val materializer = ActorMaterializer

      Json.parse("blah")
   }

   /**
    * Extract the session from the session store, making sure that
    * 1. schema exists.
    * 2. if session exists, its schema gen must match the schema name.
    */
   def getSession(schemaName: String, sid: String)(implicit server: VariantServer): Option[SessionImpl] = {

      val schema = server.schemata.get(schemaName).getOrElse {
         throw ServerExceptionRemote(ServerError.UNKNOWN_SCHEMA, schemaName)
      }

      server.ssnStore.get(sid) map { ssn =>
         if (ssn.schemaGen.getMeta.getName != schemaName)
            throw new ServerExceptionRemote(ServerError.WRONG_CONNECTION, schemaName)
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
}