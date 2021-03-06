package com.variant.server.test.routes

import scala.util.Random
import com.variant.core.Constants._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.schema.parser.SchemaParser
import com.variant.server.schema.ServerSchemaParser
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpMethods._
import play.api.libs.json._
import com.variant.core.error.ServerError
import akka.http.scaladsl.model.HttpMethods

/*
 * Reusable event JSON objects.
 */
object SchemaTest {

   val body = ParameterizedString("""
      {"sid":"${sid:SID}",
       "name":"${name:NAME}",
       "value":"${value:VALUE}",
       "ts":${ts:%d},
       "params":[{"name":"Name One","value":"Value One"},{"name":"Name Two","value":"Value Two"}]
      }
   """.format(System.currentTimeMillis()))

   val bodyNoSid = """{"name":"NAME","value":"VALUE"}"""

   val bodyNoName = """{"sid":"SID","value":"VALUE"}"""

   val bodyNoParamName = ParameterizedString("""
      {"sid":"${sid:SID}",
       "name":"NAME",
       "value":"VALUE",
       "ts":%d,
       "params":[{"namee":"Name One","value":"Value One"}]
      }
   """.format(System.currentTimeMillis()))

}

/**
 * Event Controller
 */
class SchemaTest extends EmbeddedServerSpec {

   "ConnectionController" should {

      "respond NotFound on GET with no schema name" in {

         HttpRequest(uri = "/schema") ~> router ~> check {
            handled mustBe true
            status mustBe NotFound
            contentType mustBe ContentTypes.`text/plain(UTF-8)`
            entityAs[String] mustBe "The requested resource could not be found."
         }
      }

      "respond OK on GET with valid schema" in {

         HttpRequest(method = GET, uri = "/schema/monstrosity") ~> router ~> check {
            handled mustBe true
            status mustBe OK
            val respBody = Json.parse(entityAs[String])
            (respBody \ "ssnto").as[Long] mustBe server.config.sessionTimeout
         }
      }

      "respond UNKNOWN_SCHEMA on GET with invalid schema" in {

         HttpRequest(method = GET, uri = "/schema/invalid") ~> router ~> check {
            handled mustBe true
            status mustBe BadRequest
            //println(entityAs[String])
            val respBody = Json.parse(entityAs[String])
            (respBody \ "code").as[Long] mustBe ServerError.UNKNOWN_SCHEMA.getCode
            (respBody \ "args").as[List[String]] mustBe List("invalid")
         }
      }

      "respond MethodNotAllowed on everythig but GET with invalid schema" in {

         httpMethods.filter(_ != HttpMethods.GET).foreach { method =>
            HttpRequest(method = method, uri = "/schema/invalid") ~> router ~> check {
               handled mustBe true
               status mustBe MethodNotAllowed
               contentType mustBe ContentTypes.`text/plain(UTF-8)`
               entityAs[String] mustBe "HTTP method not allowed, supported methods: GET"
            }
         }
      }

   }
}

