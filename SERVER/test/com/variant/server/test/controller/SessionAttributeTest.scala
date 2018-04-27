package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.controller.SessionTest._
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.core.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.util.Constants._
import com.variant.core.ConnectionStatus._

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

      var cid1: String = null 
      val sid1 = newSid
      
      "obtain a connection and a session" in {

         assertResp(route(app, connectionRequest("big_covar_schema")))
           .isOk
           .withConnStatusHeader(OPEN)
           .withBodyJson { json =>
               cid1 = (json \ "id").as[String]
               cid1 mustNot be (null)
         }
         
         val body = sessionJsonBigCovar.expand("sid" -> sid1)
         assertResp(route(app, connectedRequest(PUT, endpointSession, cid1).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
      }

      "return null on read of non-existent attribute" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "non-existent"
         )
         assertResp(route(app, connectedRequest(GET, endpointSession, cid1).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson  { json =>
               extractAttr(json, "non-existent") mustBe None
            }         
      }

      "clear non-existent attribute should return null" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "non-existent"
         )
         assertResp(route(app, connectedRequest(DELETE, endpointAttribute, cid1).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson  { json =>
               extractAttr(json, "non-existent") mustBe None
            }         
      }

      "set an attribute in first session" in {
         
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME",
            "value" -> "ATTRIBUTE VALUE"
         )
         assertResp(route(app, connectedRequest(PUT, endpointAttribute, cid1).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson  { json =>
               (json \ "returns").asOpt[String] mustBe None
               extractAttr(json, "ATTRIBUTE NAME") mustBe Some("ATTRIBUTE VALUE")
            }
      }

      "read the attribute in first session" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1
         )
         assertResp(route(app, connectedRequest(GET, endpointSession, cid1).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson  { json =>
               extractAttr(json, "ATTRIBUTE NAME") mustBe Some("ATTRIBUTE VALUE")
            }         
      }
      
      var cid2: String = null

      "read the attribute in a parallel session" in {
         
         assertResp(route(app, connectionRequest("big_covar_schema")))
           .isOk
           .withConnStatusHeader(OPEN)
           .withBodyJson { json =>
               cid2 = (json \ "id").as[String]
               cid2 mustNot be (null)
               cid2 mustNot be (sid1)
         }
         
         val body: JsValue = Json.obj(
            "sid" -> sid1
         )
         assertResp(route(app, connectedRequest(GET, endpointSession, cid2).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson  { json =>
               extractAttr(json, "ATTRIBUTE NAME") mustBe Some("ATTRIBUTE VALUE")
            }         
      }
      
      "update the attribute in the parallel session" in {
     
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME",
            "value" -> "SOME OTHER VALUE"
         )
         assertResp(route(app, connectedRequest(PUT, endpointAttribute, cid2).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson  { json =>
               (json \ "returns").asOpt[String] mustBe Some("ATTRIBUTE VALUE")
               extractAttr(json, "ATTRIBUTE NAME") mustBe Some("SOME OTHER VALUE")
            }
      }
      
      "read the updated attribute in original session" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1
         )
         assertResp(route(app, connectedRequest(GET, endpointSession, cid1).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson  { json =>
               extractAttr(json, "ATTRIBUTE NAME") mustBe Some("SOME OTHER VALUE")
            }         
      }

      "refuse to read attribute in non-parallel session" in {
                  
         var cid3: String = null
         
         assertResp(route(app, connectionRequest("petclinic")))
           .isOk
           .withConnStatusHeader(OPEN)
           .withBodyJson { json =>
               cid3 = (json \ "id").as[String]
               cid3 mustNot be (null)
         }
         
         val body: JsValue = Json.obj(
            "sid" -> sid1
         )
         assertResp(route(app, connectedRequest(GET, endpointSession, cid3).withBody(body.toString())))
            .isError(SessionExpired, sid1)
            .withConnStatusHeader(OPEN)
      }

      "clear the updated attribute in original session" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME"
         )
         assertResp(route(app, connectedRequest(DELETE, endpointAttribute, cid1).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson  { json =>
               (json \ "returns").asOpt[String] mustBe Some("SOME OTHER VALUE")
               extractAttr(json, "ATTRIBUTE NAME") mustBe None
            }         
      }

      "Confirm that the attribute is gone" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1
         )
         assertResp(route(app, connectedRequest(GET, endpointSession, cid2).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
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
