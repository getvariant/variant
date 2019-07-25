package com.variant.server.boot;

import play.api.libs.json._

import com.variant.core.error.UserError.Severity
import com.variant.core.error.ServerError
import com.variant.server.api.ServerException
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.settings.ServerSettings

/**
 * Server exceptions that are generated by the foreground and have predefined
 * error constants in ServerError (core).
 *
 * @since 0.7
 */
class ServerExceptionRemote(error: ServerError, args: String*) extends ServerException {

   // This is tricky. We want java code to be able to construct this object with idiomatic java
   // varargs. To get scala compiler to generate the right bytecode for that, the varargs annotation
   // must be used. For reasons beyond my understanding this annotation does not work on the
   // primary constructor, so we've changed the primary constructor to take an explicit Seq
   // and added this auxiliary constructor with scala varargs. Now both scala (via the
   /* object.apply() and java (via this constructor) can use idiomatic varargs construction.
   @annotation.varargs
   def this(error: ServerError, args: String*) {
      this(error, args)
   }
*/
   /**
    * Remote errors take severity of the underlying error.
    * @return
    */
   override def getSeverity: Severity = error.getSeverity

   /**
    */
   override def getMessage = error.asMessage(args)

   /**
    * Serialize the runtime error as json in a way that will allow the client to
    * deserialize it as an error.
    */
   def toResponseEntity: ResponseEntity = {

      val bodyJson: JsObject = Json.obj(
         // We shouldn't need this. 500 indicates the internal nature of the exception.
         // Plus the error itself has the isInternal method.
         //"isInternal" -> JsBoolean(error.isInternal),
         "code" -> error.getCode,
         "args" -> JsArray(args.map { JsString(_) }))

      HttpEntity(ContentTypes.`application/json`, bodyJson.toString())

      //          header = ResponseHeader(HttpStatus.SC_BAD_REQUEST, Map.empty),
      //          body = HttpEntity.Strict(ByteString(bodyJson.toString()), Some("application/json"))
      //        )

   }

}

object ServerExceptionRemote {

   @annotation.varargs
   def apply(error: ServerError, args: String*) = new ServerExceptionRemote(error, args: _*)

}