package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.api.ConfigKeys
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
   
   val sessionTimeoutMillis = server.config.getSessionTimeout * 1000
   sessionTimeoutMillis mustEqual 1000
   
   val vacuumIntervalMillis = server.config.getSessionVacuumInterval * 1000
  
   vacuumIntervalMillis  mustEqual 1000

   val emptyTargetingTrackerBody = "{\"tt\":[]}"
   
   "SessionController" should {

      var genId: String = null
      
      "return 404 on GET with no sid" in {  
                  
         assertResp(route(app, httpReq(GET, endpoint + "/petclinic")))
            .is(NOT_FOUND)
            .withNoBody
      }

      "return SessionExpired on GET non-existent session on valid schema" in {  
         
         assertResp(route(app, httpReq(GET, endpoint + "/petclinic/foo")))
            .isError(SESSION_EXPIRED, "foo")
      }

      "return OK on PUT non-existent session with valid conn ID" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> "foo")
         assertResp(route(app, httpReq(PUT, endpoint + "/monstrosity").withBody(body)))
            .isOk
            .withNoBody
      }

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
   }   
}
