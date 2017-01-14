package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParamString
import com.variant.server.ConfigKeys
import com.variant.server.test.BaseSpecWithServer
import com.variant.core.exception.ServerError._
import com.variant.core.util.VariantStringUtils
import play.api.libs.json._
import com.variant.server.session.ServerSession
import com.variant.core.session.CoreSession


/**
 * Session Controller Tests
 */
class RequestController extends BaseSpecWithServer {
   
   val schemaId = server.schema.get.getId

   "RequestController" should {

      val sid = newSid
      var cid: String = null // Most recent conn ID

      "obtain a connection" in {
         // POST new connection
         val connResp = route(app, FakeRequest(POST, context + "/connection/big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         cid = (json \ "id").as[String]
         cid mustNot be (null)
      }

      "create new session" in {
         
         val reqBody = Json.obj(
            "cid" -> cid,
            "ssn" -> ServerSession.empty(sid).toJson
            ).toString
         val resp = route(app, FakeRequest(PUT, context + "/session").withTextBody(reqBody)).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
         
      }

      "create and commit new state request" in {

         // Get the session.
         var resp = route(app, FakeRequest(GET, context + "/session/" + scid(sid, cid))).get
         status(resp) mustBe OK
         var respAsJson = contentAsJson(resp)
         val coreSsn1 = CoreSession.fromJson((respAsJson \ "session").as[String], server.schema.get)
         val stateReq1 = coreSsn1.getStateRequest
         stateReq1 mustBe (null)
         
         // Create state request object.
         val reqBody = Json.obj(
            "sid" -> scid(sid, cid),
            "state" -> "state2"
            ).toString

         // Get the session.
         resp = route(app, FakeRequest(POST, context + "/request").withTextBody(reqBody)).get
         status(resp) mustBe OK
         respAsJson = contentAsJson(resp)
         val coreSsn2 = CoreSession.fromJson((respAsJson \ "session").as[String], server.schema.get)
         val stateReq2 = coreSsn2.getStateRequest
         stateReq2 mustNot be (null)
         stateReq2.isCommitted() mustBe false
         stateReq2.getLiveExperiences.size mustBe 6
         stateReq2.getResolvedParameters.size mustBe 1
         stateReq2.getSession.getId mustBe sid
         stateReq2.getState mustBe server.schema.get.getState("state2")

      }

   }
}
