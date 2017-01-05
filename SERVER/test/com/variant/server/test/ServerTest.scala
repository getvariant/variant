package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ServerTest extends BaseSpecWithServer {

  "Routes" should {

    "send 404 on a bad request" in  {
       val resp = route(app, FakeRequest(GET, "/session/foo")).get
       status(resp) mustBe NOT_FOUND
       contentAsString(resp) mustBe empty
    }
  }
}
