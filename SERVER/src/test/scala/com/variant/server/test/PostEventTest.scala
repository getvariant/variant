package com.variant.server.test

import org.apache.http.HttpStatus
import com.variant.server.config.UserError
import com.variant.server.core.VariantCore
import com.variant.server.util.JettyStartupAndShutdown
import com.variant.server.util.JettyTestServer
import com.variant.server.util.UnitSpec
import net.liftweb.http.testing.HttpResponse
import net.liftweb.http.testing.TestKit
import com.variant.server.SessionCache
import com.variant.core.session.VariantSessionImpl

/**
 * TODO: read events from database.
 */
class PostEventTest extends UnitSpec {
  
   lazy val baseUrl = JettyTestServer.baseUrl

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
      val postResp = post("/event", json.getBytes, "application/json").asInstanceOf[HttpResponse]
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
      val postResp = post("/event", json.getBytes, "application/json").asInstanceOf[HttpResponse]
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
      val postResp = post("/event", json.getBytes, "application/json").asInstanceOf[HttpResponse]
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
      val postResp = post("/event", json.getBytes, "application/json").asInstanceOf[HttpResponse]
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
      val postResp = post("/event", json.getBytes, "application/json").asInstanceOf[HttpResponse]
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
      val postResp = post("/event", json.getBytes, "application/json").asInstanceOf[HttpResponse]
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
      val postResp = post("/event", json.getBytes, "application/json").asInstanceOf[HttpResponse]
      postResp.code should be (HttpStatus.SC_BAD_REQUEST)
      postResp.bodyAsString should equal (UserError.errors(UserError.UnsupportedProperty).message("unknown"))
   }

   //---------------------------------------------------------------------------------------//
   //                                   PARSE OKAY                                          //
   //---------------------------------------------------------------------------------------//

   it should "parse mixed case properties" in {
      val json = """
      {
         "sid":"key1",
         "nAmE":"NAME",
         "VALUE":"VALUE",
         "parameters":{
            "pAraM1":"PARAM1",
            "PARAM2":"PARAM2"
          }
      }"""
      val id = "key1"
      SessionCache.put(id, new VariantSessionImpl(id).toJson.getBytes)
      val postResp = post("/event", json.getBytes, "application/json").asInstanceOf[HttpResponse]
      postResp.code should be (HttpStatus.SC_OK)
      postResp.bodyAsString.openOrThrowException("Unexpected null response").length should equal (0)
   }
   
   it should "parse valid createDate spec" in {
      val json = """
      {
         "sid":"key1",
         "name":"NAME",
         "value":"VALUE",
         "createDate":1454959622350,
         "parameters":{
            "param1":"PARAM1"
            "param2":"PARAM2"
          }
      }"""
      val id = "key1"
      SessionCache.put(id, new VariantSessionImpl(id).toJson.getBytes)
      val postResp = post("/event", json.getBytes, "application/json").asInstanceOf[HttpResponse]
      postResp.code should be (HttpStatus.SC_OK)
      postResp.bodyAsString.openOrThrowException("Unexpected null response").length should equal (0)
   }


}