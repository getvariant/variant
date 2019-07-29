package com.variant.server.routes

import akka.http.scaladsl.server.RequestContext
import play.api.libs.json._
import akka.http.scaladsl.model.ContentTypes
import com.variant.server.boot.ServerExceptionRemote
import com.variant.core.error.ServerError
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal

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
}