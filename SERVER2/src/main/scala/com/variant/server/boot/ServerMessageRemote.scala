package com.variant.server.boot

import akka.util.ByteString
import com.variant.core.error.ServerError
import com.variant.server.api.ServerException
import akka.http.scaladsl.model.HttpEntity
import play.api.libs.json._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.ResponseEntity

/**
 * Wrap the core ServerError type with some server side semantics.
 */
object ServerMessageRemote {

   def apply(error: ServerError) = new ServerErrorRemote(error)
}

class ServerErrorRemote(error: ServerError) {

   /**
    * Serialize the runtime error as json in a way that will allow the client to
    * deserialize it as an error.
    */
   def asResponseEntity(args: String*): ResponseEntity = {

      val bodyJson: JsObject = Json.obj(
         "isInternal" -> JsBoolean(error.isInternal),
         "code" -> error.getCode,
         "args" -> JsArray(args.map { JsString(_) }))

      HttpEntity(ContentTypes.`application/json`, bodyJson.toString())

      //          header = ResponseHeader(HttpStatus.SC_BAD_REQUEST, Map.empty),
      //          body = HttpEntity.Strict(ByteString(bodyJson.toString()), Some("application/json"))
      //        )

   }

}