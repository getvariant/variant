package com.variant.server.test.routes

import java.time.Instant
import java.time.format.DateTimeFormatter

import com.variant.core.error.ServerError
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.util.ParameterizedString

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes.MethodNotAllowed
import akka.http.scaladsl.model.StatusCodes.NotFound

/**
 * Session Controller Tests
 */
object SessionTest {
   val monsterSessionPrototype = """
     {"sid":"${sid:SID}",
      "ts": "${ts:%s}", 
      "request": {
            "state": "state1", 
            "status": 1,
            "variant":{"test":"test2", "offset":1}, 
            "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
       "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
       "attrs": {"NAME1":"${attrValue:VALUE1}", "NAME2": "VALUE2"},
       "tests": ["test1","test2"]
      }
   """
   val sessionJsonPetclinicPrototype = """
      {"sid":"${sid:SID}",
       "ts": "${ts:%s}", 
       "request": {"state": "newVisit", "status": 0,
                  "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}],
                  "exps": ["ScheduleVisitTest.withLink.false"]},
        "attrs": {"NAME1": "VALUE1", "NAME2": "VALUE2"}
      }
   """

   val emptyTargetingTrackerBody = "{\"tt\":[]}"

}

class SessionTest extends EmbeddedServerSpec {

   import SessionTest._

   val endpoint = "/session"

   /*
   val sessionJsonBigCovar = ParameterizedString(
      monsterSessionPrototype.format(DateTimeFormatter.ISO_INSTANT.format(Instant.now())))

   val sessionJsonPetclinic = ParameterizedString(
      sessionJsonPetclinicPrototype.format(DateTimeFormatter.ISO_INSTANT.format(Instant.now())))
*/
   val sessionTimeoutMillis = server.config.sessionTimeout * 1000
   sessionTimeoutMillis mustEqual 1000

   val vacuumIntervalMillis = server.config.sessionVacuumInterval * 1000

   vacuumIntervalMillis mustEqual 1000

   "Session route" should {

      var genId: String = null

      "respond NotFound on GET with no sid" in {

         HttpRequest(method = HttpMethods.GET, uri = "/session/petclinic") ~> router ~> check {
            handled mustBe true
            status mustBe NotFound
            entityAs[String] mustBe "The requested resource could not be found."
         }
      }

      "respond SESSION_EXPIRED on GET non-existent session on valid schema" in {

         HttpRequest(method = HttpMethods.GET, uri = "/session/petclinic/foo") ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.SESSION_EXPIRED, "foo")
         }
      }

      var sid = newSid

      "respond OK on POST non-existent session with valid schema" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }
      }

      "respond OK and existing session on GET" in {

         HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}", entity = "junk to be ignored") ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustBe sid
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
         }
      }
      /* i don't think we call this fro the client, so PUT is currently not implemented.
      "respond OK and replace existing session on PUT" in {

         val reqBody = sessionJsonBigCovar.expand("sid" -> "foo")
         assertResp(route(app, httpReq(PUT, endpoint + "/monstrosity").withBody(reqBody)))
            .isOk
            .withNoBody

         assertResp(route(app, httpReq(GET, endpoint + "/monstrosity/foo")))
            .isOk
            .withBodyJson { json =>
               StringUtils.digest((json \ "session").as[String]) mustBe
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo"))
            }
      }
*/
      "keep an existing session alive over time" in {

         val halfExp = sessionTimeoutMillis / 2
         halfExp mustBe 500
         for (wait <- Seq(halfExp, halfExp, halfExp, halfExp)) {
            Thread.sleep(wait)
            HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}") ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.session.getId mustBe sid
               ssnResp.schema.getMeta.getName mustBe "monstrosity"
            }
         }
      }

      "expire an existing session affter session timeout interfal" in {

         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis);

         HttpRequest(method = HttpMethods.GET, uri = s"/session/monstrosity/${sid}") ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.SESSION_EXPIRED, sid)
         }

         server.ssnStore.get(sid) mustBe empty

      }

      "recreate an expired session with new SID" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }
      }

      "respond UNKNOWN_SCHEMA on get non-existent session over non-existent connection" in {

         HttpRequest(method = HttpMethods.GET, uri = s"/session/foo/bar") ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.UNKNOWN_SCHEMA, "foo")
         }
      }

      "respond UNKNOWN_SCHEMA on get existing session over non-existent connection" in {

         HttpRequest(method = HttpMethods.GET, uri = s"/session/foo/${sid}") ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.UNKNOWN_SCHEMA, "foo")
         }
      }

      "respond WRONG_CONNECTION on get existing session over existing but wrong connection" in {

         HttpRequest(method = HttpMethods.GET, uri = s"/session/petclinic/${sid}") ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.WRONG_CONNECTION, "petclinic")
         }
      }

      "respond UNKNOWN_SCHEMA on post non-existent session over non-existent connection" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/foo/bar", entity = emptyTargetingTrackerBody) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.UNKNOWN_SCHEMA, "foo")
         }
      }

      "respond UNKNOWN_SCHEMA on post existing session over non-existent connection" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/foo/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.UNKNOWN_SCHEMA, "foo")
         }
      }

      "respond WRONG_CONNECTION on post existing session over existing but wrong connection" in {

         HttpRequest(method = HttpMethods.POST, uri = s"/session/petclinic/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            ServerErrorResponse(response).mustBe(ServerError.WRONG_CONNECTION, "petclinic")
         }
      }

      "respond MethodNotAllowed on everythig except GET,POST" in {

         httpMethods.filterNot(List(HttpMethods.GET, HttpMethods.POST).contains _).foreach { method =>
            HttpRequest(method = method, uri = "/session/petclinic/foo") ~> router ~> check {
               handled mustBe true
               status mustBe MethodNotAllowed
               contentType mustBe ContentTypes.`text/plain(UTF-8)`
               entityAs[String] mustBe "HTTP method not allowed, supported methods: GET, POST"
            }
         }
      }
   }
}
