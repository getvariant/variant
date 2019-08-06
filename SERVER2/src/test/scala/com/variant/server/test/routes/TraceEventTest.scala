package com.variant.server.test.routes

import com.variant.core.error.ServerError
import com.variant.server.api.StateRequest.Status._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.util.ParameterizedString

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.StatusCodes.MethodNotAllowed
import java.util.Optional
import play.api.libs.json.Json
import com.variant.server.test.util.TraceEventReader
import com.variant.core.Constants
import com.variant.server.test.spec.TraceEventsSpec

object TraceEventTest {

   val body = ParameterizedString("""
      {
          "name":"${name:NAME}",
          "attrs":{"Name One":"Value One", "Name Two":"Value Two"}
      }
   """.format(System.currentTimeMillis()))

   val bodyNoAttrs = ParameterizedString("""
      {
          "name":"${name:NAME}"
      }
   """.format(System.currentTimeMillis()))

   val bodyNoName = """
      {
          "attrs":{"k 1":"v 1", "k 2":"v 2"}
      }
   """

}

/**
 * Event Controller
 */
class TraceEventTest extends EmbeddedServerSpec with TraceEventsSpec {

   import SessionTest._
   import TraceEventTest._

   "Event Route" should {

      val schema = server.schemata.get("monstrosity").get.liveGen.get
      val eventWriter = schema.eventWriter
      eventWriter.maxDelayMillis mustEqual 2000
      val millisToSleep = eventWriter.maxDelayMillis + 500

      // New session
      var sid = newSid
      HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
         val ssnResp = SessionResponse(response)
         ssnResp.session.getId mustNot be(sid)
         ssnResp.schema.getMeta.getName mustBe "monstrosity"
         sid = ssnResp.session.getId
      }

      "return 400 and error on POST with no body" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/event/monstrosity/${sid}") ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.EmptyBody)
         }

      }

      "return  400 and error on POST with invalid JSON" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/event/monstrosity/${sid}", entity = "bad json") ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.JsonParseError, "Unrecognized token 'bad': was expecting ('true', 'false' or 'null')\n at [Source: (String)\"bad json\"; line: 1, column: 4]")
         }

      }

      "return 400 and error on POST with no name" in {
         HttpRequest(method = HttpMethods.POST, uri = s"/event/monstrosity/${sid}", entity = bodyNoName) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.MissingProperty, "name")
         }
      }

      "return 400 and error on POST with non-existent session" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/event/monstrosity/foo", entity = body.expand()) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.SESSION_EXPIRED, "foo")
         }
      }

      "flush a state visited event on request commit without attributes" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            ssnResp.session.getStateRequest mustBe Optional.empty
            sid = ssnResp.session.getId
         }

         // Target sesion for "state2"
         val reqBody1 = Json.obj(
            "state" -> "state4").toString

         // Target and get the request.
         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 4
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state4").get
         }

         // Commit without SVE attributes.
         val reqBody2 = Json.obj(
            "status" -> Committed.ordinal).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/monstrosity/${sid}", entity = reqBody2) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe Committed.ordinal
            stateReq.getLiveExperiences.size mustBe 4
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state4").get
         }

         // Read the event back from the db.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1

         val event = eventsFromDatabase.head
         event.name mustBe Constants.SVE_NAME
         event.attributes.size mustBe 2
         event.attributes("$STATE") mustBe "state4"
         event.attributes("$STATUS") mustBe "Committed"
         event.sessionId mustBe sid
         event.createdOn.toEpochMilli mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.eventExperiences.size mustBe 4
         // Test4 is not instrumented.
         event.eventExperiences.exists { _.testName == "test6" } mustBe false

      }

      "flush a state visited event on request commit with attributes" in {

         // New session
         var sid = newSid

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         // Target sesion for "state3"
         val reqBody1 = Json.obj(
            "state" -> "state3").toString

         // Target and get the request.
         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 5
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state3").get
         }

         // Commit with SVE attributes.
         val reqBody2 = Json.obj(
            "status" -> Committed.ordinal,
            "attrs" -> Map("key1" -> "val1", "key2" -> "val2", "key3" -> "val3")).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/monstrosity/${sid}", entity = reqBody2) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe Committed.ordinal
            stateReq.getLiveExperiences.size mustBe 5
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state3").get
         }

         // Read the event back from the db.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1

         val event = eventsFromDatabase.head
         event.name mustBe Constants.SVE_NAME
         event.attributes.size mustBe 5
         event.attributes("$STATE") mustBe "state3"
         event.attributes("$STATUS") mustBe "Committed"
         event.attributes("key1") mustBe "val1"
         event.attributes("key2") mustBe "val2"
         event.attributes("key3") mustBe "val3"
         event.sessionId mustBe sid
         event.createdOn.toEpochMilli mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.eventExperiences.size mustBe 5
         // Test4 is not instrumented.
         event.eventExperiences.exists { _.testName == "test4" } mustBe false

      }

      "Trigger custom event without a state request" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         val eventName = "Custom Name"
         val eventBody = body.expand("name" -> eventName)

         HttpRequest(method = HttpMethods.POST, uri = s"/event/monstrosity/${sid}", entity = eventBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe (sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
         }

         // Read the event back from the db.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head

         event.sessionId mustBe sid
         event.createdOn.toEpochMilli mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.name mustBe eventName
         event.eventExperiences mustBe empty
         event.attributes.size mustBe 2

      }

      "Trigger custom event with active state request and no attribubes" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            ssnResp.session.getStateRequest mustBe Optional.empty
            sid = ssnResp.session.getId
         }

         // Target and get the request.
         val reqBody1 = Json.obj(
            "state" -> "state5").toString

         // Target and get the request.
         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 4
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state5").get
         }

         // Custom event.
         val eventName = "Custom Name"
         val eventBody = bodyNoAttrs.expand("name" -> eventName)

         HttpRequest(method = HttpMethods.POST, uri = s"/event/monstrosity/${sid}", entity = eventBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe (sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
         }

         // Read events back from the db, but must wait for the asych flusher.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head

         event.sessionId mustBe sid
         event.createdOn.toEpochMilli mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.name mustBe eventName
         event.eventExperiences.size mustBe 4
         event.attributes mustBe empty
         event.eventExperiences.exists(_.testName == "test1") mustBe true
         event.eventExperiences.exists(_.testName == "test2") mustBe false
         event.eventExperiences.exists(_.testName == "test3") mustBe false
         event.eventExperiences.exists(_.testName == "test4") mustBe true
         event.eventExperiences.exists(_.testName == "test5") mustBe true
         event.eventExperiences.exists(_.testName == "test6") mustBe true
         event.eventExperiences.foreach(_.eventId mustBe event.id)

      }

      "Trigger custom event with committed state request and attribubes" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            ssnResp.session.getStateRequest mustBe Optional.empty
            sid = ssnResp.session.getId
         }

         // Target and get the request.
         val reqBody1 = Json.obj(
            "state" -> "state4").toString

         // Target and commit the request.
         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 4
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state4").get
         }

         val reqBody2 = Json.obj(
            "status" -> Committed.ordinal).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/monstrosity/${sid}", entity = reqBody2) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe Committed.ordinal
            stateReq.getLiveExperiences.size mustBe 4
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state4").get
         }

         // Custom event.
         val eventName = "Custom Name"
         val eventBody = body.expand("name" -> eventName)

         HttpRequest(method = HttpMethods.POST, uri = s"/event/monstrosity/${sid}", entity = eventBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe (sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
         }

         // Read events back from the db, but must wait for the asych flusher.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 2
         val event = eventsFromDatabase(0)

         event.sessionId mustBe sid
         event.createdOn.toEpochMilli mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.name mustBe eventName
         event.eventExperiences.size mustBe 4
         event.attributes.size mustBe 2
         event.eventExperiences.exists(_.testName == "test1") mustBe true
         event.eventExperiences.exists(_.testName == "test2") mustBe true
         event.eventExperiences.exists(_.testName == "test3") mustBe false
         event.eventExperiences.exists(_.testName == "test4") mustBe true
         event.eventExperiences.exists(_.testName == "test5") mustBe true
         event.eventExperiences.exists(_.testName == "test6") mustBe false
         event.eventExperiences.foreach(_.eventId mustBe event.id)

      }

      "respond MethodNotAllowed on everythig except POST" in {

         httpMethods.filterNot(List(HttpMethods.POST).contains _).foreach { method =>
            HttpRequest(method = method, uri = "/event/monstrosity/foo") ~> router ~> check {
               handled mustBe true
               status mustBe MethodNotAllowed
               contentType mustBe ContentTypes.`text/plain(UTF-8)`
               entityAs[String] mustBe "HTTP method not allowed, supported methods: POST"
            }
         }
      }
   }
}
