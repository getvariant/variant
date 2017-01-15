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
import com.variant.server.test.util.EventReader
import com.variant.server.test.util.EventExperienceFromDatabase
import com.variant.core.impl.StateVisitedEvent


/**
 * Session Controller Tests
 */
class RequestTest extends BaseSpecWithServer {
   
   val schema = server.schema.get
   val schemaId = schema.getId
   val writer = server.eventWriter
   val reader = EventReader(writer)

   "Event writer" should {

      "have expected confuration" in {
         writer.bufferSize mustEqual 200
         writer.pctFullSize mustEqual 100
	      writer.maxDelayMillis mustEqual 2000

      }
   }
   
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
         val coreSsn1 = CoreSession.fromJson((respAsJson \ "session").as[String], schema)
         val stateReq1 = coreSsn1.getStateRequest
         stateReq1 mustBe (null)
         
         // Create state request object.
         val reqBody1 = Json.obj(
            "sid" -> scid(sid, cid),
            "state" -> "state2"
            ).toString

         // Target and get the request.
         resp = route(app, FakeRequest(POST, context + "/request").withTextBody(reqBody1)).get
         status(resp) mustBe OK
         respAsJson = contentAsJson(resp)
         val coreSsn2 = CoreSession.fromJson((respAsJson \ "session").as[String], schema)
         val stateReq2 = coreSsn2.getStateRequest
         stateReq2 mustNot be (null)
         stateReq2.isCommitted() mustBe false
         stateReq2.getLiveExperiences.size mustBe 6
         stateReq2.getResolvedParameters.size mustBe 1
         stateReq2.getSession.getId mustBe sid
         stateReq2.getState mustBe schema.getState("state2")

         // Commit the request.
         val reqBody2 = Json.obj(
            "cid" -> cid,
            "ssn" -> coreSsn2.toJson
            ).toString         
         resp = route(app, FakeRequest(DELETE, context + "/request").withTextBody(reqBody2)).get
         status(resp) mustBe OK
         respAsJson = contentAsJson(resp)
         val coreSsn3 = CoreSession.fromJson((respAsJson \ "session").as[String], schema)
         val stateReq3 = coreSsn3.getStateRequest
         stateReq3 mustNot be (null)
         stateReq3.isCommitted() mustBe true
         stateReq3.getLiveExperiences.size mustBe 6
         stateReq3.getResolvedParameters.size mustBe 1
         stateReq3.getSession.getId mustBe sid
         stateReq3.getState mustBe schema.getState("state2")
 
         // Wait for event writer to flush and confirm we wrote the state visit event.
         Thread.sleep(2000)
         reader.read(e => e.getSessionId == sid).size mustBe 1
         for (e <- reader.read(e => e.getSessionId == sid)) {
            e.getSessionId mustBe sid
            e.getName mustBe StateVisitedEvent.EVENT_NAME
            e.getValue mustBe "state2"
            e.getCreatedOn.getTime mustBe stateReq3.createDate().getTime +- 100
            (e.getEventExperiences.toSet[EventExperienceFromDatabase].map {x => schema.getTest(x.getTestName).getExperience(x.getExperienceName)} 
               mustBe stateReq3.getLiveExperiences.toSet)
         }
         
         // Try committing again...
         val reqBody3 = Json.obj(
            "cid" -> cid,
            "ssn" -> coreSsn3.toJson
            ).toString         
         resp = route(app, FakeRequest(DELETE, context + "/request").withTextBody(reqBody3)).get
         status(resp) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(resp))
         isInternal mustBe StateRequestAlreadyCommitted.isInternal() 
         error mustBe StateRequestAlreadyCommitted
         args mustBe (empty)

         Thread.sleep(2000)
         reader.read(e => e.getSessionId == sid).size mustBe 1
         
      }

   }
}
