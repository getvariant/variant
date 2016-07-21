package com.variant.server.test

import scala.collection.JavaConversions._
import org.apache.http.HttpStatus
import com.variant.core.impl.CoreSessionImpl
import com.variant.core.test.jdbc.EventReader
import com.variant.server.SessionCache
import com.variant.server.boot.UserError
import com.variant.server.util.JettyStartupAndShutdown
import com.variant.server.util.JettyTestServer
import com.variant.server.util.UnitSpec
import com.variant.server.util.UnitSpec._
import net.liftweb.http.testing.HttpResponse
import net.liftweb.http.testing.TestKit
import com.variant.core.net.PayloadReader

/**
 * 
 */
class ServerPostEventTest extends UnitSpec {

   "setup" should "run after beforeAll" in {
      val parserResp = clientCore.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"))
      parserResp.getMessages should have size (0)      
   }
   //---------------------------------------------------------------------------------------//
   //                                  PARSE ERRORS                                         //
   //---------------------------------------------------------------------------------------//

   "postEvent parser" should "emit error if SID is missing" in {
      val json = """
         {
            "name":"NAME",
            "value":"VALUE",
            "parameters":{
               "param1":"PARAM1",
               "param2":"PARAM2"
             }
         }"""
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.MissingProperty).message("sid"))
   }

   it should "emit error if NAME is missing" in {
      val json = """
      {
         "sid":"SID",
         "value":"VALUE",
         "parameters":{
            "param1":"PARAM1",
            "param2":"PARAM2"
          }
      }"""
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.MissingProperty).message("name"))
   }

   it should "emit error if VALUE is missing" in {
      val json = """
      {
         "sid":"SID",
         "name":"NAME",
         "parameters":{
            "param1":"PARAM1",
            "param2":"PARAM2"
          }
      }"""
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.MissingProperty).message("value"))
   }

   it should "emit error if CREATEDATE is a string" in {
      val json = """
      {
         "sid":"SID",
         "name":"NAME",
         "value":"VALUE",
         "createDate":"1454959622350",
         "parameters":{
            "param1":"PARAM1"
            "param2":"PARAM2"
          }
      }"""
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.InvalidDate).message("createDate"))
   }

   it should "emit error if CREATEDATE is an object" in {
      val json = """
      {
         "sid":"SID",
         "name":"NAME",
         "value":"VALUE",
         "createDate":{"foo":"bar"},
         "parameters":{
            "param1":"PARAM1"
            "param2":"PARAM2"
          }
      }"""
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.InvalidDate).message("createDate"))
   }

   it should "emit error if param value is not a string" in {
      val json = """
      {
         "sid":"SID",
         "name":"NAME",
         "value":"VALUE",
         "createDate":1454959622350,
         "parameters":{
            "param1":1234,
            "param2":"PARAM2"
          }
      }"""
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.PropertyNotAString).message("param1"))
   }

   it should "emit error if unsupported property" in {
      val json = """
      {
         "sid":"SID",
         "name":"NAME",
         "value":"VALUE",
         "createDate":1454959622350,
         "unknown":3.14,
         "parameters":{
            "param1":1234,
            "param2":"PARAM2"
          }
      }"""
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.UnsupportedProperty).message("unknown"))
   }

   //---------------------------------------------------------------------------------------//
   //                                   PARSE OKAY                                          //
   //---------------------------------------------------------------------------------------//
   // Pre-pop session cache to avoid errors due to no session, while we're still testing payload parsing.
   var key1 = this.getClass.getSimpleName + "key1"

   it should "parse mixed case properties" in {

      SessionCache.put(key1, new CoreSessionImpl(key1, clientCore).toJson.getBytes)
      
      val json = """
      {
         "sid":"$sid",
         "nAmE":"NAME",
         "VALUE":"VALUE",
         "parameters":{
            "pAraM1":"PARAM1",
            "PARAM2":"PARAM2"
          }
      }""".replaceAll("\\$sid", key1)
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.UnknownState).message())
      
   }

   it should "parse valid createDate spec" in {
      val json = """
      {
         "sid":"$sid",
         "name":"NAME",
         "value":"VALUE",
         "createDate":1454959622350,
         "parameters":{
            "param1":"PARAM1"
            "param2":"PARAM2"
          }
      }""".replaceAll("\\$sid", key1)
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.UnknownState).message())
   }

   //---------------------------------------------------------------------------------------//
   //                                   NO SESSION                                          //
   //---------------------------------------------------------------------------------------//
   
   it should "fail due to no session with 403" in {
      val id = this.getClass.getSimpleName + rand.nextInt();  
      val json = """
      {
         "sid":"$sid",
         "name":"NAME",
         "value":"VALUE",
         "createDate":1454959622350,
         "parameters":{
            "param1":"PARAM1"
            "param2":"PARAM2"
          }
      }""".replaceAll("\\$sid", id)
      val postResp = post("/event", json.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_FORBIDDEN)
      postResp.bodyAsString should equal (UserError.errors(UserError.SessionExpired).message())
   }
   
   //---------------------------------------------------------------------------------------//
   //                                        AOK                                            //
   //---------------------------------------------------------------------------------------//
  
   it should "succeed if valid session and request" in {
   val id = this.getClass.getSimpleName + rand.nextInt();
   val eventJson = """
      {
         "sid":"$sid",
         "name":"NAME",
         "value":"VALUE",
         "createDate":1454959622350,
         "parameters":{
            "param1":"PARAM1"
            "param2":"PARAM2"
          }
      }""".replaceAll("\\$sid", id)
      
      val ssn = new CoreSessionImpl(id, clientCore)
      SessionCache.put(id, ssn.toJson())
      val getResp = get("/session/" + id) ! "Jetty is not running"
      getResp.code should be (HttpStatus.SC_OK)
   
      val payloadReader = new PayloadReader(getResp.bodyAsString.openOrThrowException("Unexpected null response"))
      val ssnIn = CoreSessionImpl.fromJson(clientCore, payloadReader.getBody);
      ssnIn.getTraversedStates().toList should be ('empty)
      ssnIn.getTraversedTests().toList should be ('empty)
      ssnIn.getStateRequest should be (null)

      val req = ssnIn.targetForState(clientCore.getSchema.getState("state1"))
      val jsonIn = req.getSession.asInstanceOf[CoreSessionImpl].toJson()
         
      // Update the session with the state dispatch data.
      val putResp =  put("/session/" + id, jsonIn.getBytes, "application/json") ! "No response from server "
      putResp.code should be (HttpStatus.SC_OK)
      putResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
         
      // Post remote event
      val postResp = post("/event", eventJson.getBytes, "application/json") ! "No response from server "
      postResp.code should be (HttpStatus.SC_OK)
      postResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
      
      Thread.sleep(500) // Writes to DB are async
      
      val eventsFromDb = new EventReader(clientCore).readEvents().filter(e => e.getSessionId == id)
		eventsFromDb should have size (1)
      val eventFromDb = eventsFromDb.iterator.next
      // TODO: uncomment when bug #15
		//eventFromDb.getCreatedOn.getTime should be (1454959622350L)
		eventFromDb.getEventName should be ("NAME")
		eventFromDb.getEventValue should be ("VALUE")
      eventFromDb.getEventExperiences.size() should be (req.getActiveExperiences.size)
		for (variantEvent <- eventFromDb.getEventExperiences) {
		   variantEvent.getEventId should be (eventFromDb.getId)
		   val test = clientCore.getSchema.getTest(variantEvent.getTestName)
		   req.getActiveExperience(test) should equal (test.getExperience(variantEvent.getExperienceName))
		}
   }

}
