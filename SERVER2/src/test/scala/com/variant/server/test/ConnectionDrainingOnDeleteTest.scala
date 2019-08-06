package com.variant.server.test

import scala.sys.process._

import com.variant.core.error.ServerError._
import com.variant.server.schema.SchemaGen.State._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.spec.Async
import com.variant.server.test.util.ParameterizedString
import com.variant.core.util.StringUtils
import scala.util.Random
import play.api.libs.json._
import com.variant.server.impl.SessionImpl

import java.time.format.DateTimeFormatter
import java.time.Instant
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import com.variant.core.error.ServerError

/**
 * Test session drainage.
 */
class ConnectionDrainingOnDeleteTest extends TempSchemataDir with Async {

   private val random = new Random(System.currentTimeMillis())
   private val SESSIONS = 100

   val sessionTimeoutMillis = server.config.sessionTimeout * 1000
   val vacuumIntervalMillis = server.config.sessionVacuumInterval * 1000

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

      val ssnId2Monster = new Array[String](SESSIONS)
      val ssnId2Petclinic = new Array[String](SESSIONS)

      "create SESSIONS sessionns to each schema" in {

         for (j <- 0 until SESSIONS) async {

            HttpRequest(method = HttpMethods.POST, uri = "/session/monstrosity/blah", entity = emptyTargetingTrackerBody) ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.schema.getMeta.getName mustBe "monstrosity"
               ssnId2Monster(j) = ssnResp.session.getId
            }
         }

         for (j <- 0 until SESSIONS) async {

            HttpRequest(method = HttpMethods.POST, uri = "/session/petclinic/blah", entity = emptyTargetingTrackerBody) ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.schema.getMeta.getName mustBe "petclinic"
               ssnId2Petclinic(j) = ssnResp.session.getId
            }
         }

         joinAll // blocks until all sessions are created by async blocks above

      }

      "all sessions must be readable over the right connection" in {

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Monster(i)
            HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}") ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.session.getId mustBe sid
               ssnResp.schema.getMeta.getName mustBe "monstrosity"
            }
         }

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Petclinic(i)
            HttpRequest(method = HttpMethods.GET, uri = s"/session/petclinic/${sid}") ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.session.getId mustBe sid
               ssnResp.schema.getMeta.getName mustBe "petclinic"
            }
         }

         joinAll
      }

      "all sessions must not be readable over the wrong connection" in {

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Monster(i)
            HttpRequest(method = HttpMethods.GET, uri = s"/session/petclinic/${sid}") ~> router ~> check {
               ServerErrorResponse(response).mustBe(ServerError.WRONG_CONNECTION, "petclinic")
            }
         }

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Petclinic(i)
            HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}") ~> router ~> check {
               ServerErrorResponse(response).mustBe(ServerError.WRONG_CONNECTION, "monstrosity")
            }
         }

         joinAll
      }

      "delete schema monstrosity" in {

         val oldGen = server.schemata.getLiveGen("monstrosity").get
         oldGen.state mustBe Live

         s"rm -rf ${schemataDir}/monster.schema" !;
         Thread.sleep(dirWatcherLatencyMsecs)

         oldGen.state mustBe Dead

         // Confirm the schema is gone.
         server.schemata.size mustBe 1
         server.schemata.getLiveGen("monstrosity") mustBe None
         server.schemata.getLiveGen("petclinic").isDefined mustBe true
      }

      "permit session updates in both live and dead generations" in {

         for (i <- 0 until SESSIONS) async {

            val attrKey = Random.nextString(5)
            val attrVal = Random.nextString(5)
            val sid = ssnId2Monster(i)
            val body = Json.obj(
               "attrs" -> Map(attrKey -> attrVal)).toString

            HttpRequest(method = HttpMethods.PUT, uri = s"/session-attr/monstrosity/${sid}", entity = body) ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.session.getId mustBe sid
               ssnResp.schema.getMeta.getName mustBe "monstrosity"
               ssnResp.session.getAttributes.get(attrKey) mustBe attrVal
            }
         }

         for (i <- 0 until SESSIONS) async {

            val attrKey = Random.nextString(5)
            val attrVal = Random.nextString(5)
            val sid = ssnId2Petclinic(i)
            val body = Json.obj(
               "attrs" -> Map(attrKey -> attrVal)).toString

            HttpRequest(method = HttpMethods.PUT, uri = s"/session-attr/petclinic/${sid}", entity = body) ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.session.getId mustBe sid
               ssnResp.schema.getMeta.getName mustBe "petclinic"
               ssnResp.session.getAttributes.get(attrKey) mustBe attrVal
            }
         }

         joinAll

      }

      "permit session create over live generations" in {

         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/petclinic/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "petclinic"
            sid = ssnResp.session.getId
         }

         HttpRequest(method = HttpMethods.GET, uri = s"/session/petclinic/${sid}") ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "petclinic"
         }
      }

      "refuse session create in dead generation" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${newSid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.UNKNOWN_SCHEMA, "monstrosity")
         }
      }

      "expire all sessions and dispose of all dead generations after session timeout period" in {

         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis)

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Monster(i)
            HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}") ~> router ~> check {
               ServerErrorResponse(response).mustBe(ServerError.SESSION_EXPIRED, sid)
            }
         }

         for (i <- 0 until SESSIONS) async {
            val sid = ssnId2Petclinic(i)
            HttpRequest(method = HttpMethods.GET, uri = s"/session/petclinic/${sid}") ~> router ~> check {
               ServerErrorResponse(response).mustBe(ServerError.SESSION_EXPIRED, sid)
            }
         }

         // TODO: test that dead generations were vacuumed.
      }
   }
}
