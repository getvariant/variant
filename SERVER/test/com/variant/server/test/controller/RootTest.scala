package com.variant.server.test.controller

import com.variant.server.test.spec.EmbeddedServerSpec

import play.api.test._
import play.api.test.Helpers._

/**
 * Root Controller (health message)
 */
class RootTest extends EmbeddedServerSpec {
   
      
   "RootController" should {
         
      "Send valid health page" in {
         
         assertResp(route(app, httpReq(GET, "/")))
            .isOk
            .withBodyText { body =>
            	//println(body)
               var lineCount = 1
               for (char <- body if char == '\n' ) lineCount += 1
               lineCount mustBe 16
         }
      }         
   }
}

