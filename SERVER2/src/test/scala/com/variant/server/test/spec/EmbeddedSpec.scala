package com.variant.server.test.spec

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.variant.server.routs.Router
import com.variant.server.boot.VariantServerImpl
import com.variant.server.boot.VariantServer

/**
 * Embedded Server
 */
trait EmbeddedSpec extends BaseSpec with ScalatestRouteTest {

   // Build straight up server upon initialization
   // Tests may use the builder object to build other server instances with
   // alternate configuration parameters.
   private[this] var _server: VariantServer = new VariantServerImpl

   def server = _server

   def router = Router(_server).routs

   /**
    * implements a basid builder pattern.
    */
   class ServerBuilder {

      private[this] var overrides: Option[Map[String, String]] = None

      def withConfig(overrides: Map[String, String]): ServerBuilder = {
         this.overrides = Some(overrides)
         this
      }

      def build {
         EmbeddedSpec.this._server = overrides match {
            case None => new VariantServerImpl
            case Some(map) => new VariantServerImpl(map)
         }

      }
   }

}