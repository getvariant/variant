package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.api.ConfigKeys
import com.variant.server.test.BaseSpecWithServer
import com.variant.core.ServerError._
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.session.CoreSession
import com.variant.server.test.util.EventReader
import com.variant.server.test.util.EventExperienceFromDatabase
import com.variant.core.impl.StateVisitedEvent


/**
 * Session Controller Tests
 */
class RequestTest extends BaseSpecWithServer {
      
   
   "Schema big_covar_schema" should {

      val schema = server.schemata("big_covar_schema")
      val schemaId = schema.getId
      val writer = schema.eventWriter
      val reader = EventReader(writer)
      val sid = newSid
      var cid: String = null

      
      "have expected event writer confuration" in {
         writer.maxBufferSize mustEqual 200
         writer.fullSize mustEqual 100
	      writer.maxDelayMillis mustEqual 2000

      }

      "obtain a connection" in {
         // Open new connection
         val connResp = route(app, connectionRequest("big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         cid = (json \ "id").as[String]
         cid mustNot be (null)
      }

      "create new session" in {
         
         val body = SessionImpl.empty(sid, schema).toJson
         val resp = route(app, connectedRequest(PUT, context + "/session", cid).withTextBody(body)).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
         
      }

      "create and commit new state request" in {

         // Get the session.
         var resp = route(app, connectedRequest(GET, context + "/session/" + sid, cid)).get
         status(resp) mustBe OK
         var respAsJson = contentAsJson(resp)
         val coreSsn1 = CoreSession.fromJson((respAsJson \ "session").as[String], schema)
         val stateReq1 = coreSsn1.getStateRequest
         stateReq1 mustBe (null)
         
         // Create state request object.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state2"
            ).toString

         // Target and get the request.
         resp = route(app, connectedRequest(POST, context + "/request", cid).withTextBody(reqBody1)).get
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
         stateReq2.getStateVisitedEvent mustNot be (null)


         // Commit the request.
         val reqBody2 = Json.obj(
            "sid" -> sid
            ).toString
            
         resp = route(app, connectedRequest(PUT, context + "/request", cid).withTextBody(reqBody2)).get
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
 
         // Try committing again... Should work because we don't actually check for this on the server.
         // and trust that the client will check before sending the request and check again after receiving.
         resp = route(app, connectedRequest(PUT, context + "/request", cid).withTextBody(reqBody2)).get
         status(resp) mustBe OK

         // Wait for event writer to flush and confirm we wrote 1 state visit event.
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
         
         // should not have produced a new event, i.e. still 1.
         Thread.sleep(2000)
         reader.read(e => e.getSessionId == sid).size mustBe 1
      }

   }

   "Schema petclinic" should {

      val schema = server.schemata("petclinic")
      val schemaId = schema.getId
      val writer = schema.eventWriter
      val reader = EventReader(writer)
      val sid = newSid
      var cid: String = null

      "obtain a connection" in {
         // POST new connection
         val connResp = route(app, connectionRequest("petclinic")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         cid = (json \ "id").as[String]
         cid mustNot be (null)
      }

      "create new session" in {
         
         val ssn = SessionImpl.empty(sid, schema)
         ssn.setAttribute("user-agent", "Firefox")
         val reqBody = ssn.toJson

         val resp = route(app, connectedRequest(PUT, context + "/session", cid).withTextBody(reqBody)).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
      }

      "Disqualify session from test" in {

         // Get the session.
         var resp = route(app, connectedRequest(GET, context + "/session/" + sid, cid)).get
         status(resp) mustBe OK
         var respAsJson = contentAsJson(resp)
         val coreSsn1 = CoreSession.fromJson((respAsJson \ "session").as[String], schema)
         coreSsn1.getAttribute("user-agent") mustBe "Firefox"
         coreSsn1.getStateRequest mustBe (null)
         
         // Create state request object.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "newOwner"
            ).toString

         // Target and get the request.
         resp = route(app, connectedRequest(POST, context + "/request", cid).withTextBody(reqBody1)).get
         status(resp) mustBe OK
         respAsJson = contentAsJson(resp)
         val coreSsn2 = CoreSession.fromJson((respAsJson \ "session").as[String], schema)
         val stateReq2 = coreSsn2.getStateRequest
         stateReq2 mustNot be (null)
         stateReq2.isCommitted() mustBe false
         stateReq2.getLiveExperiences.size mustBe 0
         coreSsn2.getDisqualifiedTests.size mustBe 1
         coreSsn2.getDisqualifiedTests.toSeq(0).getName mustBe "NewOwnerTest"         
         // Resolved parameter must always be from the state def because we're disqualified
         stateReq2.getResolvedParameters.size mustBe 1
         stateReq2.getResolvedParameters.get("path") mustBe schema.getState("newOwner").getParameters().get("path")
         stateReq2.getSession.getId mustBe sid
         stateReq2.getState mustBe schema.getState("newOwner")
         stateReq2.getStateVisitedEvent mustBe null

         // Commit the request.
         val reqBody2 = Json.obj(
            "sid" -> sid
            ).toString
         resp = route(app, connectedRequest(PUT, context + "/request", cid).withTextBody(reqBody2)).get
         status(resp) mustBe OK
         respAsJson = contentAsJson(resp)
         val coreSsn3 = CoreSession.fromJson((respAsJson \ "session").as[String], schema)
         val stateReq3 = coreSsn3.getStateRequest
         stateReq3 mustNot be (null)
         stateReq2.isCommitted() mustBe false
         stateReq2.getLiveExperiences.size mustBe 0
         coreSsn2.getDisqualifiedTests.size mustBe 1
         coreSsn2.getDisqualifiedTests.toSeq(0).getName mustBe "NewOwnerTest"         
         // Resolved parameter must always be from the state def because we're disqualified
         stateReq2.getResolvedParameters.size mustBe 1
         stateReq2.getResolvedParameters.get("path") mustBe schema.getState("newOwner").getParameters().get("path")
         stateReq2.getSession.getId mustBe sid
         stateReq2.getState mustBe schema.getState("newOwner")
         stateReq2.getStateVisitedEvent mustBe null

         // Send custom event.
         val eventBody = EventTest.body.expand("sid" -> sid, "name" -> "eventName", "value" -> "eventValue")
         val eventResp = route(app, connectedRequest(POST, context + "/event", cid).withTextBody(eventBody)).get
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         status(eventResp) mustBe OK
         contentAsString(eventResp) mustBe empty

         // Wait for event writer to flush and confirm all event were discarded.
         Thread.sleep(2000)
         val flushedEvents = reader.read(e => e.getSessionId == sid)
         if (flushedEvents.size > 0) {
            println("*** These are not expected: ***")
            flushedEvents.foreach(println(_))
         }
         flushedEvents.size mustBe 0
      }
   }
}
