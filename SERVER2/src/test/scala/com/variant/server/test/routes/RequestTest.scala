package com.variant.server.test.routes

import java.util.Optional

import scala.collection.JavaConverters._

import com.variant.server.api.StateRequest.Status._
import com.variant.core.error.ServerError._
import com.variant.core.session.CoreSession
import com.variant.core.session.CoreStateRequest
import com.variant.server.impl.SessionImpl
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.util.EventExperienceFromDatabase
import com.variant.server.test.util.TraceEventReader

import play.api.libs.json._

import com.variant.core.Constants
import com.variant.server.api.TraceEvent
import com.variant.server.impl.TraceEventImpl
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods

/**
 * Session Controller Tests
 */
class RequestTest extends EmbeddedServerSpec {

   import SessionTest._

   "Schema monstrosity" should {

      val schema = server.schemata.get("monstrosity").get.liveGen.get
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

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }
      }

      "create and commit new state request without SVE attributes" in {

         // Get existing session.
         HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}", entity = "junk to be ignored") ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            ssnResp.session.getStateRequest mustBe Optional.empty
         }

         // Target sesion for "state2"
         val reqBody1 = Json.obj(
            "state" -> "state2"
            ).toString

         // Target and get the request.
         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json =>
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe InProgress.ordinal
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().get.getStatus.ordinal mustBe InProgress.ordinal

         // Commit without SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString

         var stateReq: CoreStateRequest = null
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json =>
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe Committed.ordinal
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2").get
         }

         serverSsn.getStateRequest().get.getStatus.ordinal mustBe Committed.ordinal

         // Try committing again... Should work because we don't actually check for this on the server.
         // and trust that the client will check before sending the request and check again after receiving.
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk

         // Wait for event writer to flush and confirm we wrote 1 state visit event.
         Thread.sleep(2000)
         reader.read(e => e.sessionId == sid).size mustBe 1
         for (e <- reader.read(e => e.sessionId == sid)) {
            e.sessionId mustBe sid
            e.name mustBe Constants.SVE_NAME
            e.attributes.size mustBe 2
            e.attributes("$STATE") mustBe "state2"
            e.attributes("$STATUS") mustBe "Committed"
            e.eventExperiences.toSet[EventExperienceFromDatabase].map {x =>
               schema.getVariation(x.testName).get.getExperience(x.experienceName).get
               } mustBe stateReq.getLiveExperiences.asScala.toSet
         }

      }

      /*
      "create and commit new state request with SVE attributes" in {

         var sid = newSid

         // Recreate session because the old one expired while we waited for the event writer.
         assertResp(route(app, httpReq(POST, "/session/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "monstrosity"
               ssn.getStateRequest() mustBe Optional.empty
         }

         // Target sesion for "state3"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state3"
            ).toString

         // Target and get the request.
         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isOk
            .withBodySession { ssn =>
               val stateReq = ssn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe InProgress.ordinal
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().get.getStatus.ordinal mustBe InProgress.ordinal

         // Commit with SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString

         var stateReq: CoreStateRequest = null
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json =>
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe Committed.ordinal
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3").get
         }

         serverSsn.getStateRequest().get.getStatus.ordinal mustBe Committed.ordinal

         // Try committing again... Should work because we don't actually check for this on the server.
         // and trust that the client will check before sending the request and check again after receiving.
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk

         // Wait for event writer to flush and confirm we wrote 1 state visit event.
         Thread.sleep(2000)
         reader.read(e => e.sessionId == sid).size mustBe 1
         for (e <- reader.read(e => e.sessionId == sid)) {
            e.sessionId mustBe sid
            e.name mustBe Constants.SVE_NAME
            e.attributes.size mustBe 4
            e.attributes mustBe Map("$STATE"->"state3", "$STATUS" -> "Committed", "key1"->"val1", "key2"->"val2")
            e.eventExperiences.toSet[EventExperienceFromDatabase].map {x =>
               schema.getVariation(x.testName).get.getExperience(x.experienceName).get
               } mustBe stateReq.getLiveExperiences.asScala.toSet
         }

      }

      "refuse to fail after commit" in {

         var sid = newSid

         assertResp(route(app, httpReq(POST, "/session/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "monstrosity"
         }

         // Target sesion for "state4"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state4"
            ).toString

         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json =>
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe InProgress.ordinal
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().get.getStatus.ordinal mustBe InProgress.ordinal

         // Commit with SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString

         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json =>
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe Committed.ordinal
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4").get
         }

         serverSsn.getStateRequest().get.getStatus.ordinal mustBe Committed.ordinal

         // Try failing...
         val reqBody3 = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString

         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody3)))
            .isError(CANNOT_FAIL)

         // Only commit sve in written
         Thread.sleep(2000)
         val events = reader.read(e => e.sessionId == sid)
         events.size mustBe 1
         val sve = events.head
         sve.sessionId mustBe sid
         sve.name mustBe Constants.SVE_NAME
         sve.attributes.size mustBe 4
         sve.attributes mustBe Map("$STATE"->"state4", "$STATUS"->"Committed", "key1"->"val1", "key2"->"val2")

      }

      "refulse to commit a failed request" in {

         var sid = newSid

         assertResp(route(app, httpReq(POST, "/session/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "monstrosity"
         }

         // Target sesion for "state4"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state5"
            ).toString

         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json =>
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe InProgress.ordinal
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]
         serverSsn.getStateRequest().get.getStatus.ordinal mustBe InProgress.ordinal

         // Fail with SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString

         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json =>
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe Failed.ordinal
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5").get
         }

         serverSsn.getStateRequest.get.getStatus.ordinal mustBe Failed.ordinal

         // Attempt to commit.
         val reqBody3 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
         ).toString

         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody3)))
            .isError(CANNOT_COMMIT)

         // Only failure sve in written
         Thread.sleep(2000)
         val events = reader.read(e => e.sessionId == sid)
         events.size mustBe 1
         val sve = events.head
         sve.sessionId mustBe sid
         sve.name mustBe Constants.SVE_NAME
         sve.attributes.size mustBe 4
         sve.attributes mustBe Map("$STATE"->"state5", "$STATUS"->"Failed", "key1"->"val1", "key2"->"val2")

      }

      "refulse to undo failed status" in {

         var sid = newSid

         assertResp(route(app, httpReq(POST, "/session/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "monstrosity"
         }

         // Target sesion for "state5"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state5"
            ).toString

         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json =>
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe InProgress.ordinal
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5").get
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]
         serverSsn.getStateRequest().get.getStatus.ordinal mustBe InProgress.ordinal

         // Fail with SVE attributes.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
         ).toString

         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json =>
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe Failed.ordinal
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5").get
         }

         serverSsn.getStateRequest().get.getStatus.ordinal mustBe Failed.ordinal

         // Attempt to send invalid status
         val reqBody3 = Json.obj(
            "sid" -> sid,
            "status" -> InProgress.ordinal
         ).toString

         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody3)))
            .isError(InvalidRequestStatus, "InProgress")

         // Only failure sve in written
         Thread.sleep(2000)
         val events = reader.read(e => e.sessionId == sid)
         events.size mustBe 1
         val sve = events.head
         sve.sessionId mustBe sid
         sve.name mustBe Constants.SVE_NAME
         sve.attributes.size mustBe 4
         sve.attributes mustBe Map("$STATE"->"state5", "$STATUS"->"Failed", "key1"->"val1", "key2"->"val2")

      }

      "refuse to create a new state request on top of an existing one" in {

         var sid = newSid

         // create a new session.
         assertResp(route(app, httpReq(POST, "/session/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema.getMeta.getName mustBe "monstrosity"
         }

         // Target sesion for "state2"
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state2"
            ).toString

         // Target and get the request.
         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isOk
            .withBodySession { ssn =>
               val stateReq = ssn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe InProgress.ordinal
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2").get
            }

         // Target again and get the error.
         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isError(ACTIVE_REQUEST)

         // Commit the request.
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString

         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId must be (sid)
               ssn.getSchema.getMeta.getName mustBe "monstrosity"
               val stateReq = ssn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe Committed.ordinal
               stateReq.getLiveExperiences.size mustBe 5
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
         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody3)))
            .isOk
            .withBodySession { ssn =>
               val stateReq = ssn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus.ordinal mustBe InProgress.ordinal
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3").get
            }
      }
      *
      */
   }
}
