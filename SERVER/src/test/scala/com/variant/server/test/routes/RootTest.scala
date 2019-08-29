package com.variant.server.test.routes

import com.variant.server.test.spec.EmbeddedServerSpec

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpMethods
import com.variant.server.boot.VariantServer

/**
 * Root Controller (health message)
 */
class RootTest extends EmbeddedServerSpec {

   "RootController" should {

      "Send valid health page" in {

         HttpRequest(uri = "/") ~> router ~> check {

            status mustBe OK
            contentType mustBe ContentTypes.`text/plain(UTF-8)`
            val lines = entityAs[String].split("\n").toSeq
            lines(0) mustBe s"${VariantServer.productVersion._1} release ${VariantServer.productVersion._2}."
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

