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
import com.variant.server.impl.SessionImpl


/**
 * Test session drainage.
 */
class ConnectionDrainingOnDeleteTest extends EmbeddedServerAsyncSpec with TempSchemataDir {
      
   private val logger = Logger(this.getClass)
   private val random = new Random(System.currentTimeMillis())
   private val SESSIONS = 100 
   
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
 
      val emptyTargetingTrackerBody = "{\"tt\":[]}"
 
      "startup with two schemata" in {
	      
         server.schemata.size mustBe 2
         server.schemata.get("monstrosity").get.liveGen.isDefined mustBe true 
         server.schemata.get("petclinic").get.liveGen.isDefined mustBe true
                  
         // Let the directory watcher thread start before copying any files.
   	   Thread.sleep(100)
	   }
	   	   
	   val ssnId2Big = new Array[String](SESSIONS)
	   val ssnId2Pet = new Array[String](SESSIONS)

	   "create SESSIONS sessionns to each schema" in {
         
         for (j <- 0 until SESSIONS) async {

            val sid = newSid
            assertResp(route(app, httpReq(POST, "/session/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
               .is(OK)
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               ssnId2Big(j) = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "monstrosity"
            }
         }
	      
         for (j <- 0 until SESSIONS) async {

            val sid = newSid
            assertResp(route(app, httpReq(POST, "/session/petclinic/" + sid).withBody(emptyTargetingTrackerBody)))
               .is(OK)
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               ssnId2Pet(j) = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "petclinic"
            }
         }
	      
	      joinAll // blocks until all sessions are created by async blocks above
	   }

      "all sessions must be readable" in {
         	   
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

      "delete schema monstrosity" in {

	      val oldGen = server.schemata.getLiveGen("monstrosity").get
         oldGen.state mustBe Live

         async {
   	      
	         IoUtils.delete(s"${schemataDir}/monster.schema");
            Thread.sleep(dirWatcherLatencyMsecs)
            
            oldGen.state mustBe Dead
            server.schemata.getLiveGen("monstrosity") mustBe None
	      }
	   }

      "permit session reads in both live and dead generations" in {

         for (i <- 0 until SESSIONS) async {
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
            server.ssnStore.get(sid).get.getAttributes.get("foo") mustBe "bar"
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

         // Confirm the schema is gone.
         server.schemata.size mustBe 1
         server.schemata.getLiveGen("monstrosity") mustBe None
         server.schemata.getLiveGen("petclinic").isDefined mustBe true

   	}

      "permit session updates over dead generations " in {
     
         val sessionJsonBig = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
         val sessionJsonPet = ParameterizedString(SessionTest.sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))
   
         for (i <- 0 until SESSIONS) {
	         val body = sessionJsonBig.expand("sid" -> ssnId2Big(i))
            assertResp(route(app, httpReq(PUT, "/session/monstrosity").withBody(body)))
               .is(OK)
               .withNoBody
         }

         for (i <- 0 until SESSIONS) {
	         val body = sessionJsonPet.expand("sid" -> ssnId2Pet(i))
            assertResp(route(app, httpReq(PUT, "/session/petclinic").withBody(body)))
               .is(OK)
               .withNoBody
         }

         joinAll
   	}

      "permit session create over live generations" in {
         
         // Create
         val sid = newSid
         var actualSid: String = null
         assertResp(route(app, httpReq(POST, "/session/petclinic/" + sid).withBody(emptyTargetingTrackerBody)))
            .is(OK)
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               actualSid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "petclinic"
            }

         // And read, just in case
         assertResp(route(app, httpReq(GET, "/session/petclinic/" + actualSid)))
            .is(OK)
            .withBodySession { ssn =>
               ssn.getId must be (actualSid)
               ssn.getSchema().getMeta().getName mustBe "petclinic"
            }
      }
      
      "refuse session create in dead generation" in {

         assertResp(route(app, httpReq(POST, "/session/monstrosity/" + newSid).withBody(emptyTargetingTrackerBody)))
            .isError(UNKNOWN_SCHEMA, "monstrosity")
      }

      "expire all sessions and dispose of all dead generations after session timeout period" in {
       
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
         
         // TODO: test that dead generations were vacuumed.
      }
   }
}
