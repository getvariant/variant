package com.variant.server.test.controller

import scala.util.Random
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.collection.JavaConversions._
import com.variant.core.ServerError._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.util.EventReader
import com.variant.server.test.BaseSpecWithServer
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
         val resp = route(app, FakeRequest(GET, endpoint)).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }

      "return 404 on PUT" in {
         val resp = route(app, FakeRequest(PUT, endpoint)).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }


      var cid: String = null 
      var schid: String = null
      
      "obtain a connection" in {
         // POST new connection
         val connResp = route(app, connectionRequest("big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         cid = (json \ "id").as[String]
         cid mustNot be (null)
         schid = (json \ "schema" \ "id").as[String]
      }

      "return 400 and error on POST with no body" in {
         val resp = route(app, connectedRequest(POST, endpoint, cid)).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "isInternal").as[Boolean] mustBe EmptyBody.isInternal() 
         (respJson \ "code").as[Int] mustBe EmptyBody.getCode 
         val args = (respJson \ "args").as[Seq[String]]
         args mustBe empty
     }
      
      "return  400 and error on POST with invalid JSON" in {
         val resp = route(app, connectedRequest(POST, endpoint, cid).withTextBody("bad json")).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "isInternal").as[Boolean] mustBe JsonParseError.isInternal() 
         (respJson \ "code").as[Int] mustBe JsonParseError.getCode 
         val args = (respJson \ "args").as[Seq[String]]
         args(0) must startWith ("Unrecognized token 'bad'")
     }

      "return  400 and error on POST with no sid" in {
         val resp = route(app, connectedRequest(POST, endpoint, cid).withTextBody(bodyNoSid)).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "isInternal").as[Boolean] mustBe MissingProperty.isInternal() 
         (respJson \ "code").as[Int] mustBe MissingProperty.getCode 
         (respJson \ "args").as[Seq[String]] mustBe Seq("sid") 
      }

      "return 400 and error on POST with no name" in {
         val resp = route(app, connectedRequest(POST, endpoint, cid).withTextBody(bodyNoName)).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "isInternal").as[Boolean] mustBe MissingProperty.isInternal() 
         (respJson \ "code").as[Int] mustBe MissingProperty.getCode 
         (respJson \ "args").as[Seq[String]] mustBe Seq("name") 
      }

/*
       var connId: String = null
      
      "obtain a connection" in {
         // POST new connection
         val connResp = route(app, FakeRequest(POST, context + "/connection/big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         connId = (json \ "id").as[String]
         connId mustNot be (null)
      }
*/
      var ssn: Session = null;
      
      "obtain a session" in {
         val sid = newSid()
         // PUT session.
         val sessionJson = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis(), schema.getId)).expand("sid" -> sid)
         val ssnResp = route(app, connectedRequest(PUT, context + "/session", cid).withTextBody(sessionJson)).get
         status(ssnResp) mustBe OK
         contentAsString(ssnResp) mustBe empty
         ssn = ssnStore.get(sid, cid).get
      }
      
      "return  400 and error on POST with non-existent session" in {
         
         val eventBody = body.expand("sid" -> "foo")
         val resp = route(app, connectedRequest(POST, endpoint, cid).withTextBody(eventBody)).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "isInternal").as[Boolean] mustBe SessionExpired.isInternal() 
         (respJson \ "code").as[Int] mustBe SessionExpired.getCode 
         (respJson \ "args").as[Seq[String]] mustBe Seq("foo") 
      }

      "return 400 and error on POST with missing param name" in {

         val eventBody = bodyNoParamName.expand("sid" -> ssn.getId)
         val resp = route(app, connectedRequest(POST, endpoint, cid).withTextBody(eventBody)).get
         status(resp)mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "isInternal").as[Boolean] mustBe MissingParamName.isInternal() 
         (respJson \ "code").as[Int] mustBe MissingParamName.getCode 
         (respJson \ "args").as[Seq[String]] mustBe empty 
      }

      "flush the event with explicit timestamp" in {

         val timestamp = System.currentTimeMillis()
         val eventName = Random.nextString(5)
         val eventValue = Random.nextString(5)
         val eventBody = body.expand("sid" -> ssn.getId, "ts" -> timestamp, "name" -> eventName, "value" -> eventValue)
         val eventResp = route(app, connectedRequest(POST, endpoint, cid).withTextBody(eventBody)).get
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         status(eventResp) mustBe OK
         contentAsString(eventResp) mustBe empty
         
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
