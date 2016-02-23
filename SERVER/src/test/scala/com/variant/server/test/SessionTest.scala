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
import scala.collection.JavaConversions._
import UnitSpec._
import com.variant.core.hook.TestQualificationHook
import com.variant.core.hook.HookListener

/**
 */
class SessionTest extends UnitSpec {
    
   lazy val baseUrl = JettyTestServer.baseUrl

   "setup" should "run after beforeAll" in {
      val parserResp = api.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"))
      parserResp.getMessages should have size (0)      
   }

   "Get non-existent session" should "create new session" in {
      val id = this.getClass.getSimpleName + "key1"
      val httpResp =  get("/session/" + id) ! "No response from server "
      httpResp.bodyAsString should equal (new VariantSessionImpl(id).toJson)
      val cacheEntry = SessionCache.get(id)
      cacheEntry should not be (null)
      new String(cacheEntry.getJson) should equal (new VariantSessionImpl(id).toJson)
      val httpResp2 = get("/session/" + id) !@ "Jetty is not running"
      httpResp2.bodyAsString should equal (new VariantSessionImpl(id).toJson)
   }

   "Update non-existent session" should "create new session" in {
      val id = this.getClass.getSimpleName + "key2"
      val ssn = api.getSession(id)
      val req = api.dispatchRequest(ssn, api.getSchema.getState("state2"), "")
      req.getSession shouldBe ssn
      val json = req.getSession.asInstanceOf[VariantSessionImpl].toJson()
      val httpPutResp =  put("/session/" + id, json.getBytes, "application/json") ! "No response from server "
      httpPutResp.code should be (HttpStatus.SC_OK)
      httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0)
      val cacheEntry = SessionCache.get(id)
      cacheEntry should not be (null)
      new String(cacheEntry.getJson) should equal (json)
      val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
      httpGetResp.code should be (HttpStatus.SC_OK)
      httpGetResp.bodyAsString should equal (json)
      api.commitStateRequest(req, "")
   }

   "Update existing session" should "create new session" in {
      
      val id = this.getClass.getSimpleName + "key2"
      val ssn = api.getSession(id)
      val req = api.dispatchRequest(ssn, api.getSchema.getState("state4"), "")
      req.getSession shouldBe ssn
      val json = req.getSession.asInstanceOf[VariantSessionImpl].toJson()
      val httpPutResp =  put("/session/" + id, json.getBytes, "application/json") ! "No response from server "
      httpPutResp.code should be (HttpStatus.SC_OK)
      httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0)
      val cacheEntry = SessionCache.get(id)
      cacheEntry should not be (null)
      new String(cacheEntry.getJson) should equal (json)
      val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
      httpGetResp.code should be (HttpStatus.SC_OK)
      httpGetResp.bodyAsString should equal (json)
      api.commitStateRequest(req, "")
   }

   "Session caching" should "preserve traversed tests" in {
      
      val id = this.getClass.getSimpleName + "key3"

      // Get new session
      val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
      httpGetResp.code should be (HttpStatus.SC_OK)
      val json = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
      var ssnIn = VariantSessionImpl.fromJson(json);
      ssnIn.getTraversedStates().toList should be ('empty)
      ssnIn.getTraversedTests().toList should be ('empty)
      ssnIn.getStateRequest should be (null)

      for (state <- "state1" :: "state2" :: "state3" :: "state4" :: "state5" :: Nil) {

         val req = api.dispatchRequest(ssnIn, api.getSchema.getState(state), "")
         val jsonIn = req.getSession.asInstanceOf[VariantSessionImpl].toJson()
         
         val httpPutResp =  put("/session/" + id, jsonIn.getBytes, "application/json") ! "No response from server "
         httpPutResp.code should be (HttpStatus.SC_OK)
         httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
         
         val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
         httpGetResp.code should be (HttpStatus.SC_OK)
         val jsonOut = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
         jsonOut should equal (jsonIn)
         val ssnOut = VariantSessionImpl.fromJson(jsonOut)
         for (testIn <- ssnIn.getTraversedTests) ssnOut.getTraversedTests.exists(p => p.equals(testIn)) should be (true)
        
         api.commitStateRequest(req, "")
      }      
   }

   it should "preserve traversed states" in {
      
      val id = this.getClass.getSimpleName + "key4"

      // Get new session
      val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
      httpGetResp.code should be (HttpStatus.SC_OK)
      val json = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
      var ssnIn = VariantSessionImpl.fromJson(json);
      ssnIn.getTraversedStates().toList should be ('empty)
      ssnIn.getTraversedTests().toList should be ('empty)
      ssnIn.getStateRequest should be (null)
      
      for (state <- "state1" :: "state2" :: "state3" :: "state4" :: "state5" :: Nil) {
         
         val req = api.dispatchRequest(ssnIn, api.getSchema.getState(state), "")
         val jsonIn = req.getSession.asInstanceOf[VariantSessionImpl].toJson()
         
         val httpPutResp =  put("/session/" + id, jsonIn.getBytes, "application/json") ! "No response from server "
         httpPutResp.code should be (HttpStatus.SC_OK)
         httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
         
         val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
         httpGetResp.code should be (HttpStatus.SC_OK)
         val jsonOut = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
         jsonOut should equal (jsonIn)
         val ssnOut = VariantSessionImpl.fromJson(jsonOut)
         for (stateIn <- ssnIn.getTraversedStates) ssnOut.getTraversedStates.exists(p => p.equals(stateIn)) should be (true)
        
         api.commitStateRequest(req, "")
      }
   }

   it should "preserve targeted experiences" in {
      
      val id = this.getClass.getSimpleName + "key5"

      // Get new session
      val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
      httpGetResp.code should be (HttpStatus.SC_OK)
      val json = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
      var ssnIn = VariantSessionImpl.fromJson(json);
      ssnIn.getTraversedStates().toList should be ('empty)
      ssnIn.getTraversedTests().toList should be ('empty)
      ssnIn.getStateRequest should be (null)
      
      for (state <- "state1" :: "state2" :: "state3" :: "state4" :: "state5" :: Nil) {
         
         val req = api.dispatchRequest(ssnIn, api.getSchema.getState(state), "")
         val jsonIn = req.getSession.asInstanceOf[VariantSessionImpl].toJson()
         
         val httpPutResp =  put("/session/" + id, jsonIn.getBytes, "application/json") ! "No response from server "
         httpPutResp.code should be (HttpStatus.SC_OK)
         httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
         
         val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
         httpGetResp.code should be (HttpStatus.SC_OK)
         val jsonOut = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
         jsonOut should equal (jsonIn)
         val ssnOut = VariantSessionImpl.fromJson(jsonOut)
         ssnOut should not be (null)
         val reqOutBeforeCommit = ssnOut.getStateRequest()
         reqOutBeforeCommit should not be (null)
         for (test <- api.getSchema.getState(state).getInstrumentedTests) {
            reqOutBeforeCommit.getTargetedExperiences.exists { exp => exp.getTest equals test } should be (true)
         }
         for (exp <- reqOutBeforeCommit.getTargetedExperiences) {
            api.getSchema.getState(state).getInstrumentedTests.exists { t => exp.getTest equals t } should be (true)
         }

         api.commitStateRequest(req, "")
         
         val reqOutAfterCommit = ssnOut.getStateRequest()
         reqOutAfterCommit shouldBe reqOutBeforeCommit
         for (test <- api.getSchema.getState(state).getInstrumentedTests) {
            reqOutAfterCommit.getTargetedExperiences.exists { exp => exp.getTest equals test } should be (true)
         }
         for (exp <- reqOutAfterCommit.getTargetedExperiences) {
            api.getSchema.getState(state).getInstrumentedTests.exists { t => exp.getTest equals t } should be (true)
         }

      }      
   }
   
   it should "preserve targeted experiences when they are empty list if we hit a state with no instrumented tests" in {
      
      val id = this.getClass.getSimpleName + "key6"

      // Get new session
      val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
      httpGetResp.code should be (HttpStatus.SC_OK)
      val json = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
      var ssnIn = VariantSessionImpl.fromJson(json);
      ssnIn.getTraversedStates().toList should be ('empty)
      ssnIn.getTraversedTests().toList should be ('empty)
      ssnIn.getStateRequest should be (null)
      
      // disqual all the tests instrumented on state4
      api.addHookListener(new DisqualAllHookListener())
      
      for (state <- "state1" :: "state2" :: "state3" :: "state4" :: "state5" :: Nil) {
         
         val req = api.dispatchRequest(ssnIn, api.getSchema.getState(state), "")
         val jsonIn = req.getSession.asInstanceOf[VariantSessionImpl].toJson()
         req.getTargetedExperiences should be ('empty)
         
         val httpPutResp =  put("/session/" + id, jsonIn.getBytes, "application/json") ! "No response from server "
         httpPutResp.code should be (HttpStatus.SC_OK)
         httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
         
         val httpGetResp = get("/session/" + id) !@ "Jetty is not running"
         httpGetResp.code should be (HttpStatus.SC_OK)
         val jsonOut = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
         jsonOut should equal (jsonIn)
         val ssnOut = VariantSessionImpl.fromJson(jsonOut)
         ssnOut should not be (null)
         val reqOut = ssnOut.getStateRequest
         reqOut should not be (null)
         reqOut.getTargetedExperiences should be ('empty)
         
         api.commitStateRequest(req, "")
      }
      
      /**
       * Qualifer hook listener.  Disqualifies everything.
       */
      class DisqualAllHookListener extends HookListener[TestQualificationHook] {

   		override def getHookClass: Class[TestQualificationHook] = {
   			return classOf[TestQualificationHook];
   		}
   
   		override def post(hook: TestQualificationHook ) = {
   		   hook.setQualified(false)
   		}
      }
   }
}
