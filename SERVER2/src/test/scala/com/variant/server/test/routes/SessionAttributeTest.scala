package com.variant.server.test.routes

import scala.collection.JavaConverters._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.routes.SessionTest._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.error.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.Constants._
import java.time.format.DateTimeFormatter
import java.time.Instant
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes

/**
 * Session Attribute Tests
 */
class SessionAttributeTest extends EmbeddedServerSpec {

   import SessionTest._

   "Session route" should {

      var sid = newSid

      "Create a new session" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/petclinic/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            ssnResp.session.getAttributes mustBe empty
            sid = ssnResp.session.getId
         }
         server.ssnStore.get(sid).get.getAttributes mustBe empty
      }

      "respond OK on clear non-existent attributes" in {

         val body: JsValue = Json.obj(
            "attrs" -> List("NAME1", "NAME2"))

         HttpRequest(method = HttpMethods.DELETE, uri = s"/session-attr/petclinic/${sid}", entity = body.toString()) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            ssnResp.session.getAttributes mustBe empty
         }
         server.ssnStore.get(sid).get.getAttributes.size mustBe 0
      }

      "set new attributes" in {

         val body: JsValue = Json.obj(
            "map" -> Map("foo" -> "bar", "another attribute" -> "another value"))

         HttpRequest(method = HttpMethods.PUT, uri = s"/session-attr/petclinic/${sid}", entity = body.toString()) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            ssnResp.session.getAttributes.asScala mustBe Map("foo" -> "bar", "another attribute" -> "another value")
         }

         server.ssnStore.get(sid).get.getAttributes.asScala mustBe Map("foo" -> "bar", "another attribute" -> "another value")

      }

      "replace a single attribute" in {

         val body: JsValue = Json.obj(
            "map" -> Map("foo" -> "barr"))

         HttpRequest(method = HttpMethods.PUT, uri = s"/session-attr/petclinic/${sid}", entity = body.toString()) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            ssnResp.session.getAttributes.asScala mustBe Map("foo" -> "barr", "another attribute" -> "another value")
         }

         server.ssnStore.get(sid).get.getAttributes.asScala mustBe Map("foo" -> "barr", "another attribute" -> "another value")
      }

      "respond MethodNotAllowed on everythig except PUT,DELETE" in {

         httpMethods.filterNot(List(HttpMethods.PUT, HttpMethods.DELETE).contains _).foreach { method =>
            println(method)
            HttpRequest(method = method, uri = "/session-attr/petclinic/foo") ~> router ~> check {
               handled mustBe true
               status mustBe MethodNotAllowed
               contentType mustBe ContentTypes.`text/plain(UTF-8)`
               entityAs[String] mustBe "HTTP method not allowed, supported methods: PUT, DELETE"
            }
         }
      }
   }
}
