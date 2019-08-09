package com.variant.server.test

import java.io.File
import java.time.format.DateTimeFormatter
import java.time.Instant

import scala.sys.process._

import com.variant.core.schema.parser.error.SemanticError
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.server.api.StateRequest.Status._
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.schema.SchemaGen.State._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.test.routes.SessionTest
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import play.api.libs.json.Json
import com.variant.core.error.ServerError

/**
 * Test various schema deployment scenarios
 */
class SchemaDeployHotTest extends EmbeddedServerSpec with TempSchemataDir {

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
   "File System Schema Deployer" should {

      "startup with two schemata" in {

         server.schemata.size mustBe 2
         val bigGen = server.schemata.get("monstrosity").get.liveGen.get
         bigGen.getMeta.getName mustEqual "monstrosity"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"

         // Let the directory watcher thread start before copying any files.
         Thread.sleep(100)
      }

      "deploy a third schema" in {

         s"cp schemata/monster0.schema ${schemataDir}".!!

         // Sleep awhile to let WatcherService.take() have a chance to detect.
         Thread.sleep(dirWatcherLatencyMsecs);

         server.schemata.size mustBe 3
         val monstr0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monstr0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"
         val monstrGen = server.schemata.get("monstrosity").get.liveGen.get
         monstrGen.getMeta.getName mustEqual "monstrosity"
      }

      "replace petclinic from same origin" in {

         val currentGen = server.schemata.get("petclinic").get.liveGen.get

         s"cp schemata/petclinic.schema ${schemataDir}/petclinic.schema".!!
         Thread.sleep(dirWatcherLatencyMsecs)

         val newGen = server.schemata.get("petclinic").get.liveGen.get
         newGen.id mustNot be(currentGen.id)
         currentGen.state mustBe Dead
         newGen.state mustBe Live

         server.schemata.size mustBe 3
         val monstr0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monstr0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"
         val monstrGen = server.schemata.get("monstrosity").get.liveGen.get
         monstrGen.getMeta.getName mustEqual "monstrosity"

      }

      "refuse to re-deploy petclinic from different origin" in {

         val currentGen = server.schemata.get("petclinic").get.liveGen.get

         s"cp schemata/petclinic.schema ${schemataDir}/petclinic2.schema".!!
         Thread.sleep(dirWatcherLatencyMsecs)

         val newGen = server.schemata.get("petclinic").get.liveGen.get
         newGen.id must be(currentGen.id)
         newGen.state mustBe Live

         server.schemata.size mustBe 3
         val monstr0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monstr0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"
         val monstrGen = server.schemata.get("monstrosity").get.liveGen.get
         monstrGen.getMeta.getName mustEqual "monstrosity"

         val logLines = ServerLogTailer.last(2)
         logLines(0).message must startWith(s"[${SCHEMA_CANNOT_REPLACE.getCode}]")
         logLines(1).message must startWith(s"[${SCHEMA_FAILED.getCode}]")

      }

      "re-deploy a schema with parse warnings" in {

         val currentGen = server.schemata.get("monstrosity").get.liveGen.get

         s"cp schemata-errata/monster-warning.schema ${schemataDir}/monster.schema".!!
         Thread.sleep(dirWatcherLatencyMsecs)

         val newGen = server.schemata.get("monstrosity").get.liveGen.get
         newGen.id mustNot be(currentGen.id)
         currentGen.state mustBe Dead
         newGen.state mustBe Live

         server.schemata.size mustBe 3
         val monstr0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monstr0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"
         val bgsGen = server.schemata.get("monstrosity").get.liveGen.get
         bgsGen.getMeta.getName mustEqual "monstrosity"
      }

      "undeploy deleted monster.schema" in {

         server.schemata.get("monstrosity").get.liveGen.isDefined

         // Create a session to keep the schema from being vacuumed after undeployment.
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         s"rm ${schemataDir}/monster.schema".!!
         Thread.sleep(dirWatcherLatencyMsecs)

         server.schemata.get("monstrosity").get.liveGen.isDefined mustBe false

         server.schemata.size mustBe 2
         val monster0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monster0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"
      }

      "refuse to deploy a schema with syntax errors" in {

         s"cp schemata-errata/monster-error.schema ${schemataDir}".!!
         Thread.sleep(dirWatcherLatencyMsecs)

         val logLines = ServerLogTailer.last(2)
         logLines(0).message must startWith(s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(1).message must startWith(s"[${SCHEMA_FAILED.getCode}]")

         server.schemata.size mustBe 2
         val monster0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monster0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"

      }

      "ignore deletion of an orphan file unbounded to a live schema" in {

         s"rm ${schemataDir}/monster-error.schema".!!
         Thread.sleep(dirWatcherLatencyMsecs)

         server.schemata.size mustBe 2
         val monster0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monster0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"

      }

      "refuse to re-deploy a schema with semantic errors" in {

         s"cp schemata-errata/petclinic.schema ${schemataDir}/petclinic2.schema".!!
         Thread.sleep(dirWatcherLatencyMsecs)

         val logLines = ServerLogTailer.last(2)
         logLines(0).message must startWith(s"[${SemanticError.CONTROL_EXPERIENCE_MISSING.getCode}]")
         logLines(1).message must startWith(s"[${SCHEMA_FAILED.getCode}]")

         server.schemata.size mustBe 2
         val monster0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monster0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"

      }

      "redeploy the third schema after sessions expire" in {

         Thread.sleep(sessionTimeoutMillis)

         s"cp schemata/monster.schema ${schemataDir}/monster2.schema".!!

         // Sleep awhile to let WatcherService.take() have a chance to detect.
         Thread.sleep(dirWatcherLatencyMsecs)

         server.schemata.size mustBe 3
         val monster0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monster0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"
         val monsterGen = server.schemata.get("monstrosity").get.liveGen.get
         monsterGen.getMeta.getName mustEqual "monstrosity"
      }

      var sid = newSid()

      "create session in the third schema" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

      }

      "delete schema file monster2.schema" in {

         val currentGen = server.schemata.get("monstrosity").get.liveGen.get

         s"rm ${schemataDir}/monster2.schema".!!
         Thread.sleep(dirWatcherLatencyMsecs)

         // Schema gen should be vacuumed.
         currentGen.state mustBe Dead
         server.schemata.get("monstrosity").get.liveGen mustBe None

      }

      "permit session read over draining connection" in {

         HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}") ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
         }
      }

      "permit session update over draining connection" in {

         val reqBody = Json.obj(
            "state" -> "state2").toString

         // Target and get the request.
         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 5
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
         }
      }

      "refuse session create over draining connection" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${newSid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.UNKNOWN_SCHEMA, "monstrosity")
         }
      }

      "expire existing session as normal in the undeployed schema" in {

         Thread.sleep(sessionTimeoutMillis);

         HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}") ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.SESSION_EXPIRED, sid)
         }

      }

      "confirm the 2 schemata" in {

         s"cp schemata/monster0.schema ${schemataDir}".!!
         s"cp schemata/petclinic.schema ${schemataDir}".!!

         Thread.sleep(dirWatcherLatencyMsecs)

         server.schemata.size mustBe 2
         val monstr0Gen = server.schemata.get("monstrosity0").get.liveGen.get
         monstr0Gen.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"

      }

      "create a session in schema monstrosity0" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity0/${sid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity0"
            sid = ssnResp.session.getId
         }
      }

      "redeploy schemata at once" in {

         val currentGen = server.schemata.get("monstrosity0").get.liveGen.get

         // Override
         s"cp schemata/monster0.schema ${schemataDir}".!!
         // New file
         s"cp schemata/monster.schema ${schemataDir}".!!

         Thread.sleep(dirWatcherLatencyMsecs)

         // Session should still be there
         HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity0/${sid}") ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity0"
         }

         currentGen.state mustBe Dead

         server.schemata.size mustBe 3
         val monstr0 = server.schemata.get("monstrosity0").get.liveGen.get
         monstr0.getMeta.getName mustEqual "monstrosity0"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic"
         val monstrGen = server.schemata.get("monstrosity").get.liveGen.get
         monstrGen.getMeta.getName mustEqual "monstrosity"

      }

      "create new session in the unaffected schema petclinic" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/petclinic/${newSid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.schema.getMeta.getName mustBe "petclinic"
         }
      }

      "create new session in the new schema" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${newSid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
         }
      }
   }
}
