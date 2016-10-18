package com.variant.server.test

import scala.math.BigDecimal.int2bigDecimal
import scala.math.BigDecimal.long2bigDecimal
import com.variant.server.test.util.ParamString
import play.api.libs.json.JsArray
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
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
import com.variant.server.session.SessionStore
import scala.util.Random
import com.variant.core.impl.CoreSessionImpl

/*
 * Reusable session JSON objects. 
 */
object SessionSpec {


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


   val body = ParamString("""
      {"sid": "${sid:SID}",
       "ts": ${ts:%d},
       "schid": "SCHID", 
       "request": {"state": "state1","status": "OK","committed": true, 
                  "params": [{"name": "Param One", "value": "Param One Value"},{"name": "Param Two", "value": "Param Two Value"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "tests": [{"test": "test1","qualified": true},{"test": "test1","qualified": true}]}
   """.format(System.currentTimeMillis()))

}

/**
 * Session Controller Tests
 */
class SessionSpec extends VariantSpec {
   
   import com.variant.server.test.SessionSpec._

   val endpoint = context + "/session"

   "SessionController" should {

      // All tests try text and json bodies.
      for (contType <- List("text", "json")) {

         "return 404 on GET no SID with " + contType + " body" in {
          
            val resp = contType match {
               case "text" => route(app, FakeRequest(GET, endpoint + "/").withHeaders("Content-Type" -> "text/plain")).get
               case "json" => route(app, FakeRequest(GET, endpoint + "/").withHeaders("Content-Type" -> "application/json")).get
            }
            status(resp) mustBe NOT_FOUND
            contentAsString(resp) mustBe empty
         }

         "return 404 on GET non-existent session with " + contType + " body" in {
          
            val resp = contType match {
               case "text" => route(app, FakeRequest(GET, endpoint + "/foo").withHeaders("Content-Type" -> "text/plain")).get
               case "json" => route(app, FakeRequest(GET, endpoint + "/foo").withHeaders("Content-Type" -> "application/json")).get
            }
            status(resp) mustBe NOT_FOUND
            contentAsString(resp) mustBe empty
         }
      
         "return 200 on PUT non-existent session with " + contType + " body" in {

            val req = FakeRequest(PUT, endpoint + "/foo").withTextBody(body.expand("sid" -> "foo1"))
            val resp = route(app, req).get
            println(contentAsString(resp))
            status(resp) mustBe OK
            contentAsString(resp) mustBe empty
         }
   
         "return existing session on GET and return 200 with " + contType + " body" in {
          
            val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
            status(resp) mustBe OK
            contentAsString(resp) mustBe body.expand("sid" -> "foo1")
         }

         "replace existing session on PUT and return 200 with " + contType + " body" in {
          
            val reqPut = FakeRequest(PUT, endpoint + "/foo").withTextBody(body.expand("sid" -> "foo2"))
            val respPut = route(app, reqPut).get
            status(respPut) mustBe OK
            contentAsString(respPut) mustBe empty
            
            val respGet = route(app, FakeRequest(GET, endpoint + "/foo")).get
            status(respGet) mustBe OK
            contentAsString(respGet) mustBe body.expand("sid" -> "foo2")
         }
   
         "create session on PUT and return 200 with " + contType + " body" in {
          
            val reqPut = FakeRequest(PUT, endpoint + "/bar").withTextBody(body.expand("sid" -> "bar1"))
            val respPut = route(app, reqPut).get
            status(respPut) mustBe OK
            contentAsString(respPut) mustBe empty
            
            val respGet = route(app, FakeRequest(GET, endpoint + "/bar")).get
            status(respGet) mustBe OK
            contentAsString(respGet) mustBe body.expand("sid" -> "bar1")
         }
   
        "not lose existing session with different key and " + contType + " body" in {
   
            val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
            status(resp) mustBe OK
            contentAsString(resp) mustBe body.expand("sid" -> "foo2")
         }
   
        "expire existing sessions after timeout with " + contType + " body" in {
            app.configuration.getInt("variant.session.timeout.secs").get mustEqual 1
            app.configuration.getInt("variant.session.store.vacuum.interval.secs").get mustEqual 1
         
            Thread.sleep(2000);
         
            ("foo" :: "bar" :: Nil)
               .foreach(sid => status(route(app, FakeRequest(GET, endpoint + "/" + sid)).get) mustBe NOT_FOUND)  
         }

      }
      
      "deserialize payload into session object" in {
       
         val sid = Random.nextInt(100000).toString
         val ts = System.currentTimeMillis()
         val reqPut = FakeRequest(PUT, endpoint + "/" + sid).withTextBody(body.expand("sid" -> sid, "ts" -> ts))
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         store.asString(sid.toString).get mustBe body.expand("sid" -> sid, "ts" -> ts)
         
         val session = store.asSession(sid.toString).get
         //println(session.asInstanceOf[CoreSessionImpl].toJson())
         session.creationTimestamp() mustBe ts
         session.getId mustBe sid
      }
      
   }
}
