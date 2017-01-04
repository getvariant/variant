package com.variant.server.test.controller

import scala.util.Random
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.collection.JavaConversions._
import com.variant.server.boot.ServerErrorApi._
import com.variant.server.test.util.ParamString
import com.variant.server.test.util.EventReader
import com.variant.server.ConfigKeys._
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
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "code").as[Int] mustBe UnknownSchema.code 
         (respJson \ "message").as[String] mustBe UnknownSchema.message("foo")        
      }

      var connId: String = null
      
      "open connection on POST with valid schema name" in {
         val resp = route(app, FakeRequest(POST, endpoint + "/big_covar_schema").withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe OK
         val body = contentAsString(resp)
         body mustNot be (empty)
         val json = Json.parse(body)
         (json \ "id").asOpt[String].isDefined mustBe true
         connId = (json \ "id").as[String]
         (json \ "ssnto").as[Long] mustBe server.config.getInt(SESSION_TIMEOUT)
         (json \ "ts").asOpt[Long].isDefined mustBe true
         val schemaSrc = (json \ "schema").as[String]
         val parser = new SchemaParser(new UserHooker())
         val parserResp = parser.parse(schemaSrc)
         parserResp.hasMessages() mustBe false
   		parserResp.getSchema() mustNot be (null)
	   	parserResp.getSchemaSrc() mustNot be (null)
	   	
         val schema = parserResp.getSchema
         schema.getName mustEqual "big_covar_schema"
      }
      
      "close connection on DELETE with valid id" in {
         val resp = route(app, FakeRequest(DELETE, endpoint + "/" + connId).withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
      }

      "return 400 on DELETE of connection that no longer exists" in {
         val resp = route(app, FakeRequest(DELETE, endpoint + "/" + connId).withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe BAD_REQUEST
         val respJson = contentAsJson(resp)
         respJson mustNot be (null)
         (respJson \ "code").as[Int] mustBe UnknownConnection.code 
         (respJson \ "message").as[String] mustBe UnknownConnection.message(connId)
      }

      "return 400 when attempting to open one too many connections" in {
         val max = server.config.getInt(MAX_CONCURRENT_CONNECTIONS)
         for (i <- 1 to max) {
            val resp = route(app, FakeRequest(POST, endpoint + "/big_covar_schema").withHeaders("Content-Type" -> "text/plain")).get
            status(resp) mustBe OK
            val body = contentAsString(resp)
            body mustNot be (empty)
            val json = Json.parse(body)
            connId = (json \ "id").as[String]
            (json \ "ssnto").as[Long] mustBe server.config.getInt(SESSION_TIMEOUT)
            (json \ "ts").asOpt[Long].isDefined mustBe true
            val schemaSrc = (json \ "schema").as[String]
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
         val respJson = contentAsJson(resp1)
         respJson mustNot be (null)
         (respJson \ "code").as[Int] mustBe TooManyConnections.code 
         (respJson \ "message").as[String] mustBe TooManyConnections.message()
         
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
         connId = (json \ "id").as[String]
         (json \ "ssnto").as[Long] mustBe server.config.getInt(SESSION_TIMEOUT)
         (json \ "ts").asOpt[Long].isDefined mustBe true
         val schemaSrc = (json \ "schema").as[String]
         val parser = new SchemaParser(new UserHooker())
         val parserResp = parser.parse(schemaSrc)
         parserResp.hasMessages() mustBe false
   		parserResp.getSchema() mustNot be (null)
	   	parserResp.getSchemaSrc() mustNot be (null)
	   	
         val schema = parserResp.getSchema
         schema.getName mustEqual "big_covar_schema"
      }
   }
}
