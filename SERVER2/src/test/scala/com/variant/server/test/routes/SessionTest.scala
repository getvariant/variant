package com.variant.server.test.routes

import scala.collection.JavaConversions._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.error.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.Constants._
import com.variant.core.schema.impl.SchemaImpl
import com.variant.server.schema.ServerSchemaParser
import java.time.format.DateTimeFormatter
import java.time.Instant
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpMethods
import com.variant.core.error.ServerError
import com.variant.core.schema.parser.SchemaParser
import com.variant.core.session.CoreSession
import com.variant.server.boot.VariantServer

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
}

class SessionTest extends EmbeddedServerSpec {

   import SessionTest._

   val endpoint = "/session"

   val sessionJsonBigCovar = ParameterizedString(
      monsterSessionPrototype.format(DateTimeFormatter.ISO_INSTANT.format(Instant.now())))

   val sessionJsonPetclinic = ParameterizedString(
      sessionJsonPetclinicPrototype.format(DateTimeFormatter.ISO_INSTANT.format(Instant.now())))

   val sessionTimeoutMillis = server.config.sessionTimeout * 1000
   sessionTimeoutMillis mustEqual 1000

   val vacuumIntervalMillis = server.config.sessionVacuumInterval * 1000

   vacuumIntervalMillis mustEqual 1000

   val emptyTargetingTrackerBody = "{\"tt\":[]}"

   "SessionController" should {

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
            handled mustBe true
            status mustBe BadRequest
            val respBody = Json.parse(entityAs[String])
            (respBody \ "code").as[Long] mustBe ServerError.SESSION_EXPIRED.getCode
            (respBody \ "args").as[List[String]] mustBe List("foo")
         }
      }

      "respond OK on POST non-existent session with valid schema" in {

         val sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            handled mustBe true
            status mustBe OK
            entityAs[String] mustNot be(null)
            val ssnResp = new SessionResponse(entityAs[String])
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
         }
      }
      /*
      "return OK and existing session on GET" in {

         val body = "just some junk that should be ignored"

         assertResp(route(app, httpReq(GET, endpoint + "/monstrosity/foo").withBody(body)))
            .isOk
            .withBodyJson { json =>
               StringUtils.digest((json \ "session").as[String]) mustBe
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo").toString())
            }
      }

      "return OK and replace existing session on PUT" in {

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

      "return OK and create session on POST" in {

         val sid = "bar"
         var actualSid: String = null

         assertResp(route(app, httpReq(POST, endpoint + "/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               actualSid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "monstrosity"
         }

         assertResp(route(app, httpReq(GET, endpoint + "/monstrosity/" + actualSid)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustBe actualSid
               ssn.getSchema().getMeta().getName mustBe "monstrosity"
         }
      }

     "not lose existing session with different key" in {

         assertResp(route(app, httpReq(GET, endpoint + "/monstrosity/foo")))
            .isOk
            .withBodyJson { json =>
               StringUtils.digest((json \ "session").as[String]) mustBe
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo"))
            }
      }

      "keep an existing session alive over time" in {

         val halfExp = sessionTimeoutMillis / 2
         halfExp mustBe 500
         for ( wait <- Seq(halfExp, halfExp, halfExp, halfExp) ) {
            Thread.sleep(wait)
            assertResp(route(app, httpReq(GET, endpoint + "/monstrosity/foo")))
               .isOk
               .withBodyJson { json =>
                  StringUtils.digest((json \ "session").as[String]) mustBe
                     StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo").toString())
            }
         }
      }

      "return SessionExpired on GET expired session" in {

         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis);

         ("foo" :: "bar" :: Nil).foreach { sid =>

            assertResp(route(app, httpReq(GET, endpoint + "/monstrosity/" + sid)))
               .isError(SESSION_EXPIRED, sid)
         }
      }

      "deserialize payload into session object" in {

         val sid = newSid()
         val ts = Instant.now()
         val body = sessionJsonBigCovar.expand("sid" -> sid, "ts" -> ts)
         assertResp(route(app, httpReq(PUT, endpoint + "/monstrosity").withBody(body)))
            .isOk
            .withNoBody

         val ssnJson = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl].toJson
         ssnJson mustBe normalJson(sessionJsonBigCovar.expand("sid" -> sid, "ts" -> DateTimeFormatter.ISO_INSTANT.format(ts)))
         val ssn = server.ssnStore.get(sid).get
         ssn.getTimestamp mustBe ts
         ssn.getId mustBe sid

         Thread.sleep(2000);
         server.ssnStore.get(sid) mustBe empty

      }

      var sid: String = null

       "expire session as normal" in {

         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis);

         assertResp(route(app, httpReq(GET, endpoint + "/monstrosity/" + sid)))
            .isError(SESSION_EXPIRED, sid)

       }
       *
       */
   }

   /**
    * Unmarshal the common sessionResponse
    */
   private class SessionResponse(respBody: String)(implicit server: VariantServer) {

      private[this] val respJson = Json.parse(respBody)
      private[this] val ssnSrc = (respJson \ "session").asOpt[String].getOrElse { fail("No 'session' element in reponse") }
      private[this] val schemaSrc = (respJson \ "schema" \ "src").asOpt[String].getOrElse { fail("No 'schema/src' element in reponse") }
      private[this] val parserResponse = ServerSchemaParser(implicitly).parse(schemaSrc)

      parserResponse.hasMessages mustBe false

      val session = new CoreSession(ssnSrc)
      val schema = parserResponse.getSchema
      val schemaId = (respJson \ "schema" \ "id").asOpt[String].getOrElse { fail("No 'schema/id' element in reponse") }
   }
}
