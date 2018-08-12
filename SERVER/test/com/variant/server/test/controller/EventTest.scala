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
import com.variant.server.test.util.EventReader
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

object EventTest {
   
   val body = ParameterizedString("""
      {"sid":"${sid:SID}",
       "name":"${name:NAME}",
       "value":"${value:VALUE}",
       "ts":${ts:%d},
       "attrs":[{"name":"Name One","value":"Value One"},{"name":"Name Two","value":"Value Two"}]
      }
   """.format(System.currentTimeMillis()))
      
   val bodyNoSid = """{"name":"NAME","value":"VALUE"}"""
   val bodyNoName = """{"sid":"SID","value":"VALUE"}"""
   val bodyNoParamName = ParameterizedString("""
      {"sid":"${sid:SID}",
       "name":"NAME",
       "value":"VALUE",
       "ts":%d,
       "attrs":[{"namee":"Name One","value":"Value One"}]
      }
   """.format(System.currentTimeMillis()))

}

/**
 * Event Controller
 */
class EventTest extends EmbeddedServerSpec {
   
   import EventTest._
   
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

      val sid = newSid

      "obtain a session" in {

         assertResp(route(app, httpReq(POST, context + "/session/big_conjoint_schema/" + sid)))
            .isOk         
            .withBodyJsonSession(sid, "big_conjoint_schema")

      }

      "target for state5" in {
         
         val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state3"
            ).toString
         
         // Target and get the request.
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
               stateReq.getStateVisitedEvent mustBe null 
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().isCommitted() mustBe false

         // Commit request body
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "sve" -> s""" {"sid":"${sid}","ts":1533787754794,"name":"$$STATE_VISIT","value":"state3"} """
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
      }

      "return 400 and error on POST with missing param name" in {

         val eventBody = bodyNoParamName.expand("sid" -> sid)
         assertResp(route(app, httpReq(POST, endpoint).withBody(eventBody)))
            .isError(MissingParamName)
      }

      "flush custom event with explicit timestamp" in {

         val timestamp = System.currentTimeMillis()
         val eventName = Random.nextString(5)
         val eventValue = Random.nextString(5)
         val eventBody = body.expand("sid" -> sid, "ts" -> timestamp, "name" -> eventName, "value" -> eventValue)
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, httpReq(POST, endpoint).withBody(eventBody)))
            .isOk
            .withNoBody
         
         // Read events back from the db, but must wait for the asych flusher.
         eventWriter.maxDelayMillis  mustEqual 2000
         Thread.sleep(eventWriter.maxDelayMillis + 500)
         val eventsFromDatabase = EventReader(eventWriter).read()
         eventsFromDatabase.size mustBe 2
         
         eventsFromDatabase.foreach { event =>
            
            //println("****\n" + event)
            
            event.getName match {
               
               case `eventName` =>
                  
                  event.getCreatedOn.getTime mustBe timestamp
                  event.getValue mustBe eventValue
                  event.getSessionId mustBe sid
                  event.getEventExperiences.size() mustBe 5
                  // Test4 is not instrumented.
                  event.getEventExperiences.exists {_.getTestName == "test4"} mustBe false
               
               case "$STATE_VISIT" =>
                  event.getValue mustBe "state3"
                  event.getSessionId mustBe sid
                  event.getCreatedOn.getTime mustBe 1533787754794L   
                  event.getEventExperiences.size() mustBe 5
                  // Test4 is not instrumented.
                  event.getEventExperiences.exists {_.getTestName == "test4"} mustBe false
                  
               case name => fail(s"Unexpected event [${name}]")
                  
            }
            
         }
      }
   }
}
