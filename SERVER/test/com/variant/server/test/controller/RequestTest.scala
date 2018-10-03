package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.impl.ServerError._
import com.variant.core.StateRequestStatus._
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.session.CoreSession
import com.variant.core.StateRequestStatus._
import com.variant.server.test.util.TraceEventReader
import com.variant.server.test.util.EventExperienceFromDatabase
import com.variant.core.impl.StateVisitedEvent
import com.variant.core.session.CoreStateRequest
import com.variant.server.schema.ServerSchemaParser
import com.variant.core.TraceEvent
import com.variant.server.event.ServerTraceEvent
import com.variant.server.api.StateRequest
import java.util.Optional


/**
 * Session Controller Tests
 */
class RequestTest extends EmbeddedServerSpec {
      
   val emptyTargetingTrackerBody = "{\"tt\":[]}"

   
   "Schema big_conjoint_schema" should {

      val schema = server.schemata.get("big_conjoint_schema").get.liveGen.get
      val schemaId = schema.id
      val writer = schema.eventWriter
      val reader = TraceEventReader(writer)
      var sid = newSid
      
      "have expected event writer confuration" in {
         writer.maxBufferSize mustEqual 200
         writer.fullSize mustEqual 100
	      writer.maxDelayMillis mustEqual 2000

      }

      "create new session" in {
         
         // create a new session.
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk         
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "big_conjoint_schema"
         }
      }
      
      "create and commit new state request without SVE attributes" in {

         // Get existing session.
         assertResp(route(app, httpReq(GET, context + "/session/big_conjoint_schema/" + sid)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               coreSsn.getStateRequest mustBe Optional.empty
         }
 
         // Target sesion for "state2"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state2"
            ).toString
         
         // Target and get the request.
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 6
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().get.getStatus mustBe InProgress

         // Commit without SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString
           
         var stateReq: CoreStateRequest = null
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Committed
               stateReq.getLiveExperiences.size mustBe 6
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2").get
         }
         
         serverSsn.getStateRequest().get.getStatus mustBe Committed

         // Try committing again... Should work because we don't actually check for this on the server.
         // and trust that the client will check before sending the request and check again after receiving.
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk

         // Wait for event writer to flush and confirm we wrote 1 state visit event.
         Thread.sleep(2000)
         reader.read(e => e.sessionId == sid).size mustBe 1
         for (e <- reader.read(e => e.sessionId == sid)) {
            e.sessionId mustBe sid
            e.name mustBe TraceEvent.SVE_NAME
            e.attributes.size mustBe 2
            e.attributes("$STATE") mustBe "state2"
            e.attributes("$STATUS") mustBe "Committed"
            e.eventExperiences.toSet[EventExperienceFromDatabase].map {x => 
               schema.getVariation(x.testName).get.getExperience(x.experienceName).get
               } mustBe stateReq.getLiveExperiences.toSet
         }
         
      }
      
      "create and commit new state request with SVE attributes" in {

         var sid = newSid
         
         // Recreate session because the old one expired while we waited for the event writer.
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "big_conjoint_schema"
               ssn.getStateRequest() mustBe Optional.empty
         }
 
         // Target sesion for "state3"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state3"
            ).toString
         
         // Target and get the request.
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodySession { ssn => 
               val stateReq = ssn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().get.getStatus mustBe InProgress

         // Commit with SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString
           
         var stateReq: CoreStateRequest = null
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Committed
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3").get
         }
         
         serverSsn.getStateRequest().get.getStatus mustBe Committed

         // Try committing again... Should work because we don't actually check for this on the server.
         // and trust that the client will check before sending the request and check again after receiving.
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
         
         // Wait for event writer to flush and confirm we wrote 1 state visit event.
         Thread.sleep(2000)
         reader.read(e => e.sessionId == sid).size mustBe 1
         for (e <- reader.read(e => e.sessionId == sid)) {
            e.sessionId mustBe sid
            e.name mustBe TraceEvent.SVE_NAME
            e.attributes.size mustBe 4
            e.attributes mustBe Map("$STATE"->"state3", "$STATUS" -> "Committed", "key1"->"val1", "key2"->"val2")
            e.eventExperiences.toSet[EventExperienceFromDatabase].map {x => 
               schema.getVariation(x.testName).get.getExperience(x.experienceName).get
               } mustBe stateReq.getLiveExperiences.toSet
         }
         
      }
      
      "refuse to fail after commit" in {

         var sid = newSid
         
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "big_conjoint_schema"
         }
 
         // Target sesion for "state4"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state4"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().get.getStatus mustBe InProgress

         // Commit with SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString
           
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Committed
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4").get
         }
         
         serverSsn.getStateRequest().get.getStatus mustBe Committed

         // Try failing...
         val reqBody3 = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString

         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody3)))
            .isError(CANNOT_FAIL)
            
         // Only commit sve in written
         Thread.sleep(2000)
         val events = reader.read(e => e.sessionId == sid)
         events.size mustBe 1
         val sve = events.head
         sve.sessionId mustBe sid
         sve.name mustBe TraceEvent.SVE_NAME
         sve.attributes.size mustBe 4
         sve.attributes mustBe Map("$STATE"->"state4", "$STATUS"->"Committed", "key1"->"val1", "key2"->"val2")

      }

      "refulse to commit a failed request" in {

         var sid = newSid
         
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "big_conjoint_schema"
         }
 
         // Target sesion for "state4"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state5"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]
         serverSsn.getStateRequest().get.getStatus mustBe InProgress

         // Fail with SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString
           
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Failed
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5").get
         }
         
         serverSsn.getStateRequest.get.getStatus mustBe Failed

         // Attempt to commit.
         val reqBody3 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
         ).toString
           
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody3)))
            .isError(CANNOT_COMMIT)

         // Only failure sve in written
         Thread.sleep(2000)
         val events = reader.read(e => e.sessionId == sid)
         events.size mustBe 1
         val sve = events.head
         sve.sessionId mustBe sid
         sve.name mustBe TraceEvent.SVE_NAME
         sve.attributes.size mustBe 4
         sve.attributes mustBe Map("$STATE"->"state5", "$STATUS"->"Failed", "key1"->"val1", "key2"->"val2")

      }
      
      "refulse to undo failed status" in {

         var sid = newSid
         
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "big_conjoint_schema"
         }
 
         // Target sesion for "state5"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state5"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]
         serverSsn.getStateRequest().get.getStatus mustBe InProgress

         // Fail with SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString
           
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Failed
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5").get
         }
         
         serverSsn.getStateRequest().get.getStatus mustBe Failed

         // Attempt to send invalid status 
         val reqBody3 = Json.obj(
            "sid" -> sid,
            "status" -> InProgress.ordinal
         ).toString
           
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody3)))
            .isError(InvalidRequestStatus, "InProgress")

         // Only failure sve in written
         Thread.sleep(2000)
         val events = reader.read(e => e.sessionId == sid)
         events.size mustBe 1
         val sve = events.head
         sve.sessionId mustBe sid
         sve.name mustBe TraceEvent.SVE_NAME
         sve.attributes.size mustBe 4
         sve.attributes mustBe Map("$STATE"->"state5", "$STATUS"->"Failed", "key1"->"val1", "key2"->"val2")

      }

      "refuse to create a new state request on top of an existing one" in {

         var sid = newSid
         
         // create a new session.
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "big_conjoint_schema"
         }
   
         // Target sesion for "state2"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state2"
            ).toString

         // Target and get the request.
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodySession { ssn => 
               val stateReq = ssn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 6
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2").get
            }

         // Target again and get the error.
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isError(ACTIVE_REQUEST)

         // Commit the request.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString

         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId must be (sid)
               ssn.getSchema.getMeta.getName mustBe "big_conjoint_schema"
               val stateReq = ssn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Committed
               stateReq.getLiveExperiences.size mustBe 6
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2").get
         }

         // Can target again.
         // Target sesion for "state3"
         val reqBody3 = Json.obj(
            "sid" -> sid,
            "state" -> "state3"
            ).toString

         // Target and get the request.
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody3)))
            .isOk
            .withBodySession { ssn => 
               val stateReq = ssn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3").get
            }

      }
   }

   "Schema petclinic" should {

      val schema = server.schemata.get("petclinic").get.liveGen.get
      val schemaId = schema.id
      val writer = schema.eventWriter
      val reader = TraceEventReader(writer)
      var sid = newSid

      "create new session" in {
         
         // ssn.setAttribute("user-agent", "Safari")

         assertResp(route(app, httpReq(POST, context + "/session/petclinic/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "petclinic"
         }
      }

      "set an session attribute" in {
         
         val body: JsValue = Json.obj(
            "sid" -> sid,
            "name" -> "user-agent",
            "value" -> "Safari"     // this will cause disqualification
         )
         assertResp(route(app, httpReq(PUT, context + "/session/attr").withBody(body.toString())))
            .isOk
      }

      "disqualify session from test" in {

         assertResp(route(app, httpReq(POST, context + "/session/petclinic/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               coreSsn.getAttributes.get("user-agent") mustBe "Safari"
               coreSsn.getStateRequest mustBe Optional.empty
             } 
         
         // State request object.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "newOwner"
            ).toString

         // Target and get the request.
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 0  // We were disqualified by SafariDisqualHook
               coreSsn.getDisqualifiedVariations.size mustBe 1
               coreSsn.getDisqualifiedVariations.toSeq(0).getName mustBe "NewOwnerTest"         
               // Resolved parameter must always be from the state def because we're disqualified
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getResolvedParameters.get("path") mustBe schema.getState("newOwner").get.getParameters.get("path")
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("newOwner").get
         }
         
         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]
         serverSsn.triggerEvent(new ServerTraceEvent("Custom Event", Map("foo"->"bar")));
         
         // Commit the request.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString
        
         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Committed
               stateReq.getLiveExperiences.size mustBe 0
               coreSsn.getDisqualifiedVariations.size mustBe 1
               coreSsn.getDisqualifiedVariations.toSeq(0).getName mustBe "NewOwnerTest"         
               // Resolved parameter must always be from the state def because we're disqualified
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getResolvedParameters.get("path") mustBe schema.getState("newOwner").get.getParameters.get("path")
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("newOwner").get
            }
         
         // Send custom event.
         val eventBody = TraceEventTest.body.expand("sid" -> sid, "name" -> "eventName")
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, httpReq(POST, context + "/event").withTextBody(eventBody)))
            .isOk
            .withNoBody

         // Confirm that the SVE and the custom events are both orphans.
         Thread.sleep(2000)
         val flushedEvents = reader.read(e => e.sessionId == sid)
         flushedEvents.size mustBe 2
         flushedEvents.exists { _.eventExperiences.size > 0 } mustBe false
      }
   }
}
