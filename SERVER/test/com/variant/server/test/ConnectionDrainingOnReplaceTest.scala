package com.variant.server.test

import play.api.Logger
import play.api.test.Helpers._
import com.variant.core.ConnectionStatus._
import com.variant.core.ServerError._
import com.variant.server.schema.State
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


/**
 * Test session drainage.
 */
class ConnectionDrainingOnReplaceTest extends BaseSpecWithServerAsync with TempSchemataDir {
      
   private val logger = Logger(this.getClass)
   private val random = new Random(System.currentTimeMillis())
   private val CONNECTIONS = 25
   private val SESSIONS = 100 // per connection 

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
   "File System Schema Deployer on schema ADD" should {
 
	   "startup with two schemata" in {
	      
         server.schemata.size mustBe 2
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
                  
         // Let the directory watcher thread start before copying any files.
   	   Thread.sleep(100)
   
	   }

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
	      
	      joinAll // blocks until all sessions are created
	   }

      "all sessions must be readable over all connections" in {
         	   
         // pick connection randomly to emulate parallel connections.
         val i = random.nextInt(CONNECTIONS)
         val cidBig = connId2Big(i)
         for (j <- 0 until SESSIONS) async {
            val sid = ssnId2Big(i)(j)
            assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, cidBig)))
               .is(OK)
               .withConnStatusHeader(OPEN)
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonBig.expand("sid" -> sid).toString())
            }
         }
         
         val cidPet = connId2Pet(CONNECTIONS - (i+1))
         for (j <- 0 until SESSIONS) async {
            val sid = ssnId2Pet(i)(j)
            assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, cidPet)))
               .is(OK)
               .withConnStatusHeader(OPEN)
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonPet.expand("sid" -> sid).toString())
            }
         }
	      
	      joinAll
   	}

      "replace schema ParserCovariantOkayBigTestNoHooks" in {

	      val oldSchema = server.schemata.get("ParserCovariantOkayBigTestNoHooks").get
         oldSchema.state mustBe State.Deployed

         async {
   	      
            IoUtils.fileCopy("conf-test/ParserCovariantOkayBigTestNoHooks.json", s"${schemataDir}/ParserCovariantOkayBigTestNoHooks.json");
            Thread.sleep(dirWatcherLatencyMsecs)
            
            oldSchema.state mustBe State.Gone
            val newSchema = server.schemata.get("ParserCovariantOkayBigTestNoHooks").get
            newSchema.state mustBe State.Deployed
            newSchema mustNot be (oldSchema)
            server.schemata.size mustBe 2

	      }
	   }

      "permit session reads over parallel connections, while schema is being replaced" in {

         // pick connection randomly to emulate parallel connections.
         val i = random.nextInt(CONNECTIONS)
         val cidBig = connId2Big(i)
         for (j <- 0 until SESSIONS) async {
            val sid = ssnId2Big(i)(j)
            assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, cidBig)))
               .is(OK)
               .withConnStatusHeader(OPEN, CLOSED_BY_SERVER)  // changes underneath
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonBig.expand("sid" -> sid).toString())
            }
         }

         val cidPet = connId2Pet(i)
         for (j <- 0 until SESSIONS) async {
            val sid = ssnId2Pet(i)(j)
            assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, cidPet)))
               .is(OK)
               .withConnStatusHeader(OPEN)
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonPet.expand("sid" -> sid).toString())
            }
         }
         
         joinAll

   	}

      "permit session updates over draining connections, after schema was replaced" in {
     
         // pick connection randomly to emulate parallel connections.
         val i = random.nextInt(CONNECTIONS)
         val cidBig = connId2Big(i)
         for (j <- 0 until SESSIONS) {
	         val body = sessionJsonBig.expand("sid" -> ssnId2Big(i)(j))
            assertResp(route(app, connectedRequest(PUT, context + "/session", cidBig).withBody(body)))
               .is(OK)
               .withConnStatusHeader(CLOSED_BY_SERVER)
               .withNoBody
         }

         joinAll
   	}

      "allow session create in old live connection" in {
         
         val i = random.nextInt(CONNECTIONS)
         val cid = connId2Pet(i)
         val sid = newSid
         val body = sessionJsonPet.expand("sid" -> sid)

         // Create
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid).withBody(body)))
            .is(OK)
            .withConnStatusHeader(OPEN)
            .withNoBody

         // And read, just in case
         assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, cid)))
            .is(OK)
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(body)
         }

      }
      
      "refuse session create in draining connection" in {

         val i = random.nextInt(CONNECTIONS)
         val cid = connId2Big(i)
         val sid = newSid
         val body = sessionJsonBig.expand("sid" -> sid)

         // Create
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid).withBody(body)))
            .isError(UnknownConnection, cid)
            .withConnStatusHeader(CLOSED_BY_SERVER)

      }

      var newcid: String = null
      val newsid = newSid
      
      "open a new connection to the new schema and create a session in it" in {

         // Connection
         assertResp(route(app, connectionRequest("ParserCovariantOkayBigTestNoHooks")))
           .isOk
           .withConnStatusHeader(OPEN)
           .withBodyJson { json =>
               newcid = (json \ "id").as[String]
               newcid mustNot be (null)
            }

         // Session in it
         val body = sessionJsonBig.expand("sid" -> newsid)
         assertResp(route(app, connectedRequest(PUT, context + "/session", newcid).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
      }

      "session in the new connection should not be accessible via draining connection" in {
         
         val i = random.nextInt(CONNECTIONS)
         val cid = connId2Big(i)
         for (j <- 0 until SESSIONS) async {
            val sid = ssnId2Big(i)(j)
            assertResp(route(app, connectedRequest(GET, context + "/session/" + newsid, cid)))
               .isError(SessionExpired, newsid)
               .withConnStatusHeader(CLOSED_BY_SERVER) 
               .withNoBody
         }
         
         joinAll
      }
      
      "sessions in the draining connections should not be accessible via the new connection" in {
         
         for (i <- 0 until CONNECTIONS) {
            for (j <- 0 until SESSIONS) async {
               val sid = ssnId2Big(i)(j)
               assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, newcid)))
                  .isError(SessionExpired, sid)
                  .withConnStatusHeader(OPEN) 
                  .withNoBody
            }
         }         
         joinAll
      }

      "expire all sessions and dispose of all draining connections after session timeout period" in {
       
         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis)
         
         val i = random.nextInt(CONNECTIONS)
         val cidBig = connId2Big(i)
         for (j <- 0 until SESSIONS) {
            val sid = ssnId2Big(i)(j)
            assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, cidBig)))
               .isError(UnknownConnection, cidBig)
               .withNoConnStatusHeader
               .withNoBody
         }

         val cidPet = connId2Pet(i)
         for (j <- 0 until SESSIONS) {
            val sid = ssnId2Pet(i)(j)
            assertResp(route(app, connectedRequest(GET, context + "/session/" + sid, cidPet)))
               .isError(SessionExpired, sid)
               .withConnStatusHeader(OPEN)
               .withNoBody
         }
         
         assertResp(route(app, connectedRequest(GET, context + "/session/" + newsid, newcid)))
            .isError(SessionExpired, newsid)
            .withConnStatusHeader(OPEN)
            .withNoBody
      
      }
   }
}
