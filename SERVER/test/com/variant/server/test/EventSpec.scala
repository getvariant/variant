package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder

/**
 * Event Controller
 */
class EventSpec extends VariantSpec {

   val endpoint = context + "/event"
   
   "EventController" should {

      "GET should return 404" in {
       
         val resp = route(app, FakeRequest(GET, endpoint)).get
         status(resp) mustBe NOT_FOUND
         // The following won't work because in test mode, play emits debugging HTML.
         contentAsString(resp) mustBe empty
      }

   }
}
