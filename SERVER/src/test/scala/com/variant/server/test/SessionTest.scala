package com.variant.server.test

import net.liftweb.http.testing.{TestKit,ReportFailure,HttpResponse}
import com.variant.server.util.JettyStartupAndShutdown
import com.variant.server.util.JettyTestServer
import com.variant.core.session.VariantSessionImpl
import com.variant.server.util.UnitSpec
import com.variant.server.SessionCache
import com.variant.server.core.VariantCore
import org.apache.http.HttpStatus
import com.variant.core.schema.Schema

// trait WebServiceSpec { _: Specification with JettySetupAndTearDown with TestKit =>
class SessionTest extends UnitSpec with JettyStartupAndShutdown with TestKit {
  
   implicit val reportError = new ReportFailure {
      def fail(msg: String) = {
         SessionTest.this.fail(msg)
         
      }
   }
  
   lazy val baseUrl = JettyTestServer.baseUrl
   lazy val schema = VariantCore.api.getSchema()
   
   /**
    * 
    */
   override def beforeAll() = {
      start()
      val parserResp = VariantCore.api.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"))
      parserResp.getMessages should have size (0)
   }
   
   /**
    * 
    */
   override def afterAll() = {
      stop()
   }

   "Get non-existent session" should "create new session" in {
      val id = "key1"
      val httpResp =  get("/session/" + id) !@ "Jetty is not running"
      httpResp.bodyAsString should equal (new VariantSessionImpl(id).toJson)
      val cacheEntry = SessionCache.get(id)
      cacheEntry should not be (null)
      new String(cacheEntry.getJson) should equal (new VariantSessionImpl(id).toJson)
      val httpResp2 = get("/session/" + id) !@ "Jetty is not running"
      httpResp2.bodyAsString should equal (new VariantSessionImpl(id).toJson)
   }

   "Update non-existent session" should "create new session" in {
      val id = "key2"
      val ssn = VariantCore.api.getSession(id)
      val req = VariantCore.api.dispatchRequest(ssn, schema.getState("state2"), "")
      req.getSession shouldBe ssn
      val json = req.getSession.asInstanceOf[VariantSessionImpl].toJson()
      val httpPutResp =  put("/session/" + id, json.getBytes, "application/json").asInstanceOf[HttpResponse]
      httpPutResp.code should be (HttpStatus.SC_OK)
      httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0)
      val cacheEntry = SessionCache.get(id)
      cacheEntry should not be (null)
      new String(cacheEntry.getJson) should equal (json)
      val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
      httpGetResp.code should be (HttpStatus.SC_OK)
      httpGetResp.bodyAsString should equal (json)
      VariantCore.api.commitStateRequest(req, "")
   }

   "Update existing session" should "create new session" in {
      val id = "key2"
      val ssn = VariantCore.api.getSession(id)
      val req = VariantCore.api.dispatchRequest(ssn, schema.getState("state4"), "")
      req.getSession shouldBe ssn
      val json = req.getSession.asInstanceOf[VariantSessionImpl].toJson()
      val httpPutResp =  put("/session/" + id, json.getBytes, "application/json") ! "No response from server"
      httpPutResp.code should be (HttpStatus.SC_OK)
      httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0)
      val cacheEntry = SessionCache.get(id)
      cacheEntry should not be (null)
      new String(cacheEntry.getJson) should equal (json)
      val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
      httpGetResp.code should be (HttpStatus.SC_OK)
      httpGetResp.bodyAsString should equal (json)
      VariantCore.api.commitStateRequest(req, "")
//      println(json)
   }

}