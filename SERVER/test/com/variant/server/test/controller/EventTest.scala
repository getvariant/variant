package com.variant.server.test.controller

import scala.util.Random
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.collection.JavaConversions._
import com.variant.core.ServerError._
import com.variant.core.ConnectionStatus._
import com.variant.core.util.Constants._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.util.EventReader
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.server.conn.ConnectionStore
import com.variant.server.conn.ConnectionStore
import javax.inject.Inject
import com.variant.server.api.Session
import org.scalatest.TestData
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import com.variant.server.boot.VariantApplicationLoader
import play.api.Configuration

object EventTest {
   
   val body = ParameterizedString("""
      {"sid":"${sid:SID}",
       "name":"${name:NAME}",
       "value":"${value:VALUE}",
       "ts":${ts:%d},
       "params":[{"name":"Name One","value":"Value One"},{"name":"Name Two","value":"Value Two"}]
      }
   """.format(System.currentTimeMillis()))
      
   val bodyNoSid = """{"name":"NAME","value":"VALUE"}"""
   val bodyNoName = """{"sid":"SID","value":"VALUE"}"""
   val bodyNoParamName = ParameterizedString("""
      {"sid":"${sid:SID}",
       "name":"NAME",
       "value":"VALUE",
       "ts":%d,
       "params":[{"namee":"Name One","value":"Value One"}]
      }
   """.format(System.currentTimeMillis()))

}

/**
 * Event Controller
 */
class EventTest extends BaseSpecWithServer {
   
   import EventTest._
   
   val endpoint = context + "/event"
      
   "EventController" should {

      val schema = server.schemata("big_covar_schema")
      val eventWriter = schema.eventWriter

      "return 404 on GET" in {
         assertResp(route(app, FakeRequest(GET, endpoint)))
            .is(NOT_FOUND)
            .withNoBody
            .withNoConnStatusHeader
      }

      "return 404 on PUT" in {
         assertResp(route(app, FakeRequest(PUT, endpoint)))
            .is(NOT_FOUND)
            .withNoBody
            .withNoConnStatusHeader
      }


      var cid: String = null 
      var schid: String = null
      
      "obtain a connection" in {
         assertResp(route(app, connectionRequest("big_covar_schema")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json =>
               cid = (json \ "id").as[String]
               cid mustNot be (null)
               schid = (json \ "schema" \ "id").as[String]
            }
      }
      
      "return 400 and error on POST with no body" in {
         assertResp(route(app, connectedRequest(POST, endpoint, cid)))
            .isError(EmptyBody)
            .withConnStatusHeader(OPEN)
     }

      "return  400 and error on POST with invalid JSON" in {
         assertResp(route(app, connectedRequest(POST, endpoint, cid).withBody("bad json")))
            .isError(JsonParseError, "Unrecognized token 'bad': was expecting ('true', 'false' or 'null') at [Source: bad json; line: 1, column: 4]")
            .withConnStatusHeader(OPEN)
     }

      "return  400 and error on POST with no sid" in {
         assertResp(route(app, connectedRequest(POST, endpoint, cid).withBody(bodyNoSid)))
            .isError(MissingProperty, "sid")
            .withConnStatusHeader(OPEN)
      }

      "return 400 and error on POST with no name" in {
         assertResp(route(app, connectedRequest(POST, endpoint, cid).withBody(bodyNoName)))
            .isError(MissingProperty, "name")
            .withConnStatusHeader(OPEN)
      }

      "return  400 and error on POST with non-existent session" in {
         
         val eventBody = body.expand("sid" -> "foo")
         assertResp(route(app, connectedRequest(POST, endpoint, cid).withBody(eventBody)))
            .isError(SessionExpired, "foo")
            .withConnStatusHeader(OPEN)
      }

      var ssn: Session = null;

      "obtain a session" in {
         val sid = newSid()
         val sessionJson = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis(), schema.getId)).expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid).withBody(sessionJson)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody

         ssn = ssnStore.get(sid, cid).get
      }
      
      "return 400 and error on POST with missing param name" in {

         val eventBody = bodyNoParamName.expand("sid" -> ssn.getId)
         assertResp(route(app, connectedRequest(POST, endpoint, cid).withBody(eventBody)))
            .isError(MissingParamName)
            .withConnStatusHeader(OPEN)
      }

      "flush the event with explicit timestamp" in {

         val timestamp = System.currentTimeMillis()
         val eventName = Random.nextString(5)
         val eventValue = Random.nextString(5)
         val eventBody = body.expand("sid" -> ssn.getId, "ts" -> timestamp, "name" -> eventName, "value" -> eventValue)
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         assertResp(route(app, connectedRequest(POST, endpoint, cid).withBody(eventBody)))
            .isOk
            .withNoBody
            .withConnStatusHeader(OPEN)
         
         // Read events back from the db, but must wait for the asych flusher.
         eventWriter.maxDelayMillis  mustEqual 2000
         Thread.sleep(eventWriter.maxDelayMillis + 500)
         val eventsFromDatabase = EventReader(eventWriter).read()
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head
         event.getCreatedOn.getTime mustBe timestamp
         event.getName mustBe eventName
         event.getValue mustBe eventValue
         event.getSessionId mustBe ssn.getId
         event.getEventExperiences.size() mustBe 3
         event.getEventExperiences.foreach(ee => {
            ee.getTestName match {
               case "test1" => {
                  ee.getExperienceName mustBe "A"
                  ee.isControl() mustBe true
               }
               case "test2" => {
                  ee.getExperienceName mustBe "B"
                  ee.isControl() mustBe false
               }
               case "test3" => {
                  ee.getExperienceName mustBe "C"
                  ee.isControl() mustBe false
               }
               case t => throw new RuntimeException("Unexpected test %s".format(t))
            }
         })
      }
   }
}
