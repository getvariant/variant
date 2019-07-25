package com.variant.server.test.routs

import scala.util.Random
import com.variant.core.Constants._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.spec.EmbeddedSpec
import com.variant.core.schema.parser.SchemaParser
import com.variant.server.schema.ServerSchemaParser
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpMethods
import play.api.libs.json._

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
class SchemaTest extends EmbeddedSpec {

   "ConnectionController" should {

      "return  404 on GET with no schema name" in {

         HttpRequest(uri = "/schema") ~> router ~> check {
            handled mustBe true
            status mustBe NotFound
            contentType mustBe ContentTypes.`text/plain(UTF-8)`
            entityAs[String] mustBe "The requested resource could not be found."
         }
      }

      "respond OK on GET with valid schema" in {

         HttpRequest(method = HttpMethods.GET, uri = "/schema/monstrosity") ~> router ~> check {
            handled mustBe true
            status mustBe OK
            val respBody = Json.parse(entityAs[String])
            (respBody \ "ssnto").as[Long] mustBe server.config.sessionTimeout
         }
      }

      "respond OK on GET with invalid schema" in {

         HttpRequest(method = HttpMethods.GET, uri = "/schema/invalid") ~> router ~> check {
            handled mustBe true
            status mustBe OK
            val respBody = Json.parse(entityAs[String])
            (respBody \ "ssnto").as[Long] mustBe server.config.sessionTimeout
         }
      }

   }
}

