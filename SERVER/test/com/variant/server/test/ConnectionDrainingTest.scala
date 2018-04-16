package com.variant.server.test

import play.api.Logger
import play.api.test.Helpers._
import com.variant.core.ConnectionStatus._
import com.variant.server.schema.State
import com.variant.server.api.ConfigKeys._
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.spec.TempSchemataDir._
import com.variant.server.test.spec.BaseSpecWithServerAsync
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.controller.SessionTest
import com.variant.core.util.IoUtils


/**
 * Test session drainage.
 */
class ConnectionDrainingTest extends BaseSpecWithServerAsync with TempSchemataDir {
      
   private val logger = Logger(this.getClass)
   private val CONNECTIONS = 10 
   private val SESSIONS = 20 // per connection 

   private val sessionJsonBig = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
   private val sessionJsonPet = ParameterizedString(SessionTest.sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))
   
   // Override the test default of 10
   override val config = "variant.max.concurrent.connections = 100"
   
   "Confirm key settings" in {
   
      server.config.getInt(SESSION_TIMEOUT) mustBe 15  // Secs
      dirWatcherLatencyMsecs mustBe 10000              // Millis
   }
   
   /**
    * 
    */
   "File System Schema Deployer" should {
 
	   "startup with two schemata" in {
	      
         server.schemata.size mustBe 2
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
                  
         // Let the directory watcher thread start before copying any files.
   	   Thread.sleep(100)
	   }

	   //val connId2Big = new java.util.concurrent.ConcurrentLinkedQueue[String]
	   //val connId2Pet = new java.util.concurrent.ConcurrentLinkedQueue[String]

	   val connId2Big = new Array[String](CONNECTIONS)
	   val connId2Pet = new Array[String](CONNECTIONS)

	   "obtain CONNECTIONS connections to ParserCovariantOkayBigTestNoHooks" in {

	      for (i <- 0 until CONNECTIONS) async {   
            assertResp(route(app, connectionRequest("ParserCovariantOkayBigTestNoHooks")))
              .isOk
              .withConnStatusHeader(OPEN)
              .withBodyJson { json =>
                  val conn = (json \ "id").as[String]
                  conn mustNot be (null)
                  connId2Big(i) = conn
               }
	      }
      }

      "obtain CONNECTIONS connections to petclinic" in {

	      for (i <- 0 until CONNECTIONS) async {	      
            assertResp(route(app, connectionRequest("petclinic")))
              .isOk
              .withConnStatusHeader(OPEN)
              .withBodyJson { json =>
                  val conn = (json \ "id").as[String]
                  conn mustNot be (null)
                  connId2Pet(i) = conn
               }
	      }
      }
	   
      "CONNECTIONS*2 connetions must be started" in {
         
   	   joinAll() // blocks until all async blocks are completed.
   	   
   	   connId2Big.size mustBe CONNECTIONS
   	   connId2Pet.size mustBe CONNECTIONS
      }
	   
	   val ssnId2Big = Array.ofDim[String](CONNECTIONS, SESSIONS)
	   val ssnId2Pet = Array.ofDim[String](CONNECTIONS, SESSIONS)

	   "open SESSIONS sessionns in each of the connections" in {
         
	      for (i <- 0 until CONNECTIONS) {
	         for (j <- 0 until SESSIONS) async {
   
	            val sid = newSid
   	         ssnId2Big(i)(j) = sid
   	         val body = sessionJsonBig.expand("sid" -> sid)
               assertResp(route(app, connectedRequest(PUT, context + "/session", connId2Big(i)).withBody(body)))
                  .is(OK)
                  .withConnStatusHeader(OPEN)
                  .withNoBody
            }
	      }
	      
	      for (i <- 0 until CONNECTIONS) {
	         for (j <- 0 until SESSIONS) async {
   
	            val sid = newSid
   	         ssnId2Pet(i)(j) = sid
   	         val body = sessionJsonPet.expand("sid" -> sid)
               assertResp(route(app, connectedRequest(PUT, context + "/session", connId2Pet(i)).withBody(body)))
                  .is(OK)
                  .withConnStatusHeader(OPEN)
                  .withNoBody
            }
	      }
	      
	      joinAll // blocks until all sessions are created by async blocks above
	   }

      "SESSIONS*CONNECTIONS*2 sessions must be opened" in {
         	   
	      for (i <- 0 until CONNECTIONS) {	         
	         for (j <- 0 until SESSIONS) async {
   	         val body = sessionJsonBig.expand("sid" -> ssnId2Big(i)(j))
               assertResp(route(app, connectedRequest(PUT, context + "/session", connId2Big(i)).withBody(body)))
                  .is(OK)
                  .withConnStatusHeader(OPEN)
                  .withNoBody
            }
	         
	         for (j <- 0 until SESSIONS) async {
   	         val body = sessionJsonPet.expand("sid" -> ssnId2Pet(i)(j))
               assertResp(route(app, connectedRequest(PUT, context + "/session", connId2Pet(i)).withBody(body)))
                  .is(OK)
                  .withConnStatusHeader(OPEN)
                  .withNoBody
            }

	      }
   	}

      "delete schema ParserCovariantOkayBigTestNoHooks" in {

	      val schema = server.schemata.get("ParserCovariantOkayBigTestNoHooks").get
         schema.state mustBe State.Deployed

         async {
   	      
	         IoUtils.delete(s"${schemataDir}/ParserCovariantOkayBigTestNoHooks.json");
            Thread.sleep(dirWatcherLatencyMsecs)
            
            // Schema is gone
            schema.state mustBe State.Gone
            server.schemata.get("ParserCovariantOkayBigTestNoHooks") mustBe None
            
	      }
	      joinAll
	   }

      "permit session reads over the draining connection" in {
            	   
	      for (i <- 0 until CONNECTIONS) {	         
	         for (j <- 0 until SESSIONS) async {
   	         val body = sessionJsonBig.expand("sid" -> ssnId2Big(i)(j))
               assertResp(route(app, connectedRequest(PUT, context + "/session", connId2Big(i)).withBody(body)))
                  .is(OK)
                  .withConnStatusHeader(OPEN)
                  .withNoBody
            }
	         
	         for (j <- 0 until SESSIONS) async {
   	         val body = sessionJsonPet.expand("sid" -> ssnId2Pet(i)(j))
               assertResp(route(app, connectedRequest(PUT, context + "/session", connId2Pet(i)).withBody(body)))
                  .is(OK)
                  .withConnStatusHeader(OPEN)
                  .withNoBody
            }
	      }
         joinAll
   	}

      "permit session reads over the live connection" in {
            	   
	      for (i <- 0 until CONNECTIONS) {	         
	         for (j <- 0 until SESSIONS) async {
   	         val body = sessionJsonBig.expand("sid" -> ssnId2Big(i)(j))
               assertResp(route(app, connectedRequest(PUT, context + "/session", connId2Big(i)).withBody(body)))
                  .is(OK)
                  .withConnStatusHeader(OPEN)
                  .withNoBody
            }
	         
	         for (j <- 0 until SESSIONS) async {
   	         val body = sessionJsonPet.expand("sid" -> ssnId2Pet(i)(j))
               assertResp(route(app, connectedRequest(PUT, context + "/session", connId2Pet(i)).withBody(body)))
                  .is(OK)
                  .withConnStatusHeader(OPEN)
                  .withNoBody
            }
	      }
         joinAll
   	}

   }
}
