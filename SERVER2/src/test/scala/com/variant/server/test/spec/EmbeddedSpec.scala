package com.variant.server.test.spec

import scala.collection.mutable
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.variant.server.routs.Router
import com.variant.server.boot.VariantServerImpl
import com.variant.server.boot.VariantServer
import com.variant.server.impl.ConfigurationImpl
import com.variant.server.impl.ConfigurationImpl

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

      // This will override the regularly loaded configuration
      private[this] var overrides: Option[Map[String, _]] = None

      // These keys will be deleted from regularly loaded configuration.
      private[this] val deletes = mutable.Seq[String]()

      /**
       * Add given mappings to the standard config, loaded externally by new VariantServerImpl
       */
      def withConfig(overrides: Map[String, _]): ServerBuilder = {
         this.overrides = Some(overrides)
         this
      }

      /**
       * Remove given mappings from the standard config, loaded eternally by new VariantServerImpl
       */
      def withoutKeys(keys: String*): ServerBuilder = {
         deletes ++ keys
         this
      }

      /**
       * Reboot server with given configuration.
       */
      def reboot() {

         EmbeddedSpec.this._server.shutdown()
         EmbeddedSpec.this._server = new VariantServerImpl
         overrides.map { o => EmbeddedSpec.this._server.config.asInstanceOf[ConfigurationImpl].overrideWith(o) }
         EmbeddedSpec.this._server.config.asInstanceOf[ConfigurationImpl].deleteKeys(deletes)
      }
   }

}