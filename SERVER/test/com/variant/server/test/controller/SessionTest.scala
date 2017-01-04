package com.variant.server.test.controller

import scala.util.Random
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParamString
import com.variant.server.ConfigKeys
import com.variant.server.test.BaseSpecWithSchema

/*
 * Reusable session JSON objects. 
 */
object SessionTest {

   val body = ParamString("""
      {"sid": "${sid:SID}",
       "ts": ${ts:%d},
       "schid": "SCHID", 
       "request": {"state": "state1","status": "OK","committed": true, 
                  "params": [{"name": "Param One", "value": "Param One Value"},{"name": "Param Two", "value": "Param Two Value"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "tests": ["test1","test2"]}
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
         "disqualTests":["test1","test2","test3","test4","test5","test6"],
         "stabil":["test2.C.1476926526964","test3.A.1476926526964","test4.A.1476926526965","test5.B.1476926526965","test6.C.1476926526965","test1.A.1476926526966"]
      }""")

}

/**
 * Session Controller Tests
 */
class SessionTest extends BaseSpecWithSchema {
   
   import SessionTest._
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
         
         val textBody = body.expand("sid" -> "foo1")
         val resp = route(app, FakeRequest(PUT, endpoint + "/foo").withTextBody(textBody)).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
      }

      "return existing session on GET and return 200" in {
       
         val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(resp) mustBe OK
         val respAsJson = contentAsJson(resp)
         (respAsJson \ "session").as[String] mustBe body.expand("sid" -> "foo1")
      }

      "replace existing session on PUT and return 200" in {
       
         val reqPut = FakeRequest(PUT, endpoint + "/foo").withTextBody(body.expand("sid" -> "foo2"))
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(respGet) mustBe OK
         val respAsJson = contentAsJson(respGet)
         (respAsJson \ "session").as[String] mustBe body.expand("sid" -> "foo2")
      }

      "create session on PUT and return 200" in {
       
         val reqPut = FakeRequest(PUT, endpoint + "/bar").withTextBody(body.expand("sid" -> "bar1"))
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, FakeRequest(GET, endpoint + "/bar")).get
         status(respGet) mustBe OK
         val respAsJson = contentAsJson(respGet)
         (respAsJson \ "session").as[String] mustBe body.expand("sid" -> "bar1")
      }

     "not lose existing session with different key" in {

         val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(resp) mustBe OK
         val respAsJson = contentAsJson(resp)
         (respAsJson \ "session").as[String] mustBe body.expand("sid" -> "foo2")
      }

     "expire existing sessions after timeout" in {
        val timeout = server.config.getLong(ConfigKeys.SESSION_TIMEOUT)
        val vacuumInterval = server.config.getLong(ConfigKeys.SESSION_STORE_VACUUM_INTERVAL)
        timeout  mustEqual 1
        vacuumInterval  mustEqual 1
      
        Thread.sleep((timeout * 1000 * 2).asInstanceOf[Long]);
      
        ("foo" :: "bar" :: Nil)
           .foreach(sid => status(route(app, FakeRequest(GET, endpoint + "/" + sid)).get) mustBe NOT_FOUND)  
      }
      
      "deserialize payload into session object" in {
       
         val sid = Random.nextInt(100000).toString
         val ts = System.currentTimeMillis()
         val reqPut = FakeRequest(PUT, endpoint + "/" + sid).withTextBody(body.expand("sid" -> sid, "ts" -> ts))
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         store.asString(sid).get mustBe body.expand("sid" -> sid, "ts" -> ts)
         val session = store.asSession(sid.toString).get
         session.creationTimestamp mustBe ts
         session.getId mustBe sid
      
         Thread.sleep(2000);
         store.asSession(sid) mustBe empty

      }

   }
}
