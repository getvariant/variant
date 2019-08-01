package com.variant.server.test.spec

import scala.collection.mutable
import akka.http.scaladsl.testkit.ScalatestRouteTest
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

   // Build straight up server upon initialization
   // Tests may use the builder object to build other server instances with
   // alternate configuration parameters.
   private[this] var _server: VariantServer = VariantServer.builder.headless.build

   implicit def server = _server

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
    * Build another server. Implements a basic builder pattern.
    */
   class ServerBuilder {

      // This will override the regularly loaded configuration
      private[this] var overrides: Map[String, _] = Map.empty

      // These keys will be deleted from regularly loaded configuration.
      private[this] val deletions = mutable.ListBuffer[String]()

      /**
       * Add given mappings to the standard config, loaded externally by new VariantServerImpl
       */
      def withConfig(overrides: Map[String, _]): ServerBuilder = {
         this.overrides = overrides
         this
      }

      /**
       * Remove given mappings from the standard config, loaded eternally by new VariantServerImpl
       */
      def withoutKeys(keys: String*): ServerBuilder = {
         deletions ++= keys
         this
      }

      /**
       * Reboot server with given configuration.
       */
      def reboot() {

         EmbeddedServerSpec.this._server.shutdown()
         EmbeddedServerSpec.this._server = VariantServer.builder
            .headless
            .withOverrides(overrides)
            .withDeletions(deletions)
            .build
      }
   }

   /**
    * Unmarshal the common sessionResponse
    */
   protected class SessionResponse(resp: HttpResponse)(implicit server: VariantServer) {
      handled mustBe true
      if (resp.status == BadRequest) {
         throw new TestFailedException("Unexpected Error [" + ServerErrorResponse(resp).toString() + "]", 2)
      }
      resp.status mustBe OK
      private[this] val respString = entityAs[String]
      respString mustNot be(null)
      private[this] val respJson = Json.parse(respString)
      private[this] val ssnSrc = (respJson \ "session").asOpt[String].getOrElse { failTest("No 'session' element in reponse") }
      private[this] val schemaSrc = (respJson \ "schema" \ "src").asOpt[String].getOrElse { fail("No 'schema/src' element in reponse") }
      private[this] val parserResponse = ServerSchemaParser(implicitly).parse(schemaSrc)

      parserResponse.hasMessages mustBe false

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
      handled mustBe true
      resp.status mustBe BadRequest
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