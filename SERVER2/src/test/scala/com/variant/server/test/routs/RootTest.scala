package com.variant.server.test.routs

import com.variant.server.test.spec.EmbeddedSpec

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpMethods

/**
 * Root Controller (health message)
 */
class RootTest extends EmbeddedSpec {

   "RootController" should {

      "Send valid health page" in {

         HttpRequest(uri = "/") ~> router ~> check {

            status mustBe OK
            contentType mustBe ContentTypes.`text/plain(UTF-8)`
            val lines = entityAs[String].split("\n").toSeq
            lines(0) mustBe s"${server.productVersion.comment} release ${server.productVersion.version}."
            lines(1) must startWith("Uptime")
            lines(2) mustBe "Schemata:"
            lines.size mustBe 15
         }
      }

      "Reject unmapped path for all http methods" in {

         httpMethods.foreach { method =>
            HttpRequest(method, uri = "/invalid") ~> router ~> check {
               handled mustBe true
               status mustBe NotFound
               contentType mustBe ContentTypes.`text/plain(UTF-8)`
               entityAs[String] mustBe "The requested resource could not be found."
            }
         }
      }

   }
}

