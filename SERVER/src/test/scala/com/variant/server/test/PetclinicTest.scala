package com.variant.server.test

import scala.collection.JavaConverters._

import com.variant.share.Constants._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TraceEventsSpec
import com.variant.server.api.StateRequest.Status._

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.server.impl.TraceEventImpl
import com.variant.server.test.routes.TraceEventTest
import com.variant.share.Constants

/**
 * Petclinic demo app test.
 */
class PetclinicTest extends EmbeddedServerSpec with TraceEventsSpec with  Constants {
 
    val emptyTargetingTrackerBody = "{\"tt\":[]}"

   "Disqualified user session" should {
 
      val schema = server.schemata.get("petclinic").get.liveGen.get
      val schemaId = schema.id
      var sid = newSid
 
      val maxDelayMillis = server.config.eventWriterMaxDelay * 1000
         
      "have expected confuration" in {
         maxDelayMillis mustBe 2000
      }

      "create new session" in {
  
         HttpRequest(method = HttpMethods.POST, uri = s"/session/petclinic/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            sid = ssnResp.session.getId
         }

      }

      "set a session attribute" in {
 
         val body: JsValue = Json.obj(
            "attrs" -> Map(
               "user" -> "Nikita Krushchev",  // this will cause disqualification
	            "foo" -> "bar"
             )
         )
         
         HttpRequest(method = HttpMethods.PUT, uri = s"/session-attr/petclinic/${sid}", entity = body.toString()) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            ssnResp.session.getAttributes.asScala mustBe Map("foo" -> "bar", "user" -> "Nikita Krushchev")
            ssnResp.session.getDisqualifiedVariations.size mustBe 0
         }
      }

      "disqualify session from test" in {
 
         // State request object.
         val reqBody1 = Json.obj(
            "state" -> "vets"
         ).toString
 
         // Target and get the request.
         HttpRequest(method = HttpMethods.POST, uri = s"/request/petclinic/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.session.getDisqualifiedVariations.size mustBe 1
            ssnResp.session.getAttributes.asScala mustBe Map("foo" -> "bar", "user" -> "Nikita Krushchev")
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 1
            stateReq.getResolvedParameters.size mustBe 0
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("vets").get
         }
      }
      
      "commit the state request" in {

         // Commit the request.
         val reqBody = Json.obj(
               "status" -> Committed.ordinal
               ).toString
 
         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/petclinic/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.session.getDisqualifiedVariations.size mustBe 1
            ssnResp.session.getAttributes.asScala mustBe Map("foo" -> "bar", "user" -> "Nikita Krushchev")
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe Committed.ordinal
            stateReq.getLiveExperiences.size mustBe 1
            stateReq.getResolvedParameters.size mustBe 0
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("vets").get
         }
      }


      "trigger custom trace event" in {

         val eventBody = TraceEventTest.body.expand("name" -> "petclinic custom event")
         HttpRequest(method = HttpMethods.POST, uri = s"/event/petclinic/${sid}", entity = eventBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe (sid)
            ssnResp.schema.getMeta.getName mustBe "petclinic"
         }
      }

      "confirm that 2 events been written" in {
         Thread.sleep(maxDelayMillis + 2000)
         val flushedEvents = eventReader.read(e => e.sessionId == sid)
         flushedEvents.size mustBe 2

         flushedEvents(0).name mustBe SVE_NAME
         flushedEvents(0).attributes.mkString(", ") mustBe "$STATE -> vets, $STATUS -> Committed"
         flushedEvents(0).eventExperiences.size mustBe 1
         flushedEvents(0).eventExperiences.toSeq(0).variationName mustBe "VetsHourlyRateFeature"
         
         flushedEvents(1).name mustBe "petclinic custom event"
         flushedEvents(1).attributes.mkString(", ") mustBe "Name One -> Value One, Name Two -> Value Two"
         flushedEvents(1).eventExperiences.size mustBe 1
         flushedEvents(1).eventExperiences.toSeq(0).variationName mustBe "VetsHourlyRateFeature"
      }
   }

   "Qualified user session" should {
 
      val schema = server.schemata.get("petclinic").get.liveGen.get
      val schemaId = schema.id
      var sid = newSid
 
      val maxDelayMillis = server.config.eventWriterMaxDelay * 1000
         
      "have expected confuration" in {
         maxDelayMillis mustBe 2000
      }

      "create new session" in {
  
         HttpRequest(method = HttpMethods.POST, uri = s"/session/petclinic/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            sid = ssnResp.session.getId
         }

      }
      
       "set a session attribute" in {
 
         val body: JsValue = Json.obj(
            "attrs" -> Map(
               "user" -> "Igor Urisman"  // this will not cause disqualification
             )
         )
         
         HttpRequest(method = HttpMethods.PUT, uri = s"/session-attr/petclinic/${sid}", entity = body.toString()) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            ssnResp.session.getAttributes.asScala mustBe Map("user" -> "Igor Urisman")
            ssnResp.session.getDisqualifiedVariations.size mustBe 0
         }
      }

      "target session for test" in {
 
         // State request object.
         val reqBody1 = Json.obj(
            "state" -> "vets"
         ).toString
 
         // Target and get the request.
         HttpRequest(method = HttpMethods.POST, uri = s"/request/petclinic/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.session.getDisqualifiedVariations.size mustBe 0
            ssnResp.session.getAttributes.asScala mustBe Map("user" -> "Igor Urisman")
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 2
            stateReq.getResolvedParameters.size mustBe 0
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("vets").get
         }
      }
      
      "commit the state request" in {

         // Commit the request.
         val reqBody = Json.obj(
               "status" -> Committed.ordinal
               ).toString
 
         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/petclinic/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.session.getDisqualifiedVariations.size mustBe 0
            ssnResp.session.getAttributes.asScala mustBe Map("user" -> "Igor Urisman")
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe Committed.ordinal
            stateReq.getLiveExperiences.size mustBe 2
            stateReq.getResolvedParameters.size mustBe 0
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("vets").get
         }
      }


      "trigger custom trace event" in {

         val eventBody = TraceEventTest.body.expand("name" -> "petclinic custom event")
         HttpRequest(method = HttpMethods.POST, uri = s"/event/petclinic/${sid}", entity = eventBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe (sid)
            ssnResp.schema.getMeta.getName mustBe "petclinic"
         }
      }

      "confirm that no events been written" in {
         Thread.sleep(maxDelayMillis + 2000)
         val flushedEvents = eventReader.read(e => e.sessionId == sid)
         flushedEvents.size mustBe 2
         flushedEvents(0).name mustBe SVE_NAME
         flushedEvents(0).attributes.mkString(", ") mustBe "$STATE -> vets, $STATUS -> Committed"
         flushedEvents(0).eventExperiences.size mustBe 2
         flushedEvents(0).eventExperiences.map(_.variationName) mustBe Set("VetsHourlyRateFeature", "ScheduleVisitTest")

      }
   }
}
