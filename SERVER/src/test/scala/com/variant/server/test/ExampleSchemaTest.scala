package com.variant.server.test

import com.variant.core.error.ServerError
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.impl.SessionImpl
import com.variant.server.test.hooks.TestTargetingHook
import com.variant.server.test.hooks.TestTargetingHookSimple
import com.variant.server.test.spec.Async
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.util.ParameterizedString
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import com.variant.server.test.routes.SessionTest
import com.variant.server.api.StateRequest.Status._
import scala.util.Random

class ExampleSchemaTest extends EmbeddedServerSpec with TempSchemataDir with Async {

   override protected lazy val schemata = Set[String]("../src/universal/schemata/example.schema")

   "Server" should {

      "come up with the test schema" in {
         server.schemata.size mustBe 1
         server.bootExceptions.size mustBe 0
         server.schemata.get("exampleSchema").isDefined mustBe true
      }

      val sessions = 100

      "refue to target a session with an active request" in { 
                  
         for (i <- 0 until sessions) async {
            // Create Session.
            var sid = "uninitialized"
            HttpRequest(method = HttpMethods.POST, uri = "/session/exampleSchema/${sid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
               val ssnResp = SessionResponse(response)
               sid = ssnResp.session.getId
            }
   
            // Target for the only state
            val reqBody = """{"state": "state1"}"""
   
            HttpRequest(method = HttpMethods.POST, uri = s"/request/exampleSchema/${sid}", entity = reqBody) ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.session.getId mustBe sid
               ssnResp.schema.getMeta.getName mustBe "exampleSchema"
               val stateReq = ssnResp.session.getStateRequest.get
               stateReq mustNot be(null)
               stateReq.getStatus.ordinal mustBe InProgress.ordinal
               stateReq.getLiveExperiences.size mustBe 1
               stateReq.getResolvedParameters.size mustBe 0
               stateReq.getSession.getId mustBe sid
            }
   
            // Target again and get an error   
            HttpRequest(method = HttpMethods.POST, uri = s"/request/exampleSchema/${sid}", entity = reqBody) ~> router ~> check {
               ServerErrorResponse(response) mustBe ServerError.ACTIVE_REQUEST
            }
         }
      }

      "target and commit/rollback repeatedly " in { 
                  
         val hops = 200
         
         for (i <- 0 until sessions) async {
            // Create Session.
            var sid = "uninitialized"
            HttpRequest(method = HttpMethods.POST, uri = "/session/exampleSchema/${sid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
               val ssnResp = SessionResponse(response)
               sid = ssnResp.session.getId
            }

            for (j <- 0 until hops) {

               val targetBody = """{"state": "state1"}"""
      
               HttpRequest(method = HttpMethods.POST, uri = s"/request/exampleSchema/${sid}", entity = targetBody) ~> router ~> check {
                  val ssnResp = SessionResponse(response)
                  ssnResp.session.getId mustBe sid
                  ssnResp.schema.getMeta.getName mustBe "exampleSchema"
                  val stateReq = ssnResp.session.getStateRequest.get
                  stateReq mustNot be(null)
                  stateReq.getStatus.ordinal mustBe InProgress.ordinal
                  stateReq.getLiveExperiences.size mustBe 1
                  stateReq.getResolvedParameters.size mustBe 0
                  stateReq.getSession.getId mustBe sid
               }
      
               val state = if (Random.nextBoolean()) Committed.ordinal else Failed.ordinal
               val commitBody = s"""{"status": ${state}, "attrs":{"key":"special value"}}"""

               HttpRequest(method = HttpMethods.DELETE, uri = s"/request/monstrosity/${sid}", entity = commitBody) ~> router ~> check {
                  SessionResponse(response)
               }

               // Target again and get an error   
               HttpRequest(method = HttpMethods.POST, uri = s"/request/exampleSchema/${sid}", entity = targetBody) ~> router ~> check {
                  SessionResponse(response)
               }
            }
         }
      }
   }
}
