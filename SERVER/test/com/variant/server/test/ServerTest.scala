package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import com.variant.server.boot.VariantServer

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ServerTest extends BaseSpecWithServer {

   "Routes" should {

      "send 404 on a bad request" in  {
         val resp = route(app, FakeRequest(GET, context + "/bad")).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }
    
      "send splash on a root request" in  {
         println(context)
         val resp = route(app, FakeRequest(GET, context)).get
         status(resp) mustBe OK
         println(contentAsString(resp))
         contentAsString(resp) must startWith (VariantServer.server.productName)
         contentAsString(resp) must include ("Uptime")
      }
      
   }
}
