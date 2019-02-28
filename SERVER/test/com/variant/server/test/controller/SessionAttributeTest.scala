package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.controller.SessionTest._
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.error.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.Constants._

/**
 * Session Attribute Tests
 */
class SessionAttributeTest extends EmbeddedServerSpec {
      
   val endpointSession = "/session"
   val endpointAttribute = "/session/attr"
   
   val sessionJsonBigCovar = ParameterizedString(
      sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
         
   val sessionJsonPetclinic = ParameterizedString(
         sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))

   val emptyTargetingTrackerBody = "{\"tt\":[]}"

   "SessionController" should {

      val sid1 = newSid
      
      "Create a new session" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid1)
         assertResp(route(app, httpReq(PUT, endpointSession + "/monstrosity").withBody(body)))
            .isOk
            .withNoBody
      }

      "session must already have 2 attributes" in {
                  
         assertResp(route(app, httpReq(GET, endpointSession + "/monstrosity/" + sid1)))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.size mustBe 2
            }         
         
         server.ssnStore.get(sid1).get.getAttributes.size mustBe 2
      }

      "clear all attributes" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "map" -> Map[String,String]()
         )
         assertResp(route(app, httpReq(PUT, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.size mustBe 0
            }         
         
         server.ssnStore.get(sid1).get.getAttributes.size mustBe 0
      }

      "set new attributes" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "map" -> Map("foo"->"bar", "another attribute" -> "another value")
         )
         assertResp(route(app, httpReq(PUT, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.size mustBe 2
               ssn.getAttributes.get("foo") mustBe "bar"
               ssn.getAttributes.get("another attribute") mustBe "another value"
               ssn.getAttributes.get("bar") mustBe null
            }         
         
         // Check in the session store too.
         val attrs = server.ssnStore.get(sid1).get.getAttributes
         attrs.size mustBe 2
      	attrs.get("foo") mustBe "bar"
         attrs.get("another attribute") mustBe "another value"
         attrs.get("bar") mustBe null
      }
 
      "replace a single attribute" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "map" -> Map("foo"->"barr")
         )
         assertResp(route(app, httpReq(PUT, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.size mustBe 1
               ssn.getAttributes.get("foo") mustBe "barr"
            }         
         
         // Check in the session store too.
         val attrs = server.ssnStore.get(sid1).get.getAttributes
         attrs.size mustBe 1
      	attrs.get("foo") mustBe "barr"
      }
   }

}
