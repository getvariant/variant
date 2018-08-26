package com.variant.server.test.controller

import scala.collection.JavaConversions.mutableSetAsJavaSet

import com.variant.core.TraceEvent
import com.variant.core.StateRequestStatus._
import com.variant.core.impl.ServerError.EmptyBody
import com.variant.core.impl.ServerError.JsonParseError
import com.variant.core.impl.ServerError.MissingProperty
import com.variant.core.impl.ServerError.SESSION_EXPIRED
import com.variant.core.session.CoreSession
import com.variant.server.impl.SessionImpl
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.util.TraceEventReader

import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import play.api.test.Helpers.NOT_FOUND
import play.api.test.Helpers.POST
import play.api.test.Helpers.PUT
import play.api.test.Helpers.route
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import play.api.test.Helpers.writeableOf_AnyContentAsText

object TraceEventTest {
   
   val body = ParameterizedString("""
      {
         "sid":"${sid:SID}",
            "event": {
               "name":"${name:NAME}",
               "attrs":{"Name One":"Value One", "Name Two":"Value Two"}
            }
      }
   """.format(System.currentTimeMillis()))
   
   val bodyNoAttrs = ParameterizedString("""
      {
         "sid":"${sid:SID}",
            "event": {
               "name":"${name:NAME}"
            }
      }
   """.format(System.currentTimeMillis()))

   val bodyNoSid = """{"name":"NAME","value":"VALUE"}"""

   val bodyNoName = """
      {
         "sid":"SID",
         "event": {
             "attrs":{"k 1":"v 1", "k 2":"v 2"}
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
      eventWriter.maxDelayMillis  mustEqual 2000
      val millisToSleep = eventWriter.maxDelayMillis + 500

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
         var sid = newSid
         
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "big_conjoint_schema"
         }
         
         // Target and get the request.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state4"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodySession { ssn => 
               ssn.getId mustBe sid
               val stateReq = ssn.getStateRequest
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4")
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]
         serverSsn.getStateRequest().getStatus mustBe InProgress

         // Commit request body with attributes
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString

         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodySession { ssn => 
               val stateReq = ssn.getStateRequest
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Committed
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4")
         }
         
         // Read the event back from the db.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         
         val event = eventsFromDatabase.head
         event.name mustBe TraceEvent.SVE_NAME
         event.attributes.size mustBe 2
         event.attributes("$STATE") mustBe "state4"
         event.attributes("$STATUS") mustBe "Committed"
         event.sessionId mustBe sid
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisToSleep) +- 100   
         event.eventExperiences.size() mustBe 5
         // Test4 is not instrumented.
         event.eventExperiences.exists {_.testName == "test6"} mustBe false

      }

      "flush a state visited event on request commit with attributes" in {

         // New session
         var sid = newSid
         
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "big_conjoint_schema"
         }
         
         // Target and get the request.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state3"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodySession { ssn => 
               val stateReq = ssn.getStateRequest
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe ssn.getSchema.getState("state3")
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().getStatus mustBe InProgress

         // Commit request body with attributes
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2", "key3"->"val3")
            ).toString

         assertResp(route(app, httpReq(PUT, context + "/request").withTextBody(reqBody2)))
            .isOk
            .withBodySession { ssn => 
               val stateReq = ssn.getStateRequest
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Committed
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3")
         }
         
         // Read the event back from the db.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         
         val event = eventsFromDatabase.head
         event.name mustBe TraceEvent.SVE_NAME
         event.attributes.size mustBe 5
         event.attributes("$STATE") mustBe "state3"
         event.attributes("$STATUS") mustBe "Committed"
         event.attributes("key1") mustBe "val1"
         event.attributes("key2") mustBe "val2"
         event.attributes("key3") mustBe "val3"
         event.sessionId mustBe sid
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisToSleep) +- 100   
         event.eventExperiences.size() mustBe 5
         // Test4 is not instrumented.
         event.eventExperiences.exists {_.testName == "test4"} mustBe false

      }

      "Trigger custom event without a state request" in {

         // New session
         var sid = newSid
         
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk         
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "big_conjoint_schema"
         }

         val eventName = "Custom Name"
         val eventBody = body.expand("sid" -> sid, "name" -> eventName)
         
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, httpReq(POST, endpoint).withBody(eventBody)))
            .isOk
            
         // Read the event back from the db.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head

         event.sessionId mustBe sid
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.name mustBe eventName
         event.eventExperiences mustBe empty
         event.attributes.size mustBe 2

      }
      
      "Trigger custom event with active state request and no attribubes" in {

         // New session
         var sid = newSid
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "big_conjoint_schema"
         }

         // Target and get the request.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state5"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodySession { ssn => 
               val stateReq = ssn.getStateRequest
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state5")
            }

         // Custom event.
         val eventName = "Custom Name"
         val eventBody = bodyNoAttrs.expand("sid" -> sid, "name" -> eventName)
         
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, httpReq(POST, endpoint).withBody(eventBody)))
            .isOk
            .withNoBody
         
         // Read events back from the db, but must wait for the asych flusher.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head

         event.sessionId mustBe sid
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.name mustBe eventName
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
         var sid = newSid
         
         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk         
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "big_conjoint_schema"
         }

         // Target and get the request.
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state4"
            ).toString
         
         assertResp(route(app, httpReq(POST, context + "/request").withTextBody(reqBody1)))
            .isOk
            .withBodySession { ssn => 
               val stateReq = ssn.getStateRequest
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state4")
            }

         // Custom event.
         val eventName = "Custom Name"
         val eventBody = body.expand("sid" -> sid, "name" -> eventName)
         
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, httpReq(POST, endpoint).withBody(eventBody)))
            .isOk
            .withNoBody
         
         // Read events back from the db, but must wait for the asych flusher.
         Thread.sleep(millisToSleep)
         val eventsFromDatabase = TraceEventReader(eventWriter).read(_.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head
         
         event.sessionId mustBe sid
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisToSleep) +- 100
         event.name mustBe eventName
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
