package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import com.variant.server.boot.VariantServer
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.StandaloneServerSpec

/**
 * Test the server running in a separate process.
 */
class StandaloneServerTest extends StandaloneServerSpec {

   "Server" should {

      "start" in {
         server.start()
      }
      
      /*
      "send 404 on a bad request" in  {
         assertResp(route(app, FakeRequest(GET, context + "/bad")))
            .is(NOT_FOUND)
            .withNoBody
      }
    
      "send splash on a root request" in  {
         assertResp(route(app, FakeRequest(GET, context)))
            .isOk
            .withBodyText { body =>
               body must startWith (VariantServer.productName)
               body must include ("Uptime")
         }
      }
      */
   }
}
