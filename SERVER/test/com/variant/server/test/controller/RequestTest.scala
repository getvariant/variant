package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.core.impl.ServerError._
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.session.CoreSession
import com.variant.server.test.util.EventReader
import com.variant.server.test.util.EventExperienceFromDatabase
import com.variant.core.impl.StateVisitedEvent
import com.variant.core.ConnectionStatus._
import com.variant.core.session.CoreStateRequest


/**
 * Session Controller Tests
 */
class RequestTest extends BaseSpecWithServer {
      
   
   "Schema big_conjoint_schema" should {

      val schema = server.schemata("big_conjoint_schema")
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

         assertResp(route(app, connectionRequest("big_conjoint_schema")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               cid = (json \ "id").as[String]
               cid mustNot be (null)
           }
      }

      "create new session" in {
         
         val body = SessionImpl.empty(sid, schema).toJson
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid).withTextBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
         
      }

      "create and commit new state request" in {

         assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, cid)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               val coreSsn1 = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn1.getStateRequest
               stateReq mustBe (null)
         }
         
         // Create state request object.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state2"
            ).toString

         // Target and get the request.
         assertResp(route(app, connectedRequest(POST, context + "/request", cid).withTextBody(reqBody1)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe false
               stateReq.getLiveExperiences.size mustBe 6
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2")
               stateReq.getStateVisitedEvent mustNot be (null)
            }

         // Commit the request.
         val reqBody2 = Json.obj(
            "sid" -> sid
            ).toString
            
         var stateReq: CoreStateRequest = null
         assertResp(route(app, connectedRequest(PUT, context + "/request", cid).withTextBody(reqBody2)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe true
               stateReq.getLiveExperiences.size mustBe 6
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2")
         }
         
         // Try committing again... Should work because we don't actually check for this on the server.
         // and trust that the client will check before sending the request and check again after receiving.
         assertResp(route(app, connectedRequest(PUT, context + "/request", cid).withTextBody(reqBody2)))
            .isOk
            .withConnStatusHeader(OPEN)

         // Wait for event writer to flush and confirm we wrote 1 state visit event.
         Thread.sleep(2000)
         reader.read(e => e.getSessionId == sid).size mustBe 1
         for (e <- reader.read(e => e.getSessionId == sid)) {
            e.getSessionId mustBe sid
            e.getName mustBe StateVisitedEvent.EVENT_NAME
            e.getValue mustBe "state2"
            (e.getEventExperiences.toSet[EventExperienceFromDatabase].map {x => schema.getTest(x.getTestName).getExperience(x.getExperienceName)} 
               mustBe stateReq.getLiveExperiences.toSet)
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

         assertResp(route(app, connectionRequest("petclinic")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               cid = (json \ "id").as[String]
               cid mustNot be (null)
         }
      }

      "create new session" in {
         
         val ssn = SessionImpl.empty(sid, schema)
         ssn.setAttribute("user-agent", "Safari")
         val reqBody = ssn.toJson

         assertResp(route(app, connectedRequest(PUT, context + "/session", cid).withTextBody(reqBody)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
      }

      "Disqualify session from test" in {

         assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, cid)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               val coreSsn1 = CoreSession.fromJson((json \ "session").as[String], schema)
               coreSsn1.getAttribute("user-agent") mustBe "Safari"
               coreSsn1.getStateRequest mustBe (null)
             } 
         
         // Create state request object.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "newOwner"
            ).toString

         // Target and get the request.
         assertResp(route(app, connectedRequest(POST, context + "/request", cid).withTextBody(reqBody1)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe false
               stateReq.getLiveExperiences.size mustBe 0  // We were disqualified by SafariDisqualHook
               coreSsn.getDisqualifiedTests.size mustBe 1
               coreSsn.getDisqualifiedTests.toSeq(0).getName mustBe "NewOwnerTest"         
               // Resolved parameter must always be from the state def because we're disqualified
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getResolvedParameters.get("path") mustBe schema.getState("newOwner").getParameters().get("path")
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("newOwner")
               stateReq.getStateVisitedEvent mustBe null
         }
         
         // Commit the request.
         val reqBody2 = Json.obj(
            "sid" -> sid
            ).toString
         assertResp(route(app, connectedRequest(PUT, context + "/request", cid).withTextBody(reqBody2)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe true
               stateReq.getLiveExperiences.size mustBe 0
               coreSsn.getDisqualifiedTests.size mustBe 1
               coreSsn.getDisqualifiedTests.toSeq(0).getName mustBe "NewOwnerTest"         
               // Resolved parameter must always be from the state def because we're disqualified
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getResolvedParameters.get("path") mustBe schema.getState("newOwner").getParameters().get("path")
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("newOwner")
               stateReq.getStateVisitedEvent mustBe null
            }
         
         // Send custom event.
         val eventBody = EventTest.body.expand("sid" -> sid, "name" -> "eventName", "value" -> "eventValue")
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, connectedRequest(POST, context + "/event", cid).withTextBody(eventBody)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody

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
