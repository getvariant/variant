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
      
   val endpoint = context + "/session"
   
   val sessionJsonBigCovar = ParameterizedString(
      sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
         
   val sessionJsonPetclinic = ParameterizedString(
         sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))

   "SessionController" should {

      var cid1: String = null 
      var sid1: String = null
      
      "obtain a connection and a session" in {

         assertResp(route(app, connectionRequest("big_covar_schema")))
           .isOk
           .withConnStatusHeader(OPEN)
           .withBodyJson { json =>
               cid1 = (json \ "id").as[String]
               cid1 mustNot be (null)
         }
         
         sid1 = newSid
         val body = sessionJsonBigCovar.expand("sid" -> sid1)
         assertResp(route(app, connectedRequest(PUT, endpoint, cid1).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
      }

      "set an attribute in first session" in {
         
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME",
            "value" -> "ATTRIBUTE VALUE"
         )
         
         assertResp(route(app, connectedRequest(PUT, endpoint + "/attr", cid1).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
         
      }

      "read the attribute in first session" in {
                  
         val body: JsValue = Json.obj(
            "sid" -> sid1,
            "name" -> "ATTRIBUTE NAME"
         )
         assertResp(route(app, connectedRequest(GET, endpoint + "/attr" , cid1).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
         
      }

   }
}
