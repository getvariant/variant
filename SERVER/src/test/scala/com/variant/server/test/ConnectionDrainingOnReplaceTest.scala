package com.variant.server.test

import scala.util.Random
import scala.sys.process._

import com.variant.core.error.ServerError
import com.variant.server.schema.SchemaGen.State.Dead
import com.variant.server.schema.SchemaGen.State.Live
import com.variant.server.test.spec.Async
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import play.api.libs.json.Json

class ConnectionDrainingOnReplaceTest extends EmbeddedServerSpec with TempSchemataDir with Async {

   private val random = new Random(System.currentTimeMillis())
   private val SESSIONS = 100

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

      val ssnId2Monster = new Array[String](SESSIONS)
      val ssnId2Petclinic = new Array[String](SESSIONS)

      "create SESSIONS sessionns to both connections" in {

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

         joinAll() // blocks until all sessions are created by async blocks above

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

         joinAll()
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

         joinAll()
      }

      "replace schema monstrosity" in {

         val oldGen = server.schemata.get("monstrosity").get.liveGen.get
         oldGen.state mustBe Live

         s"cp schemata/monster.schema ${schemataDir}" !

         Thread.sleep(dirWatcherLatencyMillis)

         oldGen.state mustBe Dead

         val newGen = server.schemata.get("monstrosity").get.liveGen.get
         newGen.id mustNot be(oldGen.id)
         newGen.state mustBe Live
         server.schemata.size mustBe 2

      }

      "permit session reads over dead generation" in {

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

         joinAll()

      }

      "permit session updates in both live and draining generations" in {

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

         joinAll()

      }

      "permit session create over live generations" in {

         for (i <- 0 until SESSIONS) async {
            var sid = newSid
            HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.session.getId mustNot be(sid)
               ssnResp.schema.getMeta.getName mustBe "petclinic"
               sid = ssnResp.session.getId
            }

            HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}") ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.session.getId mustBe sid
               ssnResp.schema.getMeta.getName mustBe "petclinic"
            }
         }
      }

      "expire all sessions and dispose of all dead gens" in {

         Thread.sleep(sessionTimeoutMillis)

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

      }

      // TODO: test that dead generations were vacuumed.

   }
}
