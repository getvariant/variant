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
import com.variant.server.api.ConfigKeys._
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.core.schema.parser.SchemaParser
import com.variant.server.schema.ServerSchemaParser

/*
 * Reusable event JSON objects. 
 */
object ConnectionTest {

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
class ConnectionTest extends BaseSpecWithServer {
   

import EventTest._
   
   val endpoint = context + "/connection"

   "ConnectionController" should {

      /* GET connection is off for now.
      "return 702 Unknown connection on GET bad connection ID" in {
         val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(resp) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(resp))
         isInternal mustBe UnknownConnection.isInternal() 
         error mustBe UnknownConnection
         args mustBe Seq("foo")
      }
	   */
   
      "return  404 on POST with no schema name" in {
         assertResp(route(app, connectionRequest("")))
            .is(NOT_FOUND)
            .withNoBody
      }

      "return  400 and error on POST to non-existent schema" in {
         assertResp(route(app, connectionRequest("foo")))
            .isError(UnknownSchema, "foo")
            .withNoConnStatusHeader
      }
      
      "ignore connection ID header on connection" in {
         
         var cid: String = null
         
         assertResp(route(app, connectedRequest(POST, endpoint + "/big_covar_schema", "foo")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               (json \ "id").asOpt[String].isDefined mustBe true
               cid = (json \ "id").as[String]
               (json \ "ssnto").as[Long] mustBe server.config.getInt(SESSION_TIMEOUT)
               (json \ "ts").asOpt[Long].isDefined mustBe true
               val schemaSrc = (json \ "schema" \ "src").as[String]
               val schemaId = (json \ "schema" \ "id").as[String]
               schemaSrc mustBe server.schemata("big_covar_schema").source
               schemaId mustBe server.schemata("big_covar_schema").getId
               val parser = ServerSchemaParser()
               val parserResp = parser.parse(schemaSrc)
               parserResp.hasMessages() mustBe false
         		parserResp.getSchema() mustNot be (null)
      	   	parserResp.getSchemaSrc() mustNot be (null)      	   	
               val schema = parserResp.getSchema
               schema.getName mustEqual "big_covar_schema"
            }
         
         // close this connection. 
         assertResp(route(app, connectedRequest(DELETE, endpoint, cid)))
            .isOk
            .withNoBody
            .withConnStatusHeader(CLOSED_BY_CLIENT)
      }

      var connId: String = null

      "open connection on POST with valid schema name" in {
         
         assertResp(route(app, connectionRequest("big_covar_schema")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json =>
               (json \ "id").asOpt[String].isDefined mustBe true
               connId = (json \ "id").as[String]
               (json \ "ssnto").as[Long] mustBe server.config.getInt(SESSION_TIMEOUT)
               (json \ "ts").asOpt[Long].isDefined mustBe true
               val schemaSrc = (json \ "schema" \ "src").as[String]
               val schemaId = (json \ "schema" \ "id").as[String]
               schemaSrc mustBe server.schemata("big_covar_schema").source
               schemaId mustBe server.schemata("big_covar_schema").getId
               val parser = ServerSchemaParser()
               val parserResp = parser.parse(schemaSrc)
               parserResp.hasMessages() mustBe false
         		parserResp.getSchema() mustNot be (null)
      	   	parserResp.getSchemaSrc() mustNot be (null)
      	   	
               val schema = parserResp.getSchema
               schema.getName mustEqual "big_covar_schema"
            }
      }
      
      /* GET connection is off
      "return OK on GET open connection" in {
         val resp = route(app, FakeRequest(GET, endpoint + "/" + connId).withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
      }
      */
      
      "close connection on DELETE with valid connection ID" in {
         assertResp(route(app, connectedRequest(DELETE, endpoint, connId)))
            .isOk
            .withConnStatusHeader(CLOSED_BY_CLIENT)
            .withNoBody
      }
      
      "return 400 on DELETE of connection which no longer exists" in {
         val resp = route(app, connectedRequest(DELETE, endpoint, connId)).get
         header(HTTP_HEADER_CONN_STATUS, resp) mustBe None
         status(resp) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(resp))
         isInternal mustBe UnknownConnection.isInternal() 
         error mustBe UnknownConnection
         args mustBe Seq(connId.toString())
      }

      // At this point, we should have no open connections.
      "return 400 when attempting to open one too many connections" in {
         val max = server.config.getInt(MAX_CONCURRENT_CONNECTIONS)
         for (i <- 1 to max) {
            assertResp(route(app, connectionRequest("big_covar_schema")))
               .isOk
               .withConnStatusHeader(OPEN)
               .withBodyJson { json =>
                  connId = (json \ "id").as[String]
                  (json \ "ssnto").as[Long] mustBe server.config.getInt(SESSION_TIMEOUT)
                  (json \ "ts").asOpt[Long].isDefined mustBe true
                  val schemaSrc = (json \ "schema" \ "src").as[String]
                  val schemaId = (json \ "schema" \ "id").as[String]
                  schemaSrc mustBe server.schemata("big_covar_schema").source
                  schemaId mustBe server.schemata("big_covar_schema").getId
                  val parser = ServerSchemaParser()
                  val parserResp = parser.parse(schemaSrc)
                  parserResp.hasMessages() mustBe false
            		parserResp.getSchema() mustNot be (null)
         	   	parserResp.getSchemaSrc() mustNot be (null)
         	   	
                  val schema = parserResp.getSchema
                  schema.getName mustEqual "big_covar_schema"                  
               }
         }
         
         // One over
         assertResp(route(app, connectionRequest("big_covar_schema")))
            .isError(TooManyConnections)
            .withNoConnStatusHeader
         
         // Close one
         assertResp(route(app, connectedRequest(DELETE, endpoint, connId)))
            .isOk
            .withNoBody
            .withConnStatusHeader(CLOSED_BY_CLIENT)

         // Now should work
         assertResp(route(app, connectionRequest("big_covar_schema")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json =>
               connId = (json \ "id").as[String]
               (json \ "ssnto").as[Long] mustBe server.config.getInt(SESSION_TIMEOUT)
               (json \ "ts").asOpt[Long].isDefined mustBe true
               val schemaSrc = (json \ "schema" \ "src").as[String]
               val schemaId = (json \ "schema" \ "id").as[String]
               schemaSrc mustBe server.schemata("big_covar_schema").source
               schemaId mustBe server.schemata("big_covar_schema").getId
               val parser = ServerSchemaParser()
               val parserResp = parser.parse(schemaSrc)
               parserResp.hasMessages() mustBe false
         		parserResp.getSchema() mustNot be (null)
      	   	parserResp.getSchemaSrc() mustNot be (null)
      	   	
               val schema = parserResp.getSchema
               schema.getName mustEqual "big_covar_schema"
            }
      }
   }
}

