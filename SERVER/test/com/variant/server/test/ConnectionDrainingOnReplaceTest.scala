package com.variant.server.test

import play.api.Logger
import play.api.test.Helpers._
import com.variant.core.impl.ServerError._
import com.variant.server.api.ConfigKeys._
import com.variant.server.schema.SchemaGen.State._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.spec.TempSchemataDir._
import com.variant.server.test.spec.BaseSpecWithServerAsync
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.controller.SessionTest
import com.variant.core.util.IoUtils
import com.variant.core.util.StringUtils
import scala.util.Random
import play.api.libs.json._


/**
 * Test session drainage.
 */
class ConnectionDrainingOnReplaceTest extends BaseSpecWithServerAsync with TempSchemataDir {
      
   private val logger = Logger(this.getClass)
   private val random = new Random(System.currentTimeMillis())
   private val SESSIONS = 100

   //private val sessionJsonBig = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
   //private val sessionJsonPet = ParameterizedString(SessionTest.sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))
      
   val sessionTimeoutMillis = server.config.getInt(SESSION_TIMEOUT) * 1000
   val vacuumIntervalMillis = server.config.getInt(SESSION_VACUUM_INTERVAL) * 1000
   
   "Confirm key settings" in {
   
      sessionTimeoutMillis mustBe 15000 
      vacuumIntervalMillis mustBe 1000
      dirWatcherLatencyMsecs mustBe 10000   
   }
   
   val emptyTargetingTrackerBody = "{\"tt\":[]}"
   
   /**
    * 
    */
   "File System Schema Deployer on schema ADD" should {
 
	   "startup with two schemata" in {
	      
         server.schemata.size mustBe 2
         server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.isDefined mustBe true
         server.schemata.get("petclinic_experiments").get.liveGen.isDefined mustBe true
                  
         // Let the directory watcher thread start before copying any files.
   	   Thread.sleep(100)
   
	   }

	   val ssnId2Big = new Array[String](SESSIONS)
	   val ssnId2Pet = new Array[String](SESSIONS)

	   "open SESSIONS sessionns in each of the connections" in {
         
         for (i <- 0 until SESSIONS) async {

            val sid = newSid
	         ssnId2Big(i) = sid
            assertResp(route(app, httpReq(POST, context + "/session/ParserConjointOkayBigTestNoHooks/" + sid).withBody(emptyTargetingTrackerBody)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId mustNot be (sid)
                  ssnId2Big(i) = ssn.getId
                  ssn.getSchema().getMeta().getName mustBe "ParserConjointOkayBigTestNoHooks"
               }
         }
         
         for (i <- 0 until SESSIONS) async {

            val sid = newSid
	         ssnId2Pet(i) = sid
            assertResp(route(app, httpReq(POST, context + "/session/petclinic_experiments/" + sid).withBody(emptyTargetingTrackerBody)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId mustNot be (sid)
                  ssnId2Pet(i) = ssn.getId
                  ssn.getSchema().getMeta().getName mustBe "petclinic_experiments"
               }
         }
	      
	      joinAll // blocks until all sessions are created
	   }

      "all sessions must be readable over all connections" in {
         	   
         // pick connection randomly to emulate parallel connections.
         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Big(i)
            assertResp(route(app, httpReq(GET, context + "/session/ParserConjointOkayBigTestNoHooks/" + sid)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId must be (sid)
                  ssn.getSchema().getMeta().getName mustBe "ParserConjointOkayBigTestNoHooks"
               }
         }
         
         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Pet(i)
            assertResp(route(app, httpReq(GET, context + "/session/petclinic_experiments/" + sid)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId must be (sid)
                  ssn.getSchema().getMeta().getName mustBe "petclinic_experiments"
            }
         }
	      
	      joinAll
   	}

      "replace schema ParserConjointOkayBigTestNoHooks" in {

	      val oldGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
	      oldGen.state mustBe Live

         async {
   	      
            IoUtils.fileCopy("conf-test/ParserConjointOkayBigTestNoHooks.json", s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
            Thread.sleep(dirWatcherLatencyMsecs)
            
            oldGen.state mustBe Dead

            val newGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
            newGen.id mustNot be (oldGen.id)
            newGen.state mustBe Live
            server.schemata.size mustBe 2

	      }
	   }

      "permit session reads over dead generation" in {

         // pick connection randomly to emulate parallel connections.
         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Big(i)
            assertResp(route(app, httpReq(GET, context + "/session/ParserConjointOkayBigTestNoHooks/" + sid)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId must be (sid)
                  ssn.getSchema().getMeta().getName mustBe "ParserConjointOkayBigTestNoHooks"
            }
         }

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Pet(i)
            assertResp(route(app, httpReq(GET, context + "/session/petclinic_experiments/" + sid)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId must be (sid)
                  ssn.getSchema().getMeta().getName mustBe "petclinic_experiments"
            }
         }
         
         joinAll

   	}

      "permit session updates over draining connections, after schema was replaced" in {
     
         for (i <- 0 until SESSIONS) {
            val sid = ssnId2Big(i)
            val body: JsValue = Json.obj(
               "sid" -> sid,
               "name" -> "foo",
               "value" -> "bar"
            )
            assertResp(route(app, httpReq(PUT, context + "/session/attr").withBody(body.toString())))
               .isOk
               .withBodySession  { ssn =>
                  ssn.getAttribute("foo") mustBe "bar"
               }
         }

         joinAll
   	}

      "allow session create over live connection" in {
         
         val sid = newSid
         var actualSid: String = null
         assertResp(route(app, httpReq(POST, context + "/session/ParserConjointOkayBigTestNoHooks/" + sid).withBody(emptyTargetingTrackerBody)))
            .is(OK)
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               actualSid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "ParserConjointOkayBigTestNoHooks"
         }

         // And read, just in case
         assertResp(route(app, httpReq(GET, context + "/session/ParserConjointOkayBigTestNoHooks/" + actualSid)))
            .is(OK)
            .withBodySession { ssn =>
               ssn.getId must be (actualSid)
               ssn.getSchema().getMeta().getName mustBe "ParserConjointOkayBigTestNoHooks"
         }

      }
      
      val newsid = newSid
      
      "expire all sessions and dispose of all dead gens" in {
       
         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis)
         
         for (i <- 0 until SESSIONS) {
            val sid = ssnId2Big(i)
            assertResp(route(app, httpReq(GET, context + "/session/ParserConjointOkayBigTestNoHooks/" + sid)))
               .isError(SESSION_EXPIRED, sid)
         }

         for (i <- 0 until SESSIONS) {
            val sid = ssnId2Pet(i)
            assertResp(route(app, httpReq(GET, context + "/session/petclinic_experiments/" + sid)))
               .isError(SESSION_EXPIRED, sid)
         }
         
      }
      
      // TODO: test that dead generations were vacuumed.

   }
}
