package com.variant.server.test.controller

import scala.util.Random
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.collection.JavaConversions._
import com.variant.core.impl.ServerError._
import com.variant.core.util.Constants._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.util.TraceEventReader
import com.variant.server.api.ConfigKeys._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.schema.parser.SchemaParser
import com.variant.server.schema.ServerSchemaParser
import com.variant.server.test.spec.EmbeddedServerAsyncSpec

/**
 * Root Controller (health message)
 */
class RootTest extends EmbeddedServerSpec {
   
      
   "RootController" should {
         
      "Send valid health page" in {
         
         assertResp(route(app, httpReq(GET, "/")))
            .isOk
            .withBodyText { body =>
               var lineCount = 1
               for (char <- body if char == '\n' ) lineCount += 1
               lineCount mustBe 11
         }
      }         
   }
}

