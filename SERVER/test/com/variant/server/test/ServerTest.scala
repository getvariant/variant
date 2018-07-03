package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import com.variant.server.boot.VariantServer
import com.variant.server.test.spec.BaseSpecWithServer

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ServerTest extends BaseSpecWithServer {

   "Routes" should {

      "send 404 on a bad request" in  {
         assertResp(route(app, FakeRequest(GET, context + "/bad")))
            .is(NOT_FOUND)
            .withNoBody
      }
    
      "send splash on a root request" in  {
         assertResp(route(app, FakeRequest(GET, context)))
            .isOk
            .withBodyText { body =>
               body must startWith (VariantServer.instance.productName)
               body must include ("Uptime")
         }
      }
      
   }
}
