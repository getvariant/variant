package com.variant.server.test.controller

import scala.util.Random
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.collection.JavaConversions._
import com.variant.server.UserError
import com.variant.server.test.util.ParamString
import com.variant.server.ServerPropertiesKey._
import com.variant.server.test.util.EventReader
import com.variant.server.ServerPropertiesKey._
import com.variant.server.test.BaseSpecWithSchema
import com.variant.core.schema.parser.SchemaParser
import com.variant.core.impl.UserHooker

/*
 * Reusable event JSON objects. 
 */
object ConnectionTest {

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
class ConnectionTest extends BaseSpecWithSchema {
   
   import EventTest._
   
   val endpoint = context + "/connection"

   "ConnectionController" should {

      "return 404 on GET" in {
         val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }

      "return 404 on PUT" in {
         val resp = route(app, FakeRequest(PUT, endpoint + "/foo")).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }


      "return  404 on POST with no schema name" in {
         val resp = route(app, FakeRequest(POST, endpoint).withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty        
      }
      
      "return  400 and error on POST to non-existent schema" in {
         val resp = route(app, FakeRequest(POST, endpoint + "/foo").withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe BAD_REQUEST
         contentAsString(resp) mustBe UserError.errors(UserError.UnknownSchema).asMessage("foo")        
      }

      var connId: String = null
      
      "open connection on POST with valid schema name" in {
         val resp = route(app, FakeRequest(POST, endpoint + "/big_covar_schema").withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe OK
         val body = contentAsString(resp)
         body mustNot be (empty)
         val json = Json.parse(body)
         (json \ "id").asOpt[String].isDefined mustBe true
         connId = (json \ "id").asOpt[String].get
         (json \ "ts").asOpt[Long].isDefined mustBe true
         (json \ "schema").asOpt[String].isDefined mustBe true
         val schemaSrc = (json \ "schema").asOpt[String].get
         val parser = new SchemaParser(new UserHooker())
         val parserResp = parser.parse(schemaSrc)
         parserResp.hasMessages() mustBe false
   		parserResp.getSchema() mustNot be (null)
	   	parserResp.getSchemaSrc() mustNot be (null)
	   	
         val schema = parserResp.getSchema
         schema.getName mustEqual "big_covar_schema"
      }
      
      "close connection on DELETE with valid schema name" in {
         val resp = route(app, FakeRequest(DELETE, endpoint + "/" + connId).withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
      }

      "return 400 on DELETE of the schema that no longer exists" in {
         val resp = route(app, FakeRequest(DELETE, endpoint + "/" + connId).withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe BAD_REQUEST
         contentAsString(resp) mustBe UserError.errors(UserError.UnknownSchema).asMessage(connId)      
      }

      "return 400 when attempting to open one too many connections" in {
         val max = server.properties.getInt(MAX_CONCURRENT_CONNECTIONS)
         for (i <- 1 to max) {
            val resp = route(app, FakeRequest(POST, endpoint + "/big_covar_schema").withHeaders("Content-Type" -> "text/plain")).get
            status(resp) mustBe OK
            val body = contentAsString(resp)
            body mustNot be (empty)
            val json = Json.parse(body)
            (json \ "id").asOpt[String].isDefined mustBe true
            connId = (json \ "id").asOpt[String].get
            (json \ "ts").asOpt[Long].isDefined mustBe true
            (json \ "schema").asOpt[String].isDefined mustBe true
            val schemaSrc = (json \ "schema").asOpt[String].get
            val parser = new SchemaParser(new UserHooker())
            val parserResp = parser.parse(schemaSrc)
            parserResp.hasMessages() mustBe false
      		parserResp.getSchema() mustNot be (null)
   	   	parserResp.getSchemaSrc() mustNot be (null)
   	   	
            val schema = parserResp.getSchema
            schema.getName mustEqual "big_covar_schema"
         }
         
         // One over
         val resp1 = route(app, FakeRequest(POST, endpoint + "/big_covar_schema").withHeaders("Content-Type" -> "text/plain")).get
         status(resp1) mustBe BAD_REQUEST
         contentAsString(resp1) mustBe UserError.errors(UserError.TooManyConnections).asMessage() 
         
         // Close one
         val resp2 = route(app, FakeRequest(DELETE, endpoint + "/" + connId).withHeaders("Content-Type" -> "text/plain")).get
         status(resp2) mustBe OK
         contentAsString(resp2) mustBe empty

         // Now should work
         val resp3 = route(app, FakeRequest(POST, endpoint + "/big_covar_schema").withHeaders("Content-Type" -> "text/plain")).get
         status(resp3) mustBe OK
         val body = contentAsString(resp3)
         body mustNot be (empty)
         val json = Json.parse(body)
         (json \ "id").asOpt[String].isDefined mustBe true
         connId = (json \ "id").asOpt[String].get
         (json \ "ts").asOpt[Long].isDefined mustBe true
         (json \ "schema").asOpt[String].isDefined mustBe true
         val schemaSrc = (json \ "schema").asOpt[String].get
         val parser = new SchemaParser(new UserHooker())
         val parserResp = parser.parse(schemaSrc)
         parserResp.hasMessages() mustBe false
   		parserResp.getSchema() mustNot be (null)
	   	parserResp.getSchemaSrc() mustNot be (null)
	   	
         val schema = parserResp.getSchema
         schema.getName mustEqual "big_covar_schema"
      }

/*
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
      */
   }
}
