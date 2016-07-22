package com.variant.server.test

import net.liftweb.http.testing.{TestKit,ReportFailure,HttpResponse}
import com.variant.server.util.JettyStartupAndShutdown
import com.variant.server.util.JettyTestServer
import com.variant.server.util.UnitSpec
import com.variant.server.SessionCache
import org.apache.http.HttpStatus
import com.variant.core.schema.Schema
import scala.collection.JavaConversions._
import UnitSpec._
import com.variant.core.hook.TestQualificationHook
import com.variant.core.hook.HookListener
import scala.util.Random
import net.liftweb.json.JsonAST.JValue
import com.variant.core.impl.CoreSessionImpl
import com.variant.core.net.PayloadReader
import com.variant.core.net.Payload
import com.variant.server.ServerBoot
import com.variant.server.ServerPropertyKeys
import com.variant.core.net.SessionPayloadReader

/**
 */
class ServerSessionTest extends UnitSpec {
    
   "setup" should "run after beforeAll" in {
      
      import net.liftweb.json._
      
      // Change session expiration to 1 second by reading current store class init (as json string),
      // replacing the value and resetting it. We'll need to bounce the api for that to take effect.
      // UPDATE: this is now accomplished via variant-test.props. Keeping this cmmented as an
      // example of JSON manipualtion.
      //val initParams = parse(api.getProperties.get(VariantProperties.Key.SESSION_STORE_CLASS_INIT));
      //System.setProperty(VariantProperties.Key.SESSION_STORE_CLASS_INIT.propName(), compact(render(initParams.replace("sessionTimeoutSecs" :: Nil, JInt(1)))));
      //VariantCore.init("/variant-test.props")

      val parserResp = clientCore.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"))
      parserResp.getMessages should have size (0)      
   }

   "Get non-existent session" should "return no content" in {
      
      val id = this.getClass.getSimpleName + rand.nextInt;
      val httpResp =  get("/session/" + id) ! "No response from server "
      SessionCache.get(id) should be (null)
      httpResp.code should be (HttpStatus.SC_NO_CONTENT)
   }

   "PUT non-existent session" should "create the session" in {
      val id = this.getClass.getSimpleName + rand.nextInt();
      SessionCache.get(id) should be (null)
      val httpGetResp1 = get("/session/" + id) ! "Jetty not running"
      httpGetResp1.code should be (HttpStatus.SC_NO_CONTENT)

      val ssn = new CoreSessionImpl(id, clientCore)
      ssn should not be (null)
      val json = ssn.asInstanceOf[CoreSessionImpl].toJson()
      val httpPutResp =  put("/session/" + id, json.getBytes, "application/json") ! "No response from server "
      httpPutResp.code should be (HttpStatus.SC_OK)
      httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0)
      val cacheEntry = SessionCache.get(id)
      cacheEntry should not be (null)
      new String(cacheEntry.getJson) should equal (json)
      val httpGetResp2 = get("/session/" + id) ! "Jetty is not running"
      httpGetResp2.code should be (HttpStatus.SC_OK)
      val payloadReader = new SessionPayloadReader(clientCore, httpGetResp2.bodyAsString.openOrThrowException("Unexpected null response"))
      payloadReader.getProperty(Payload.Property.SRV_REL) should equal (ServerBoot.getCore.getComptime.getComponentVersion)
      payloadReader.getProperty(Payload.Property.SSN_TIMEOUT) should equal (ServerBoot.getCore.getProperties.get(ServerPropertyKeys.SESSION_TIMEOUT_SECS))
      val ssn2 = payloadReader.getBody();
      ssn2.asInstanceOf[CoreSessionImpl].toJson() should equal (json)
   }

   "PUT expired session" should "quietly reinstate the session" in {
            
      val id = this.getClass.getSimpleName + rand.nextInt();
      SessionCache.get(id) should be (null)      
      val ssn = new CoreSessionImpl(id, clientCore)
      ssn should not be (null)      
      val json = ssn.asInstanceOf[CoreSessionImpl].toJson()
      val httpPutResp1 =  put("/session/" + id, json.getBytes, "application/json") ! "No response from server "
      httpPutResp1.code should be (HttpStatus.SC_OK)
      httpPutResp1.bodyAsString.openOrThrowException("Unexpected null response").length should be (0)
      SessionCache.get(id) should not be (null)      

      Thread.sleep(2000);  // Wait for the vacuum to delete the session.
      SessionCache.get(id) should be (null)
      
      val httpPutResp2 =  put("/session/" + id, json.getBytes, "application/json") ! "No response from server "
      httpPutResp2.code should be (HttpStatus.SC_OK)
      httpPutResp2.bodyAsString.openOrThrowException("Unexpected null response").length should be (0)
      SessionCache.get(id) should not be (null)      
      val cacheEntry = SessionCache.get(id)
      cacheEntry should not be (null)
      new String(cacheEntry.getJson) should equal (json)  
   }

   "PUT on existing session" should "replace the session" in {
 
      val id = this.getClass.getSimpleName + rand.nextInt();
      SessionCache.get(id) should be (null)
      val ssn = new CoreSessionImpl(id, clientCore)
      ssn should not be (null)      
      ssn.getSchemaId should not be (null)
      val httpPutResp1 =  put(
            "/session/" + id, 
            ssn.asInstanceOf[CoreSessionImpl].toJson().getBytes, "application/json"
            ) ! "No response from server "
      httpPutResp1.code should be (HttpStatus.SC_OK)
      val req = ssn.targetForState(clientCore.getSchema.getState("state4"))
      req.getSession shouldBe ssn // Identity
      val json = req.getSession.asInstanceOf[CoreSessionImpl].toJson()
      val httpPutResp2 =  put("/session/" + id, json.getBytes, "application/json") ! "No response from server "
      httpPutResp2.code should be (HttpStatus.SC_OK)
      httpPutResp2.bodyAsString.openOrThrowException("Unexpected null response").length should be (0)
      val cacheEntry = SessionCache.get(id)
      cacheEntry should not be (null)
      new String(cacheEntry.getJson) should equal (json)
      val httpGetResp = get("/session/" + id) ! "Jetty is not running"
      httpGetResp.code should be (HttpStatus.SC_OK)
      val payloadReader = new SessionPayloadReader(clientCore, httpGetResp.bodyAsString.openOrThrowException("Unexpected null response"))
      payloadReader.getProperty(Payload.Property.SRV_REL) should equal (ServerBoot.getCore.getComptime.getComponentVersion)
      payloadReader.getProperty(Payload.Property.SSN_TIMEOUT) should equal (ServerBoot.getCore.getProperties.get(ServerPropertyKeys.SESSION_TIMEOUT_SECS))
      val ssn2 = payloadReader.getBody
      ssn2.asInstanceOf[CoreSessionImpl].toJson() should equal (json)
   }

   "Session storage" should "preserve traversed tests" in {
      
      val id = this.getClass.getSimpleName + rand.nextInt();

      val ssnIn = new CoreSessionImpl(id, clientCore)
      ssnIn.getTraversedStates().toList should be ('empty)
      ssnIn.getTraversedTests().toList should be ('empty)
      ssnIn.getStateRequest should be (null)

      for (state <- "state1" :: "state2" :: "state3" :: "state4" :: "state5" :: Nil) {

         val req = ssnIn.targetForState(clientCore.getSchema.getState(state))
         val jsonIn = req.getSession.asInstanceOf[CoreSessionImpl].toJson()
         
         val httpPutResp =  put("/session/" + id, jsonIn.getBytes, "application/json") ! "No response from server "
         httpPutResp.code should be (HttpStatus.SC_OK)
         httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
         
         val httpGetResp = get("/session/" + id) ! "Jetty is not running"
         httpGetResp.code should be (HttpStatus.SC_OK)
         val payloadReader = new SessionPayloadReader(clientCore, httpGetResp.bodyAsString.openOrThrowException("Unexpected null response"))
         payloadReader.getProperty(Payload.Property.SRV_REL) should equal (ServerBoot.getCore.getComptime.getComponentVersion)
         payloadReader.getProperty(Payload.Property.SSN_TIMEOUT) should equal (ServerBoot.getCore.getProperties.get(ServerPropertyKeys.SESSION_TIMEOUT_SECS))
         val ssnOut = payloadReader.getBody
         ssnOut.asInstanceOf[CoreSessionImpl].toJson() should equal (jsonIn)
         for (testIn <- ssnIn.getTraversedTests) ssnOut.getTraversedTests.exists(p => p.equals(testIn)) should be (true)
         req.commit()
      }
   }

   it should "preserve traversed states" in {
      
      val id = this.getClass.getSimpleName + rand.nextInt();
      val ssnIn = new CoreSessionImpl(id, clientCore)
      ssnIn.getTraversedStates().toList should be ('empty)
      ssnIn.getTraversedTests().toList should be ('empty)
      ssnIn.getStateRequest should be (null)
      
      for (state <- "state1" :: "state2" :: "state3" :: "state4" :: "state5" :: Nil) {
         
         val req = ssnIn.targetForState(clientCore.getSchema.getState(state))
         val jsonIn = req.getSession.asInstanceOf[CoreSessionImpl].toJson()
         
         val httpPutResp =  put("/session/" + id, jsonIn.getBytes, "application/json") ! "No response from server "
         httpPutResp.code should be (HttpStatus.SC_OK)
         httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
         
         val httpGetResp = get("/session/" + id) ! "Jetty is not running"
         httpGetResp.code should be (HttpStatus.SC_OK)
         val jsonOut = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
         val payloadReader = new SessionPayloadReader(clientCore, httpGetResp.bodyAsString.openOrThrowException("Unexpected null response"))
         payloadReader.getProperty(Payload.Property.SRV_REL) should equal (ServerBoot.getCore.getComptime.getComponentVersion)
         payloadReader.getProperty(Payload.Property.SSN_TIMEOUT) should equal (ServerBoot.getCore.getProperties.get(ServerPropertyKeys.SESSION_TIMEOUT_SECS))
         val ssnOut = payloadReader.getBody
         ssnOut.asInstanceOf[CoreSessionImpl].toJson() should equal (jsonIn)
         for (stateIn <- ssnIn.getTraversedStates) ssnOut.getTraversedStates.exists(p => p.equals(stateIn)) should be (true)
        
         req.commit()
      }
   }

   it should "preserve targeted experiences" in {
      
      val id = this.getClass.getSimpleName + rand.nextInt();
      val ssnIn = new CoreSessionImpl(id, clientCore)
      ssnIn.getTraversedStates().toList should be ('empty)
      ssnIn.getTraversedTests().toList should be ('empty)
      ssnIn.getStateRequest should be (null)
      
      for (state <- "state1" :: "state2" :: "state3" :: "state4" :: "state5" :: Nil) {
         
         val req = ssnIn.targetForState(clientCore.getSchema.getState(state))
         val jsonIn = req.getSession.asInstanceOf[CoreSessionImpl].toJson()
         
         val httpPutResp =  put("/session/" + id, jsonIn.getBytes, "application/json") ! "No response from server "
         httpPutResp.code should be (HttpStatus.SC_OK)
         httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
         
         val httpGetResp = get("/session/" + id) ! "Jetty is not running"
         httpGetResp.code should be (HttpStatus.SC_OK)
         val jsonOut = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
         val payloadReader = new SessionPayloadReader(clientCore, httpGetResp.bodyAsString.openOrThrowException("Unexpected null response"))
         payloadReader.getProperty(Payload.Property.SRV_REL) should equal (ServerBoot.getCore.getComptime.getComponentVersion)
         payloadReader.getProperty(Payload.Property.SSN_TIMEOUT) should equal (ServerBoot.getCore.getProperties.get(ServerPropertyKeys.SESSION_TIMEOUT_SECS))
         val ssnOut = payloadReader.getBody
         ssnOut.asInstanceOf[CoreSessionImpl].toJson() should equal (jsonIn)
         val reqOutBeforeCommit = ssnOut.getStateRequest()
         reqOutBeforeCommit should not be (null)
         for (test <- clientCore.getSchema.getState(state).getInstrumentedTests) {
            reqOutBeforeCommit.getActiveExperiences.exists { exp => exp.getTest equals test } should be (true)
         }
         for (exp <- reqOutBeforeCommit.getActiveExperiences) {
            clientCore.getSchema.getState(state).getInstrumentedTests.exists { t => exp.getTest equals t } should be (true)
         }

         req.commit()
         
         val reqOutAfterCommit = ssnOut.getStateRequest()
         reqOutAfterCommit shouldBe reqOutBeforeCommit
         for (test <- clientCore.getSchema.getState(state).getInstrumentedTests) {
            reqOutAfterCommit.getActiveExperiences.exists { exp => exp.getTest equals test } should be (true)
         }
         for (exp <- reqOutAfterCommit.getActiveExperiences) {
            clientCore.getSchema.getState(state).getInstrumentedTests.exists { t => exp.getTest equals t } should be (true)
         }

      }      
   }
  
   it should "preserve targeted experiences when they are empty list if we hit a state with no instrumented tests" in {
      
      val id = this.getClass.getSimpleName + rand.nextInt
      val ssnIn = new CoreSessionImpl(id, clientCore)
      ssnIn.getTraversedStates().toList should be ('empty)
      ssnIn.getTraversedTests().toList should be ('empty)
      ssnIn.getStateRequest should be (null)
      
      // Disqualify all the tests instrumented on state4
      clientCore.addHookListener(new DisqualAllHookListener())
      
      for (state <- "state1" :: "state2" :: "state3" :: "state4" :: "state5" :: Nil) {
         
         val req = ssnIn.targetForState(clientCore.getSchema.getState(state))
         val jsonIn = req.getSession.asInstanceOf[CoreSessionImpl].toJson()
         req.getActiveExperiences should be ('empty)
         println("IN " + jsonIn)
         val httpPutResp =  put("/session/" + id, jsonIn.getBytes, "application/json") ! "No response from server "
         httpPutResp.code should be (HttpStatus.SC_OK)
         httpPutResp.bodyAsString.openOrThrowException("Unexpected null response").length should be (0) 
         
         val httpGetResp = get("/session/" + id) ! "Jetty is not running"
         httpGetResp.code should be (HttpStatus.SC_OK)
         val jsonOut = httpGetResp.bodyAsString.openOrThrowException("Unexpected null response")
         val payloadReader = new SessionPayloadReader(clientCore, httpGetResp.bodyAsString.openOrThrowException("Unexpected null response"))
         payloadReader.getProperty(Payload.Property.SRV_REL) should equal (ServerBoot.getCore.getComptime.getComponentVersion)
         payloadReader.getProperty(Payload.Property.SSN_TIMEOUT) should equal (ServerBoot.getCore.getProperties.get(ServerPropertyKeys.SESSION_TIMEOUT_SECS))
         val ssnOut = payloadReader.getBody
         val reqOut = ssnOut.getStateRequest
         reqOut should not be (null)
         reqOut.getActiveExperiences should be ('empty)
         
         req.commit()
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
