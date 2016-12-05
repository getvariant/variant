package com.variant.server.test

import scala.util.Random
import org.scalatestplus.play._
import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.UserError
import com.variant.server.test.util.ParamString
import com.variant.server.ServerPropertiesKey._
import com.variant.server.test.util.EventReader
import com.variant.server.ServerPropertiesKey._
import com.variant.server.session.SessionStore
import com.variant.server.boot.VariantServer
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.TestData
import com.variant.core.exception.Error
import com.variant.server.ServerErrorException
import com.variant.server.ServerError

/**
 * Test various schema deployment error scenarios
 */
class SchemaDeployExceptionTest extends PlaySpec with OneAppPerTest {
   
   implicit override def newAppForTest(testData: TestData): Application = {
      new GuiceApplicationBuilder()
         .configure(
            Map(
                  "variant.schemas.dir" -> {
                     if (testData.name.endsWith("SCHEMAS_DIR_MISSING")) "non-existent" 
                     else if (testData.name.endsWith("SCHEMAS_DIR_NOT_DIR")) "test-schemas-file"
                     else if (testData.name.endsWith("MULTIPLE_SCHEMAS_NOT_SUPPORTED")) "test-schemas-multi"
                     else "don't know what to do"
                  }
            ))
       .build()
   }
 
   "Server must throw SCHEMAS_DIR_MISSING" in {
      val server = app.injector.instanceOf[VariantServer]
      server.isUp mustBe false
      server.schema.isDefined mustBe false
      server.startupErrorLog.size mustEqual 1
      val ex = server.startupErrorLog.head
      ex.getSeverity mustEqual Error.Severity.FATAL
      ex.getMessage mustEqual new ServerErrorException(ServerError.SCHEMAS_DIR_MISSING, "non-existent").getMessage
   }
   
   "Server must throw SCHEMAS_DIR_NOT_DIR" in {
      val server = app.injector.instanceOf[VariantServer]
      server.schema.isDefined mustBe false
      server.isUp mustBe false
      server.schema.isDefined mustBe false
      server.startupErrorLog.size mustEqual 1
      val ex = server.startupErrorLog.head
      ex.getSeverity mustEqual Error.Severity.FATAL
      ex.getMessage mustEqual new ServerErrorException(ServerError.SCHEMAS_DIR_NOT_DIR, "test-schemas-file").getMessage
   }

   "Server must throw MULTIPLE_SCHEMAS_NOT_SUPPORTED" in {
      val server = app.injector.instanceOf[VariantServer]
      server.schema.isDefined mustBe false
      server.isUp mustBe false
      server.schema.isDefined mustBe false
      server.startupErrorLog.size mustEqual 1
      val ex = server.startupErrorLog.head
      ex.getSeverity mustEqual Error.Severity.FATAL
      ex.getMessage mustEqual new ServerErrorException(ServerError.MULTIPLE_SCHEMAS_NOT_SUPPORTED, "test-schemas-multi").getMessage
   }

/*   
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
         contentAsString(resp) must startWith ("JSON parsing error")        
         contentAsString(resp) must include ("No content")        
     }
      
      "return  400 and error on POST with invalid JSON" in {
         val resp = route(app, FakeRequest(POST, endpoint).withBody("bad json").withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe BAD_REQUEST
         contentAsString(resp) must startWith ("JSON parsing error")        
         contentAsString(resp) must include ("Unrecognized token")        
     }

      "return  400 and error on POST with no sid" in {
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(bodyNoSid)).get
         status(resp) mustBe BAD_REQUEST
         contentAsString(resp) mustBe UserError.errors(UserError.MissingProperty).asMessage("sid")
      }

      "return  400 and error on POST with no name" in {
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(bodyNoName)).get
         status(resp) mustBe BAD_REQUEST
         contentAsString(resp) mustBe UserError.errors(UserError.MissingProperty).asMessage("name")
      }
      
      "return  403 and error on POST with non-existent session" in {
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(body.expand())).get
         status(resp) mustBe FORBIDDEN
         contentAsString(resp) mustBe UserError.errors(UserError.SessionExpired).asMessage("name")
      }
     
      "return 400 and error on POST with missing param name" in {
         val eventBody = bodyNoParamName.expand()
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(eventBody)).get
         status(resp)mustBe BAD_REQUEST
         contentAsString(resp) mustBe UserError.errors(UserError.MissingParamName).asMessage()
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
   * 
   */
}
