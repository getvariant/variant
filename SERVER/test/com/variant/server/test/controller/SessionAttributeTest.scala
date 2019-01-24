package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.controller.SessionTest._
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.impl.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.util.Constants._

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

      "clear non-existent attribute should return noop" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "non-existent"
         )
         assertResp(route(app, httpReq(DELETE, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.size mustBe 2
            }         
         
         server.ssnStore.get(sid1).get.getAttributes.size mustBe 2
      }

      "set an attribute" in {
         
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME",
            "value" -> "ATTRIBUTE VALUE"
         )
         assertResp(route(app, httpReq(PUT, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.size mustBe 2
            }
         
         server.ssnStore.get(sid1).get.getAttributes.size mustBe 3   
         server.ssnStore.get(sid1).get.getAttributes.get("ATTRIBUTE NAME") mustBe "ATTRIBUTE VALUE"
      }

      "read the attribute " in {
                  
         assertResp(route(app, httpReq(POST, endpointSession + "/monstrosity/" + sid1).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.get("ATTRIBUTE NAME") mustBe "ATTRIBUTE VALUE"
            }         

         server.ssnStore.get(sid1).get.getAttributes.size mustBe 3   
         server.ssnStore.get(sid1).get.getAttributes.get("ATTRIBUTE NAME") mustBe "ATTRIBUTE VALUE"
      }
            
      "update the attribute " in {
     
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME",
            "value" -> "SOME OTHER VALUE"
         )
         assertResp(route(app, httpReq(PUT, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.get("ATTRIBUTE NAME") mustBe "ATTRIBUTE VALUE"
            }

         server.ssnStore.get(sid1).get.getAttributes.size mustBe 3
         server.ssnStore.get(sid1).get.getAttributes.get("ATTRIBUTE NAME") mustBe "SOME OTHER VALUE"
      }
      
      "read the updated attribute in original session" in {
                  
         assertResp(route(app, httpReq(GET, endpointSession + "/monstrosity/" + sid1)))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.get("ATTRIBUTE NAME") mustBe "SOME OTHER VALUE"
            }         

         server.ssnStore.get(sid1).get.getAttributes.size mustBe 3
         server.ssnStore.get(sid1).get.getAttributes.get("ATTRIBUTE NAME") mustBe "SOME OTHER VALUE"
      }

      "clear the attribute" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME"
         )
         assertResp(route(app, httpReq(DELETE, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.get("ATTRIBUTE NAME") mustBe "SOME OTHER VALUE"
            }         

         server.ssnStore.get(sid1).get.getAttributes.size mustBe 2
         server.ssnStore.get(sid1).get.getAttributes.get("ATTRIBUTE NAME") mustBe null
      }

      "Confirm that the attribute is gone" in {
                  
         assertResp(route(app, httpReq(POST, endpointSession + "/monstrosity/" + sid1).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.get("ATTRIBUTE NAME") mustBe null
               ssn.getAttributes.size mustBe 2
            }         

         server.ssnStore.get(sid1).get.getAttributes.size mustBe 2
      }

   }

}
