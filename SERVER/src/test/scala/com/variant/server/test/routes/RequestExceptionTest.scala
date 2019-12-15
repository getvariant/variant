package com.variant.server.test.routes

import com.variant.server.test.spec.EmbeddedServerSpec

import SessionTest.emptyTargetingTrackerBody
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import play.api.libs.json.Json
import com.variant.share.error.ServerError
import com.variant.server.api.StateRequest.Status._

class RequestExceptionTest extends EmbeddedServerSpec {

   val sessionTimeoutMillis = server.config.sessionTimeout * 1000
   sessionTimeoutMillis mustEqual 1000

   val vacuumIntervalMillis = server.config.sessionVacuumInterval * 1000
   vacuumIntervalMillis mustEqual 1000

   val schema = server.schemata.getLiveGen("monstrosity").get

   "Request Router" should {

      "return SESSION_EXPIRED on an attempt to target" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         // Expire session
         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis)
         server.ssnStore.get(sid) mustBe empty

         // Attempt to target.
         val reqBody1 = Json.obj(
            "state" -> "state2").toString

         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody1) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.SESSION_EXPIRED, sid)
         }
      }

      "return SESSION_EXPIRED on an attempt to commit" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         // Target.
         val reqBody1 = Json.obj(
            "state" -> "state2").toString

         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 5
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state2").get
         }

         // Expire session
         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis)
         server.ssnStore.get(sid) mustBe empty

         val reqBody2 = Json.obj(
            "status" -> Committed.ordinal).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/monstrosity/${sid}", entity = reqBody2) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.SESSION_EXPIRED, sid)
         }
      }

      "return WRONG_CONNECTION if targeting over a non-existent connection" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         // Attempt to target.
         val reqBody1 = Json.obj(
            "state" -> "state2").toString

         HttpRequest(method = HttpMethods.POST, uri = s"/request/badness/${sid}", entity = reqBody1) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.WRONG_CONNECTION, "badness")
         }
      }

      "return WRONG_CONNECTION on an attempt to fail over a non-existent connection" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         // Target.
         val reqBody1 = Json.obj(
            "state" -> "state2").toString

         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 5
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state2").get
         }

         val reqBody2 = Json.obj(
            "status" -> Committed.ordinal).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/badness/${sid}", entity = reqBody2) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.WRONG_CONNECTION, "badness")
         }
      }

      "return WRONG_CONNECTION on an attempt to target" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         // Attempt to target.
         val reqBody1 = Json.obj(
            "state" -> "state2").toString

         HttpRequest(method = HttpMethods.POST, uri = s"/request/petclinic/${sid}", entity = reqBody1) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.WRONG_CONNECTION, "petclinic")
         }
      }

      "return WRONG_CONNECTION on an attempt to fail" in {

         // New session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         // Target.
         val reqBody1 = Json.obj(
            "state" -> "state2").toString

         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            val stateReq = ssnResp.session.getStateRequest.get
            stateReq mustNot be(null)
            stateReq.getStatus.ordinal mustBe InProgress.ordinal
            stateReq.getLiveExperiences.size mustBe 5
            stateReq.getResolvedParameters.size mustBe 1
            stateReq.getSession.getId mustBe sid
            stateReq.getState mustBe schema.getState("state2").get
         }

         val reqBody2 = Json.obj(
            "status" -> Committed.ordinal).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/petclinic/${sid}", entity = reqBody2) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.WRONG_CONNECTION, "petclinic")
         }
      }

   }
}
