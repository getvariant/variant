package com.variant.server.test

import play.api.Logger
import play.api.test.Helpers._
import com.variant.core.impl.ServerError._
import com.variant.server.api.ConfigKeys._
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.spec.TempSchemataDir._
import com.variant.server.test.spec.BaseSpecWithServerAsync
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.controller.SessionTest
import com.variant.core.util.IoUtils
import com.variant.core.util.StringUtils
import scala.util.Random
import play.api.libs.json.Json


/**
 * Test session drainage.
 */
class ConnectionDrainingOnDeleteTest extends BaseSpecWithServerAsync with TempSchemataDir {
      
   private val logger = Logger(this.getClass)
   private val random = new Random(System.currentTimeMillis())
   private val SESSIONS = 100 

   private val sessionJsonBig = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
   private val sessionJsonPet = ParameterizedString(SessionTest.sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))
   
   // Override the test default of 10
   override val config = "variant.max.concurrent.connections = 100"
   
   val sessionTimeoutMillis = server.config.getInt(SESSION_TIMEOUT) * 1000
   val vacuumIntervalMillis = server.config.getInt(SESSION_VACUUM_INTERVAL) * 1000
   
   "Confirm key settings" in {
   
      sessionTimeoutMillis mustBe 15000 
      vacuumIntervalMillis mustBe 1000
      dirWatcherLatencyMsecs mustBe 10000   
   }
   
   /**
    * 
    */
   "File System Schema Deployer on schema DELETE" should {
 
	   "startup with two schemata" in {
	      
         server.schemata.size mustBe 2
         server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.isDefined mustBe true 
         server.schemata.get("petclinic_experiments").get.liveGen.isDefined mustBe true
                  
         // Let the directory watcher thread start before copying any files.
   	   Thread.sleep(100)
	   }

	   "obtain concurrent connection to ParserConjointOkayBigTestNoHooks" in {

	      for (i <- 0 until 20) async { 
            assertResp(route(app, httpReq(GET, "/connection/ParserConjointOkayBigTestNoHooks")))
              .isOk
              .withNoBody
	      }
      }

      "obtain concurrent connections to petclinic_experiments" in {

	      for (i <- 0 until 20) async {	      
            assertResp(route(app, httpReq(GET, "/connection/petclinic_experiments")))
              .isOk
              .withNoBody
	      }
      }
	   
      "CONNECTIONS*2 connetions must be started" in {
         
   	   joinAll() // blocks until all async blocks are completed.
   	    
      }
	   
	   val ssnId2Big = new Array[String](SESSIONS)
	   val ssnId2Pet = new Array[String](SESSIONS)

	   "open SESSIONS sessionns to each schema" in {
         
         for (j <- 0 until SESSIONS) async {

            val sid = newSid
	         ssnId2Big(j) = sid
	         val body = sessionJsonBig.expand("sid" -> sid)
            assertResp(route(app, httpReq(PUT, context + "/session").withBody(body)))
               .is(OK)
               .withNoBody
         }
	      
         for (j <- 0 until SESSIONS) async {

            val sid = newSid
	         ssnId2Pet(j) = sid
	         val body = sessionJsonPet.expand("sid" -> sid)
            assertResp(route(app, httpReq(PUT, context + "/session").withBody(body)))
               .is(OK)
               .withNoBody
         }
	      
	      joinAll // blocks until all sessions are created by async blocks above
	   }

      "all sessions must be readable" in {
         	   
         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Big(i)
            assertResp(route(app, httpReq(GET, context + "/session/" + sid)))
               .is(OK)
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonBig.expand("sid" -> sid).toString())
            }
         }
         
         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Pet(i)
            assertResp(route(app, httpReq(GET, context + "/session/" + sid)))
               .is(OK)
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonPet.expand("sid" -> sid).toString())
            }
         }
	      
	      joinAll
   	}

      "delete schema ParserConjointOkayBigTestNoHooks" in {

	      val oldSchema = server.schemata.get("ParserConjointOkayBigTestNoHooks").get
         oldSchema.liveGen.isDefined mustBe true

         async {
   	      
	         IoUtils.delete(s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
            Thread.sleep(dirWatcherLatencyMsecs)
            
            oldSchema.liveGen.isDefined mustBe false
            server.schemata.get("ParserConjointOkayBigTestNoHooks") mustBe None
	      }
	   }

      "permit session reads over dead generations" in {

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Big(i)
            assertResp(route(app, httpReq(GET, context + "/session/" + sid)))
               .is(OK)
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonBig.expand("sid" -> sid).toString())
            }
         }

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Pet(i)
            assertResp(route(app, httpReq(GET, context + "/session/" + sid)))
               .is(OK)
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonPet.expand("sid" -> sid).toString())
            }
         }
         
         joinAll

         // Confirm the schema is gone.
         server.schemata.size mustBe 1
         server.schemata.get("ParserConjointOkayBigTestNoHooks") mustBe None

   	}

      "permit session updates over dead generations " in {
     
         for (i <- 0 until SESSIONS) {
	         val body = sessionJsonBig.expand("sid" -> ssnId2Big(i))
            assertResp(route(app, httpReq(PUT, context + "/session").withBody(body)))
               .is(OK)
               .withNoBody
         }

         for (i <- 0 until SESSIONS) {
	         val body = sessionJsonPet.expand("sid" -> ssnId2Pet(i))
            assertResp(route(app, httpReq(PUT, context + "/session").withBody(body)))
               .is(OK)
               .withNoBody
         }

         joinAll
   	}

      "permit session create over live generations" in {
         
         // Create
         val sid = newSid
         assertResp(route(app, httpReq(POST, context + "/session")))
            .is(OK)
            .withNoBody

         // And read, just in case
         assertResp(route(app, httpReq(POST, context + "/session/" + sid)))
            .is(OK)
            .withBodyJson { json => 
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest("what to do here?")
         }
      }
      
      "refuse session create over dead generation" in {

         val sid = newSid
         val body = sessionJsonBig.expand("sid" -> sid)

         // Create
         assertResp(route(app, httpReq(POST, context + "/session")))
            .isError(UnknownSchema, "WHICH?")

      }

      "expire all sessions and dispose of all dead generations after session timeout period" in {
       
         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis)
         
         for (i <- 0 until SESSIONS) {
            val sid = ssnId2Big(i)
            assertResp(route(app, httpReq(GET, context + "/session/" + sid)))
               .isError(SessionExpired, sid)
         }

         for (i <- 0 until SESSIONS) {
            val sid = ssnId2Pet(i)
            assertResp(route(app, httpReq(GET, context + "/session/" + sid)))
               .isError(SessionExpired, sid)
         }
         
         // TODO: test that dead generations were vacuumed.
      }
   }
}
