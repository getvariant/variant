package com.variant.server.test.controller

import scala.util.Random
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.collection.JavaConversions._
import com.variant.core.impl.ServerError._
import com.variant.core.util.Constants._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.util.TraceEventReader
import com.variant.server.test.spec.EmbeddedServerSpec
import javax.inject.Inject
import com.variant.server.api.Session
import org.scalatest.TestData
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import com.variant.server.play.VariantApplicationLoader
import play.api.Configuration
import com.variant.core.session.CoreSession
import com.variant.server.impl.SessionImpl
import java.util.Date

object TraceEventTest {
   
   val body = ParameterizedString("""
      {
         "sid":"${sid:SID}",
            "event": {
               "name":"${name:NAME}",
               "value":"${value:VALUE}",
               "attrList":[{"Name One":"Value One"},{"Name Two":"Value Two"}]
            }
      }
   """.format(System.currentTimeMillis()))
   
   val bodyNoAttrs = ParameterizedString("""
      {
         "sid":"${sid:SID}",
            "event": {
               "name":"${name:NAME}",
               "value":"${value:VALUE}"
            }
      }
   """.format(System.currentTimeMillis()))

   val bodyNoSid = """{"name":"NAME","value":"VALUE"}"""

   val bodyNoName = """
      {
         "sid":"SID",
         "event": {
            "value":"VALUE"
         }
      }
   """

}

/**
 * Event Controller
 */
class TraceEventTest extends EmbeddedServerSpec {
   
   import TraceEventTest._
   
   val endpoint = context + "/event"
      
   "EventController" should {

      val schema = server.schemata.get("big_conjoint_schema").get.liveGen.get
      val eventWriter = schema.eventWriter

      "return 404 on GET" in {
         assertResp(route(app, FakeRequest(GET, endpoint)))
            .is(NOT_FOUND)
            .withNoBody
      }

      "return 404 on PUT" in {
         assertResp(route(app, FakeRequest(PUT, endpoint)))
            .is(NOT_FOUND)
            .withNoBody
      }
      
      "return 400 and error on POST with no body" in {
         assertResp(route(app, httpReq(POST, endpoint)))
            .isError(EmptyBody)
     }

      "return  400 and error on POST with invalid JSON" in {
         assertResp(route(app, httpReq(POST, endpoint).withBody("bad json")))
            .isError(JsonParseError, "Unrecognized token 'bad': was expecting ('true', 'false' or 'null') at [Source: bad json; line: 1, column: 4]")
     }

      "return  400 and error on POST with no sid" in {
         assertResp(route(app, httpReq(POST, endpoint).withBody(bodyNoSid)))
            .isError(MissingProperty, "sid")
      }

      "return 400 and error on POST with no name" in {
         assertResp(route(app, httpReq(POST, endpoint).withBody(bodyNoName)))
            .isError(MissingProperty, "name")
      }

      "return  400 and error on POST with non-existent session" in {
         
         val eventBody = body.expand("sid" -> "foo")
         assertResp(route(app, httpReq(POST, endpoint).withBody(eventBody)))
            .isError(SESSION_EXPIRED, "foo")
      }

      "flush a state visited event on request commit without attributes" in {

         // New session
         val sid = newSid

         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk         
            .withBodyJsonSession(sid, "big_conjoint_schema")

         // Target and get the request.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state4"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe false
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4")
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().isCommitted() mustBe false

         // Commit request body with attributes
         val reqBody2 = Json.obj(
            "sid" -> sid
            ).toString

         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe true
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4")
         }
         
         // Read the event back from the db.
         eventWriter.maxDelayMillis  mustEqual 2000
         val millisToSleep = eventWriter.maxDelayMillis + 500
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         
         val event = eventsFromDatabase.head
         event.name mustBe "$STATE_VISIT"
         event.value mustBe "state4"
         event.sessionId mustBe sid
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisToSleep) +- 100   
         event.eventExperiences.size() mustBe 5
         // Test4 is not instrumented.
         event.eventExperiences.exists {_.testName == "test6"} mustBe false

      }

      "flush a state visited event on request commit with attributes" in {

         // New session
         val sid = newSid

         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk         
            .withBodyJsonSession(sid, "big_conjoint_schema")

         // Target and get the request.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state3"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe false
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3")
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().isCommitted() mustBe false

         // Commit request body with attributes
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "attrList" -> Map("key1"->"val1", "key2"->"val2", "key3"->"val3")
            ).toString

         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe true
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3")
         }
         
         // Read the event back from the db.
         eventWriter.maxDelayMillis  mustEqual 2000
         val millisToSleep = eventWriter.maxDelayMillis + 500
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         
         val event = eventsFromDatabase.head
         event.name mustBe "$STATE_VISIT"
         event.value mustBe "state3"
         event.sessionId mustBe sid
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisToSleep) +- 100   
         event.eventExperiences.size() mustBe 5
         // Test4 is not instrumented.
         event.eventExperiences.exists {_.testName == "test4"} mustBe false
         event.attributes.size mustBe 3
         event.attributes("key1") mustBe "val1"
         event.attributes("key1") mustBe "val1"
         event.attributes("key1") mustBe "val1"

      }

      "Refuse to trigger custom event without a state request" in {

         // New session
         val sid = newSid

         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk         
            .withBodyJsonSession(sid, "big_conjoint_schema")

         val eventName = "Custom Name"
         val eventValue = "Custom Value"
         val eventBody = body.expand("sid" -> sid, "name" -> eventName, "value" -> eventValue)
         
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, httpReq(POST, endpoint).withBody(eventBody)))
            .isError(UNKNOWN_STATE)
      }
      
      "Trigger custom event with active state request and no attribubes" in {

         // New session
         val sid = newSid

         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk         
            .withBodyJsonSession(sid, "big_conjoint_schema")

         // Target and get the request.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state5"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe false
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5")
            }

         // Custom event.
         val eventName = "Custom Name"
         val eventValue = "Custom Value"
         val eventBody = bodyNoAttrs.expand("sid" -> sid, "name" -> eventName, "value" -> eventValue)
         
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, httpReq(POST, endpoint).withBody(eventBody)))
            .isOk
            .withNoBody
         
         // Read events back from the db, but must wait for the asych flusher.
         eventWriter.maxDelayMillis  mustEqual 2000
         val millisToSleep = eventWriter.maxDelayMillis + 500
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head

         event.sessionId mustBe sid
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.name mustBe eventName
         event.value mustBe eventValue
         event.eventExperiences.size() mustBe 4
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
         val sid = newSid

         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk         
            .withBodyJsonSession(sid, "big_conjoint_schema")

         // Target and get the request.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state4"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest
               stateReq mustNot be (null)
               stateReq.isCommitted() mustBe false
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4")
            }

         // Custom event.
         val eventName = "Custom Name"
         val eventValue = "Custom Value"
         val eventBody = body.expand("sid" -> sid, "name" -> eventName, "value" -> eventValue)
         
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, httpReq(POST, endpoint).withBody(eventBody)))
            .isOk
            .withNoBody
         
         // Read events back from the db, but must wait for the asych flusher.
         eventWriter.maxDelayMillis  mustEqual 2000
         val millisToSleep = eventWriter.maxDelayMillis + 500
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head
         
         event.sessionId mustBe sid
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.name mustBe eventName
         event.value mustBe eventValue
         event.eventExperiences.size() mustBe 5
         event.attributes.size mustBe 2
         event.eventExperiences.exists(_.testName == "test1") mustBe true
         event.eventExperiences.exists(_.testName == "test2") mustBe true
         event.eventExperiences.exists(_.testName == "test3") mustBe true
         event.eventExperiences.exists(_.testName == "test4") mustBe true
         event.eventExperiences.exists(_.testName == "test5") mustBe true
         event.eventExperiences.exists(_.testName == "test6") mustBe false
         event.eventExperiences.foreach(_.eventId mustBe event.id)
               
      }
   }
}
