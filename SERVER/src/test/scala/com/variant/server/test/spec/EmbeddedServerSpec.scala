package com.variant.server.test.spec

import scala.collection.mutable
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.variant.core.error.UserError.Severity
import com.variant.server.routes.Router
import com.variant.server.boot.VariantServerImpl
import com.variant.server.boot.VariantServer
import com.variant.server.impl.ConfigurationImpl
import com.variant.server.impl.ConfigurationImpl
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpMethod
import com.variant.core.session.CoreSession
import akka.http.scaladsl.model.HttpResponse
import com.variant.core.error.ServerError
import akka.http.scaladsl.model.StatusCodes._
import play.api.libs.json._
import com.variant.server.schema.ServerSchemaParser
import org.scalatest.exceptions.TestFailedException

/**
 * Embedded Server
 */
trait EmbeddedServerSpec extends BaseSpec with ScalatestRouteTest {

   // Individual tests can mutate serverBuilder to build a custom server.
   protected lazy val serverBuilder: Unit => VariantServer.Builder = { _ =>
      VariantServer.builder.headless
   }

   // Build the server defined by the concrete descendant.
   private[this] var _server: VariantServer = serverBuilder().build()

   implicit def server = _server

   /**
    * Recreate the underlying server from the given builder.
    * Ensure that it's headless.
    */
   def reboot(f: VariantServer.Builder => Unit = {_=>}) {
      _server = null
      _server = {
         val newBuilder = serverBuilder()
         f(newBuilder)
         newBuilder.build()
      }
   }

   /**
    * Recreate the underlying server from the default builder.
    * Ensure that it's headless.
    *
   def reboot() {
      _server = null
      _server = serverBuilder().build()
      //_server = VariantServer.builder.headless.build()
   }*/
   // Seal the router in order not to have rejections.
   def router = Route.seal(Router(_server).routes)

   val httpMethods = Seq(
      HttpMethods.CONNECT,
      HttpMethods.DELETE,
      HttpMethods.GET,
      HttpMethods.HEAD,
      HttpMethods.OPTIONS,
      HttpMethods.PATCH,
      HttpMethods.POST,
      HttpMethods.PUT)

   /**
    * Unmarshal the common sessionResponse
    */
   protected class SessionResponse(resp: HttpResponse)(implicit server: VariantServer) {
      handled mustBe true
      if (resp.status == BadRequest) {
         throw new TestFailedException("Unexpected User Error [" + ServerErrorResponse(resp).toString() + "]", 2)
      }
      if (resp.status != OK) {
          throw new TestFailedException(s"Unexpected HTTP Status ${resp.status} with body [${resp.entity}]", 2)
      }
      private[this] val respString = entityAs[String]
      respString mustNot be(null)
      private[this] val respJson = Json.parse(respString)
      private[this] val ssnSrc = (respJson \ "session").asOpt[String].getOrElse { failTest("No 'session' element in reponse") }
      private[this] val schemaSrc = (respJson \ "schema" \ "src").asOpt[String].getOrElse { failTest("No 'schema/src' element in reponse") }

      private[this] val parserResponse = ServerSchemaParser(implicitly).parse(schemaSrc)
      parserResponse.hasMessages(Severity.ERROR) mustBe false

      val schema = parserResponse.getSchema
      val session = CoreSession.fromJson(ssnSrc, schema)
      val schemaId = (respJson \ "schema" \ "id").asOpt[String].getOrElse { fail("No 'schema/id' element in reponse") }
   }
   protected object SessionResponse {
      def apply(resp: HttpResponse) = new SessionResponse(resp)
   }

   /**
    * Unmarshal the common error response body
    */
   protected class ServerErrorResponse(resp: HttpResponse) {
      if (handled != true)
         throw new TestFailedException("Request was not handled", 2)
      if (resp.status != BadRequest)
         throw new TestFailedException(s"Response status [${resp.status}] was not equal to [${BadRequest}]", 2)
      private[this] val respString = entityAs[String]
      respString mustNot be(null)
      private[this] val respJson = Json.parse(respString)
      val code = (respJson \ "code").as[Int]
      val args = (respJson \ "args").as[List[String]]

      def mustBe(error: ServerError, args: String*) {
         if (this.code != error.getCode) {
            throw new TestFailedException(
               "Error [" + ServerErrorResponse(resp).toString() + "] was not equal [" + error.asMessage(args: _*) + "]", 1)
         }

         if (this.code != error.getCode || this.args != args)
            throw new TestFailedException(
               "Error [%s] was not equal to [%s]".format(
                  ServerError.byCode(this.code).asMessage(this.args: _*),
                  error.asMessage(args: _*)),
               1)
      }

      override def toString = {
         ServerError.byCode(code).asMessage(args: _*)
      }
   }
   protected object ServerErrorResponse {
      def apply(resp: HttpResponse) = new ServerErrorResponse(resp)
   }

   /**
    * Unmarshal our simple non-streaming response entity into a string.
    *
    * private def unmarshal(response: HttpResponse): String = {
    * implicit val materializer = ActorMaterializer()
    * Await.result(Unmarshal(response).to[String].recover[String] { case error ⇒ failTest("could not unmarshal") }, 1.second)
    * }
    *
    */
}