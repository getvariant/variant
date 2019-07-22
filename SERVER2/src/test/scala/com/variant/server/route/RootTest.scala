package com.variant.server.test.route

import com.variant.server.test.spec.EmbeddedSpec

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes

/**
 * Root Controller (health message)
 */
class RootTest extends EmbeddedSpec {

   "RootController" should {

      "Send valid health page" in {
         // note that there's no need for the host part in the uri:
         val request = HttpRequest(uri = "/")

         request ~> router ~> check {
            status mustBe StatusCodes.OK

            // we expect the response to be json:
            contentType mustBe ContentTypes.`text/plain(UTF-8)`

            println(entityAs[String])
            val lines = entityAs[String].split("\n").toSeq
            lines(0) mustBe server.productName + '.'
            lines(1) must startWith("Uptime")
            lines(2) mustBe "No schemata deployed."
         }
      }
   }
}

