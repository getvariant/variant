package com.variant.server.test.controller

import scala.util.Random
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.collection.JavaConversions._
import com.variant.core.error.ServerError._
import com.variant.core.Constants._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.util.TraceEventReader
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.schema.parser.SchemaParser
import com.variant.server.schema.ServerSchemaParser
import com.variant.server.test.spec.EmbeddedServerAsyncSpec

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
class ConnectionTest extends EmbeddedServerAsyncSpec {
   
      
   val endpoint = "/connection"

   "ConnectionController" should {
   
      "return  404 on GET with no schema name" in {
         assertResp(route(app, httpReq(GET, endpoint)))
            .is(NOT_FOUND)
            .withNoBody
      }

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
   }
}

