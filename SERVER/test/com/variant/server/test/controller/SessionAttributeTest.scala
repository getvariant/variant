package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.controller.SessionTest._
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.core.impl.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.util.Constants._

/**
 * Session Attribute Tests
 */
class SessionAttributeTest extends BaseSpecWithServer {
      
   val endpointSession = context + "/session"
   val endpointAttribute = context + "/session/attr"
   
   val sessionJsonBigCovar = ParameterizedString(
      sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
         
   val sessionJsonPetclinic = ParameterizedString(
         sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))

   "SessionController" should {

      val sid1 = newSid
      
      "Create a new session" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid1)
         assertResp(route(app, httpReq(PUT, endpointSession + "/big_conjoint_schema").withBody(body)))
            .isOk
            .withNoBody
      }

      "return null on read of non-existent attribute" in {
                  
         assertResp(route(app, httpReq(GET, endpointSession + "/big_conjoint_schema/" + sid1)))
            .isOk
            .withBodyJson  { json =>
               extractAttr(json, "non-existent") mustBe None
            }         
      }

      "clear non-existent attribute should return null" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "non-existent"
         )
         assertResp(route(app, httpReq(DELETE, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodyJson  { json =>
               extractAttr(json, "non-existent") mustBe None
            }         
      }

      "set an attribute" in {
         
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME",
            "value" -> "ATTRIBUTE VALUE"
         )
         assertResp(route(app, httpReq(PUT, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodyJson  { json =>
               (json \ "returns").asOpt[String] mustBe None
               extractAttr(json, "ATTRIBUTE NAME") mustBe Some("ATTRIBUTE VALUE")
            }
      }

      "read the attribute " in {
                  
         assertResp(route(app, httpReq(POST, endpointSession + "/big_conjoint_schema/" + sid1)))
            .isOk
            .withBodyJson  { json =>
               extractAttr(json, "ATTRIBUTE NAME") mustBe Some("ATTRIBUTE VALUE")
            }         
      }
            
      "update the attribute " in {
     
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME",
            "value" -> "SOME OTHER VALUE"
         )
         assertResp(route(app, httpReq(PUT, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodyJson  { json =>
               (json \ "returns").asOpt[String] mustBe Some("ATTRIBUTE VALUE")
               extractAttr(json, "ATTRIBUTE NAME") mustBe Some("SOME OTHER VALUE")
            }
      }
      
      "read the updated attribute in original session" in {
                  
         assertResp(route(app, httpReq(GET, endpointSession + "/big_conjoint_schema/" + sid1)))
            .isOk
            .withBodyJson  { json =>
               extractAttr(json, "ATTRIBUTE NAME") mustBe Some("SOME OTHER VALUE")
            }         
      }

      "clear the attribute" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME"
         )
         assertResp(route(app, httpReq(DELETE, endpointAttribute).withBody(body.toString())))
            .isOk
            .withBodyJson  { json =>
               (json \ "returns").asOpt[String] mustBe Some("SOME OTHER VALUE")
               extractAttr(json, "ATTRIBUTE NAME") mustBe None
            }         
      }

      "Confirm that the attribute is gone" in {
                  
         assertResp(route(app, httpReq(POST, endpointSession + "/big_conjoint_schema/" + sid1)))
            .isOk
            .withBodyJson  { json =>
               extractAttr(json, "ATTRIBUTE NAME") mustBe None
            }         
      }

   }

   /**
    * Extract the value of an attribute from the response JSON.
    * Note that the session JSON is stringified, so has to be parsed.
    */
   def extractAttr(json: JsValue, name: String): Option[String] = {
      val ssnJson = Json.parse((json \ "session").as[String])
      val attrList = (ssnJson \ "attrList").as[List[Map[String, String]]]
      var result: Option[String] = None
      attrList.foreach { map => 
         if (map.get("name").get == name) result = map.get("val") 
         }
      result
   }
}
