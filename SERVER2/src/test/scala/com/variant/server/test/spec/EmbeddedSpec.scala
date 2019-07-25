package com.variant.server.test.spec

import scala.collection.mutable
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.variant.server.routs.Router
import com.variant.server.boot.VariantServerImpl
import com.variant.server.boot.VariantServer
import com.variant.server.impl.ConfigurationImpl
import com.variant.server.impl.ConfigurationImpl
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpMethod

/**
 * Embedded Server
 */
trait EmbeddedSpec extends BaseSpec with ScalatestRouteTest {

   // Build straight up server upon initialization
   // Tests may use the builder object to build other server instances with
   // alternate configuration parameters.
   private[this] var _server: VariantServer = VariantServer()

   def server = _server

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

         EmbeddedSpec.this._server.shutdown()
         EmbeddedSpec.this._server = VariantServer(overrides, deletions)
      }
   }

}