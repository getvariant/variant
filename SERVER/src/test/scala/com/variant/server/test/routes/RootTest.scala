package com.variant.server.test.routes

import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.boot.VariantServer

import play.api.libs.json._

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpMethods

/**
 * Root Controller (health message)
 */
class RootTest extends EmbeddedServerSpec {

   "RootController" should {

      "Send valid health page" in {

         HttpRequest(uri = "/") ~> router ~> check {

            status mustBe OK
            //println(entityAs[String])
            contentType mustBe ContentTypes.`application/json`
            val respJson = Json.parse(entityAs[String])
            (respJson \ "name").as[String] mustBe VariantServer.name
            (respJson \ "version").as[String] mustBe VariantServer.version
            (respJson \ "uptimeSeconds").as[Long] must be <= (server.uptime.toSeconds)
            (respJson \ "build" \ "timestamp").asOpt[String].isDefined mustBe true
            (respJson \ "build" \ "scalaVersion").asOpt[String].isDefined mustBe true
            (respJson \ "build" \ "javacVersion").asOpt[String].isDefined mustBe true
            (respJson \ "build" \ "javaVm").asOpt[String].isDefined mustBe true
            (respJson \ "schemata").as[Seq[JsValue]].size mustBe 3
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

