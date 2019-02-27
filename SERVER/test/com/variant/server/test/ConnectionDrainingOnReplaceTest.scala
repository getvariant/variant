package com.variant.server.test

import play.api.Logger
import play.api.test.Helpers._
import com.variant.core.error.ServerError._
import com.variant.server.api.ConfigKeys._
import com.variant.server.schema.SchemaGen.State._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.spec.TempSchemataDir._
import com.variant.server.test.spec.EmbeddedServerAsyncSpec
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.controller.SessionTest
import com.variant.core.util.IoUtils
import com.variant.core.util.StringUtils
import scala.util.Random
import play.api.libs.json._


/**
 * Test session drainage.
 */
class ConnectionDrainingOnReplaceTest extends EmbeddedServerAsyncSpec with TempSchemataDir {
      
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
         server.schemata.get("monstrosity").get.liveGen.isDefined mustBe true
         server.schemata.get("petclinic").get.liveGen.isDefined mustBe true
                  
         // Let the directory watcher thread start before copying any files.
   	   Thread.sleep(100)
   
	   }

	   val ssnId2Big = new Array[String](SESSIONS)
	   val ssnId2Pet = new Array[String](SESSIONS)

	   "open SESSIONS sessionns in each of the connections" in {
         
         for (i <- 0 until SESSIONS) async {

            val sid = newSid
	         ssnId2Big(i) = sid
            assertResp(route(app, httpReq(POST, "/session/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId mustNot be (sid)
                  ssnId2Big(i) = ssn.getId
                  ssn.getSchema().getMeta().getName mustBe "monstrosity"
               }
         }
         
         for (i <- 0 until SESSIONS) async {

            val sid = newSid
	         ssnId2Pet(i) = sid
            assertResp(route(app, httpReq(POST, "/session/petclinic/" + sid).withBody(emptyTargetingTrackerBody)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId mustNot be (sid)
                  ssnId2Pet(i) = ssn.getId
                  ssn.getSchema().getMeta().getName mustBe "petclinic"
               }
         }
	      
	      joinAll // blocks until all sessions are created
	   }

      "all sessions must be readable over all connections" in {
         	   
         // pick connection randomly to emulate parallel connections.
         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Big(i)
            assertResp(route(app, httpReq(GET, "/session/monstrosity/" + sid)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId must be (sid)
                  ssn.getSchema().getMeta().getName mustBe "monstrosity"
               }
         }
         
         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Pet(i)
            assertResp(route(app, httpReq(GET, "/session/petclinic/" + sid)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId must be (sid)
                  ssn.getSchema().getMeta().getName mustBe "petclinic"
            }
         }
	      
	      joinAll
   	}

      "replace schema ParserConjointOkayBigTestNoHooks" in {

	      val oldGen = server.schemata.get("monstrosity").get.liveGen.get
	      oldGen.state mustBe Live

         async {
   	      
            IoUtils.fileCopy("schemata-test/monster.schema", s"${schemataDir}/monster.schema");
            Thread.sleep(dirWatcherLatencyMsecs)
            
            oldGen.state mustBe Dead

            val newGen = server.schemata.get("monstrosity").get.liveGen.get
            newGen.id mustNot be (oldGen.id)
            newGen.state mustBe Live
            server.schemata.size mustBe 2

	      }
	   }

      "permit session reads over dead generation" in {

         // pick connection randomly to emulate parallel connections.
         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Big(i)
            assertResp(route(app, httpReq(GET, "/session/monstrosity/" + sid)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId must be (sid)
                  ssn.getSchema().getMeta().getName mustBe "monstrosity"
            }
         }

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Pet(i)
            assertResp(route(app, httpReq(GET, "/session/petclinic/" + sid)))
               .is(OK)
               .withBodySession { ssn =>
                  ssn.getId must be (sid)
                  ssn.getSchema().getMeta().getName mustBe "petclinic"
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
            assertResp(route(app, httpReq(PUT, "/session/attr").withBody(body.toString())))
               .isOk
               .withBodySession  { ssn =>
                  ssn.getAttributes.get("foo") mustBe null
               }
            server.ssnStore.get(sid).get.getAttributes().get("foo") mustBe "bar"
         }
         
         joinAll
   	}

      "allow session create over live connection" in {
         
         val sid = newSid
         var actualSid: String = null
         assertResp(route(app, httpReq(POST, "/session/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
            .is(OK)
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               actualSid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "monstrosity"
         }

         // And read, just in case
         assertResp(route(app, httpReq(GET, "/session/monstrosity/" + actualSid)))
            .is(OK)
            .withBodySession { ssn =>
               ssn.getId must be (actualSid)
               ssn.getSchema().getMeta().getName mustBe "monstrosity"
         }

      }
      
      val newsid = newSid
      
      "expire all sessions and dispose of all dead gens" in {
       
         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis)
         
         for (i <- 0 until SESSIONS) {
            val sid = ssnId2Big(i)
            assertResp(route(app, httpReq(GET, "/session/monstrosity/" + sid)))
               .isError(SESSION_EXPIRED, sid)
         }

         for (i <- 0 until SESSIONS) {
            val sid = ssnId2Pet(i)
            assertResp(route(app, httpReq(GET, "/session/petclinic/" + sid)))
               .isError(SESSION_EXPIRED, sid)
         }
         
      }
      
      // TODO: test that dead generations were vacuumed.

   }
}
