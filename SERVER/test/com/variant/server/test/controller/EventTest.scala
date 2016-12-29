package com.variant.server.test.controller

import scala.util.Random
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.boot.ServerErrorApi._
import com.variant.server.test.util.ParamString
import com.variant.server.test.util.EventReader
import com.variant.server.test.BaseSpecWithSchema
//import com.variant.server.test.controller.SessionTest

/*
 * Reusable event JSON objects. 
 */
object EventTest {

   val body = ParamString("""
      {"sid":"${sid:SID}",
       "name":"${name:NAME}",
       "value":"${value:VALUE}",
       "ts":${ts:%d},
       "params":[{"name":"Name One","value":"Value One"},{"name":"Name Two","value":"Value Two"}]
      }
   """.format(System.currentTimeMillis()))
      
   val bodyNoSid = """{"name":"NAME","value":"VALUE"}"""
   val bodyNoName = """{"sid":"SID","value":"VALUE"}"""
   val bodyNoParamName = ParamString("""
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
class EventTest extends BaseSpecWithSchema {
   
   import EventTest._
   
   val endpoint = context + "/event"

   "EventController" should {

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


      "return  400 and error on POST with no body" in {
         val resp = route(app, FakeRequest(POST, endpoint).withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "code").as[Int] mustBe JsonParseError.code 
         (respJson \ "message").as[String] must startWith ("JSON parsing error") 
         (respJson \ "message").as[String] must include ("No content") 
     }
      
      "return  400 and error on POST with invalid JSON" in {
         val resp = route(app, FakeRequest(POST, endpoint).withBody("bad json").withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "code").as[Int] mustBe JsonParseError.code 
         (respJson \ "message").as[String] must startWith ("JSON parsing error") 
         (respJson \ "message").as[String] must include ("Unrecognized token") 
     }

      "return  400 and error on POST with no sid" in {
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(bodyNoSid)).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "code").as[Int] mustBe MissingProperty.code 
         (respJson \ "message").as[String] mustBe MissingProperty.message("sid") 
      }

      "return  400 and error on POST with no name" in {
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(bodyNoName)).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "code").as[Int] mustBe MissingProperty.code 
         (respJson \ "message").as[String] mustBe MissingProperty.message("name") 
      }
      
      "return  403 and error on POST with non-existent session" in {
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(body.expand())).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "code").as[Int] mustBe SessionExpired.code 
         (respJson \ "message").as[String] mustBe SessionExpired.message() 
      }
     
      "return 400 and error on POST with missing param name" in {
         val eventBody = bodyNoParamName.expand()
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(eventBody)).get
         status(resp)mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "code").as[Int] mustBe MissingParamName.code 
         (respJson \ "message").as[String] mustBe MissingParamName.message() 
      }

      "return 200 and create event with existent session" in {
         val sid = Random.nextInt(100000).toString
         // PUT session.
         val ssnBody = SessionTest.body.expand("sid" -> sid)
         val ssnResp = route(app, FakeRequest(PUT, context + "/session/" + sid).withTextBody(ssnBody)).get
         status(ssnResp) mustBe OK
         contentAsString(ssnResp) mustBe empty
         val ssn = store.asSession(sid)
         // POST event
         val ts = System.currentTimeMillis()
         val eventName = Random.nextString(5)
         val eventValue = Random.nextString(5)
         val eventBody = body.expand("sid" -> sid, "ts" -> ts, "name" -> eventName, "value" -> eventValue)
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(eventBody)).get
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
         
         // Read events back from the db, but must wait for the asych flusher.
         server.eventWriter.maxDelayMillis  mustEqual 2000
         Thread.sleep(server.eventWriter.maxDelayMillis + 500)
         val eventsFromDatabase = EventReader(server.eventWriter).read()
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head
         event.getCreatedOn.getTime mustBe ts
         event.getName mustBe eventName
         event.getValue mustBe eventValue
         event.getSessionId mustBe sid
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
