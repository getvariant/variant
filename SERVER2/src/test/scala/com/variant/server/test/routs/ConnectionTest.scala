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

/*
 * Reusable event JSON objects.
 */
object ConnectionTest {

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
class ConnectionTest extends EmbeddedSpec {

   val endpoint = "/connection"

   "ConnectionController" should {

      "return  404 on GET with no schema name" in {

         HttpRequest(uri = "/connection") ~> router ~> check {
            handled mustBe true
            status mustBe NotFound
            contentType mustBe ContentTypes.`text/plain(UTF-8)`
            entityAs[String] mustBe "The requested resource could not be found."
         }
      }
      /*
      "return  400 and error on GET to non-existent schema" in {
         assertResp(route(app, httpReq(GET, endpoint + "/bad_schema")))
            .isError(UNKNOWN_SCHEMA, "bad_schema")
      }

      "open connection on POST with valid schema name" in {

         assertResp(route(app, httpReq(GET, endpoint + "/monstrosity")))
            .isOk
            .withBodyJson { json =>
               (json \ "ssnto").as[Long] mustBe server.config.getSessionTimeout
            }
      }
      *
      */
   }
}

