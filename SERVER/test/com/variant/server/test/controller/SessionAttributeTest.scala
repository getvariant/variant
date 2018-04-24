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

      var cid: String = null 
      var sid: String = null
      
      "obtain a connection and a session" in {

         assertResp(route(app, connectionRequest("big_covar_schema")))
           .isOk
           .withConnStatusHeader(OPEN)
           .withBodyJson { json =>
               cid = (json \ "id").as[String]
               cid mustNot be (null)
         }
         
         sid = newSid
         val body = sessionJsonBigCovar.expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, endpoint, cid).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
      }

      "set an attribute first session" in {
         
         val body: JsValue = Json.obj(
            "sid" -> sid,
            "name" -> "ATTRIBUTE NAME",
            "value" -> "ATTRIBUTE VALUE"
         )
         
         assertResp(route(app, connectedRequest(PUT, endpoint + "/attr", cid).withBody(body.toString())))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
         
      }

   }
}
