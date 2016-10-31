package com.variant.server.test

import scala.util.Random

import com.variant.server.test.util.ParamString
import com.variant.server.boot.VariantConfigKey._

import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import play.api.test.Helpers.NOT_FOUND
import play.api.test.Helpers.OK
import play.api.test.Helpers.PUT
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.route
import play.api.test.Helpers.status
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import play.api.test.Helpers.writeableOf_AnyContentAsText

/*
 * Reusable session JSON objects. 
 */
object SessionSpec {

/*
   val foo = JsObject(Seq(
      "sid" -> JsString("SID"),
      "ts" -> JsNumber(System.currentTimeMillis()),
      "schid" -> JsString("SCHID"),
      "request" -> JsObject(Seq(
         "state" -> JsString("STATE"),
         "status" -> JsString("OK"),
         "committed" -> JsBoolean(true),
         "params" -> JsArray(Seq(
            JsObject(Seq(   
               "name" -> JsString("Param One"),
               "value" -> JsString("Pram One Value")
            )),
            JsObject(Seq(   
               "name" -> JsString("Param One"),
               "value" -> JsString("Pram One Value")
            ))
         )),
         "exps" -> JsArray(Seq(
            JsString("test1.A.true"),
            JsString("test2.B.false"),
            JsString("test3.C.false")
         ))
      )),
      "states" -> JsArray(Seq(
         JsObject(Seq(   
            "state" -> JsString("state1"),
            "count" -> JsNumber(2)
         )),
         JsObject(Seq(   
            "state" -> JsString("state2"),
            "count" -> JsNumber(23)
         )),
         JsObject(Seq(   
            "state" -> JsString("state3"),
            "count" -> JsNumber(32)
         ))
      )),
      "tests" -> JsArray(Seq(
         JsObject(Seq(   
            "test" -> JsString("state1"),
            "qual" -> JsBoolean(true)
         )),
         JsObject(Seq(   
            "test" -> JsString("state2"),
            "qual" -> JsBoolean(false)
         )),
         JsObject(Seq(   
            "test" -> JsString("state3"),
            "qual" -> JsBoolean(true)
         ))
      ))
   ))
*/

   val body = ParamString("""
      {"sid": "${sid:SID}",
       "ts": ${ts:%d},
       "schid": "SCHID", 
       "request": {"state": "state1","status": "OK","committed": true, 
                  "params": [{"name": "Param One", "value": "Param One Value"},{"name": "Param Two", "value": "Param Two Value"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "tests": [{"test": "test1","qualified": true},{"test": "test2","qualified": true}]}
   """.format(System.currentTimeMillis()))

   val b = ParamString("""
      {
         "sid":"ssn2",
         "ts":1476926526963,
         "schid":"BFFF42B28ED22C6D",
         "req":{
            "state":"state2",
            "status":"OK",
            "comm":true,
            "params":[{"key":"PATH","val":"/path/to/state2/test2.C+test5.B"}],
            "exps":["test1.A.true","test2.C.false","test3.A.true","test4.A.true","test5.B.false","test6.C.false"]
         },
         "states":[{"state":"state2","count":1}],
         "tests":["test1","test2","test3","test4","test5","test6"],
         "stabil":["test2.C.1476926526964","test3.A.1476926526964","test4.A.1476926526965","test5.B.1476926526965","test6.C.1476926526965","test1.A.1476926526966"]
      }""")

}

/**
 * Session Controller Tests
 */
class SessionSpec extends VariantSpec {
   
   val endpoint = context + "/session"

   "SessionController" should {

      "return 404 on GET no SID" in {
         
         val resp = route(app, FakeRequest(GET, endpoint + "/").withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }

      "return 404 on GET non-existent session" in {   
         
         val resp = route(app, FakeRequest(GET, endpoint + "/foo").withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }
   
      "return 200 on PUT non-existent session" in {
         
         val textBody = SessionSpec.body.expand("sid" -> "foo1")
         val resp = route(app, FakeRequest(PUT, endpoint + "/foo").withTextBody(textBody)).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
      }

      "return existing session on GET and return 200" in {
       
         val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe SessionSpec.body.expand("sid" -> "foo1")
      }

      "replace existing session on PUT and return 200" in {
       
         val reqPut = FakeRequest(PUT, endpoint + "/foo").withTextBody(SessionSpec.body.expand("sid" -> "foo2"))
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(respGet) mustBe OK
         contentAsString(respGet) mustBe SessionSpec.body.expand("sid" -> "foo2")
      }

      "create session on PUT and return 200" in {
       
         val reqPut = FakeRequest(PUT, endpoint + "/bar").withTextBody(SessionSpec.body.expand("sid" -> "bar1"))
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, FakeRequest(GET, endpoint + "/bar")).get
         status(respGet) mustBe OK
         contentAsString(respGet) mustBe SessionSpec.body.expand("sid" -> "bar1")
      }

     "not lose existing session with different key" in {

         val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe SessionSpec.body.expand("sid" -> "foo2")
      }

     "expire existing sessions after timeout" in {
        val timeout = boot.config().getInt(SessionTimeoutSecs)
        val vacuumInterval = boot.config().getInt(SessionStoreVacuumIntervalSecs)
        timeout  mustEqual 1
        vacuumInterval  mustEqual 1
      
        Thread.sleep((timeout * 1000 * 2).asInstanceOf[Long]);
      
        ("foo" :: "bar" :: Nil)
           .foreach(sid => status(route(app, FakeRequest(GET, endpoint + "/" + sid)).get) mustBe NOT_FOUND)  
      }
      
      "deserialize payload into session object" in {
       
         val sid = Random.nextInt(100000).toString
         val ts = System.currentTimeMillis()
         val reqPut = FakeRequest(PUT, endpoint + "/" + sid).withTextBody(SessionSpec.body.expand("sid" -> sid, "ts" -> ts))
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         store.asString(sid).get mustBe SessionSpec.body.expand("sid" -> sid, "ts" -> ts)
         val session = store.asSession(sid.toString).get
         session.creationTimestamp() mustBe ts
         session.getId mustBe sid
      
         Thread.sleep(2000);
         store.asSession(sid) mustBe empty

      }

   }
}
