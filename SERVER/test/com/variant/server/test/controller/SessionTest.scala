package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.impl.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.util.Constants._
import com.variant.core.schema.impl.SchemaImpl
import com.variant.server.schema.ServerSchemaParser

/**
 * Session Controller Tests
 */
object SessionTest {
   val sessionJsonBigCovarPrototype = """
     {"sid":"${sid:SID}",
      "ts": ${ts:%d}, 
      "request": {"state": "state1","status": "OK","committed": true, 
            "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}], 
            "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
       "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
       "attrList": [{"name": "NAME1","val": "${attrValue:VALUE1}"}, {"name": "NAME2","val": "VALUE2"}],
       "tests": ["test1","test2"]
      }
   """
   val sessionJsonPetclinicPrototype = """
      {"sid":"${sid:SID}",
       "ts": ${ts:%d}, 
       "request": {"state": "newOwner","status": "OK","committed": false, 
                  "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}],
                  "exps": ["NewOwnerTest.tosCheckbox.false"]},
        "attrList": [{"name": "NAME1","val": "VALUE1"}, {"name": "NAME2","val": "VALUE2"}]
      }
   """
}

class SessionTest extends EmbeddedServerSpec {
   
   import SessionTest._
   
   val endpoint = context + "/session"
   
   val sessionJsonBigCovar = ParameterizedString(
         sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
         
   val sessionJsonPetclinic = ParameterizedString(
         sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))
   
   val sessionTimeoutMillis = server.config.getLong(ConfigKeys.SESSION_TIMEOUT) * 1000
   sessionTimeoutMillis mustEqual 1000
   
   val vacuumIntervalMillis = server.config.getLong(ConfigKeys.SESSION_VACUUM_INTERVAL) * 1000
   vacuumIntervalMillis  mustEqual 1000


   "SessionController" should {

      var genId: String = null
      
      "return 404 on GET with no sid" in {  
                  
         assertResp(route(app, httpReq(GET, endpoint + "/petclinic_experiments")))
            .is(NOT_FOUND)
            .withNoBody
      }

      "return SessionExpired on GET non-existent session on valid CID" in {  
         
         assertResp(route(app, httpReq(GET, endpoint + "/petclinic_experiments/foo")))
            .isError(SessionExpired, "foo")
      }

      "return OK on PUT non-existent session with valid conn ID" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> "foo")
         assertResp(route(app, httpReq(PUT, endpoint + "/big_conjoint_schema").withBody(body)))
            .isOk
            .withNoBody
      }

      "return OK and existing session on GET" in {
       
         val body = "just some junk that should be ignored"

         assertResp(route(app, httpReq(GET, endpoint + "/big_conjoint_schema/foo").withBody(body)))
            .isOk
            .withBodyJson { json => 
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo").toString())
            }
      }

      "return OK and replace existing session on PUT" in {
       
         val putBody = sessionJsonBigCovar.expand("sid" -> "foo")
         assertResp(route(app, httpReq(PUT, endpoint + "/big_conjoint_schema").withBody(putBody)))
            .isOk
            .withNoBody
         
         assertResp(route(app, httpReq(GET, endpoint + "/big_conjoint_schema/foo")))
            .isOk
            .withBodyJson { json =>
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo"))
            }
      }

      "return OK and create session on POST" in {
       
         val sid = "bar"
         assertResp(route(app, httpReq(POST, endpoint + "/big_conjoint_schema/" + sid)))
            .isOk
            .withBodyJsonSession (sid, "big_conjoint_schema")
         
         assertResp(route(app, httpReq(GET, endpoint + "/big_conjoint_schema/" + sid)))
            .isOk
            .withBodyJsonSession (sid, "big_conjoint_schema")
      }

     "not lose existing session with different key" in {

         assertResp(route(app, httpReq(GET, endpoint + "/big_conjoint_schema/foo")))
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
            assertResp(route(app, httpReq(GET, endpoint + "/big_conjoint_schema/foo")))
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

            assertResp(route(app, httpReq(GET, endpoint + "/big_conjoint_schema/" + sid)))
               .isError(SessionExpired, sid)
         }
      }

      "deserialize payload into session object" in {
       
         val sid = newSid()
         val ts = System.currentTimeMillis()
         val body = sessionJsonBigCovar.expand("sid" -> sid, "ts" -> ts)
         assertResp(route(app, httpReq(PUT, endpoint + "/big_conjoint_schema").withBody(body)))
            .isOk
            .withNoBody

         val ssnJson = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl].toJson
         ssnJson mustBe normalJson(sessionJsonBigCovar.expand("sid" -> sid, "ts" -> ts))
         val ssn = server.ssnStore.get(sid).get
         ssn.getCreateDate.getTime mustBe ts
         ssn.getId mustBe sid
      
         Thread.sleep(2000);
         server.ssnStore.get(sid) mustBe empty
         
      }

      var sid: String = null

       "expire session as normal" in {

         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis);

         assertResp(route(app, httpReq(GET, endpoint + "/big_conjoint_schema/" + sid)))
            .isError(SessionExpired, sid)

       }

   }
}
