package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import com.variant.server.test.util.ParamString

/*
 * Reusable session JSON objects. 
 */
object SessionSpec extends VariantSpec {


   val body = JsObject(Seq(
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


   val foo = ParamString("""
      {"sid": "${sid:SID}",
       "ts": %s,
       "schid": "SCHID", 
       "request": {"state": "state1","status": "OK","comm": true, 
                  "params": [{"key": "KEY1", "val": "VAL1"},{"key": "KEY2", "val": "VAL2"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "tests": [{"test": "test1","qual": true},{"test": "test1","qual": true}]}
   """.format(System.currentTimeMillis()))

}

/**
 * Session Controller Tests
 */
class SessionSpec extends VariantSpec {

      println(SessionSpec.foo.expand())

   val endpoint = context + "/session"
   val body = "Does note matter becasuse we don't parse eagarly and expect a text content type"
   
   "SessionController" should {

      // All tests try text and json bodies.
      for (contType <- List("text", "json")) {

         "return 404 on GET non-existent session with " + contType + " body" in {
          
            val resp = contType match {
               case "text" => route(app, FakeRequest(GET, endpoint + "/foo").withHeaders("Content-Type" -> "text/plain")).get
               case "json" => route(app, FakeRequest(GET, endpoint + "/foo").withHeaders("Content-Type" -> "application/json")).get
            }
            status(resp) mustBe NOT_FOUND
            contentAsString(resp) mustBe empty
         }
      
         "return 200 on PUT non-existent session with " + contType + " body" in {


            val req = FakeRequest(PUT, endpoint + "/foo").withTextBody(body + 1)
            val resp = route(app, req).get
            println(contentAsString(resp))
            status(resp) mustBe OK
            contentAsString(resp) mustBe empty
         }
   
         "return existing session on GET and return 200 with " + contType + " body" in {
          
            val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
            status(resp) mustBe OK
            contentAsString(resp) mustBe (body + 1)
         }

         "replace existing session on PUT and return 200 with " + contType + " body" in {
          
            val reqPut = FakeRequest(PUT, endpoint + "/foo").withTextBody(body + 2)
            val respPut = route(app, reqPut).get
            status(respPut) mustBe OK
            contentAsString(respPut) mustBe empty
            
            val respGet = route(app, FakeRequest(GET, endpoint + "/foo")).get
            status(respGet) mustBe OK
            contentAsString(respGet) mustBe (body + 2)
         }
   
         "create session on PUT and return 200 with " + contType + " body" in {
          
            val reqPut = FakeRequest(PUT, endpoint + "/bar").withTextBody(body + 3)
            val respPut = route(app, reqPut).get
            status(respPut) mustBe OK
            contentAsString(respPut) mustBe empty
            
            val respGet = route(app, FakeRequest(GET, endpoint + "/bar")).get
            status(respGet) mustBe OK
            contentAsString(respGet) mustBe (body + 3)
         }
   
        "not lose existing session with different key and " + contType + " body" in {
   
            val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
            status(resp) mustBe OK
            contentAsString(resp) mustBe (body + 2)
         }
   
        "expire existing sessions after timeout with " + contType + " body" in {
            app.configuration.getInt("variant.session.timeout.secs").get mustEqual 1
            app.configuration.getInt("variant.session.store.vacuum.interval.secs").get mustEqual 1
         
            Thread.sleep(2000);
         
            ("foo" :: "bar" :: Nil)
               .foreach(sid => status(route(app, FakeRequest(GET, endpoint + "/" + sid)).get) mustBe NOT_FOUND)  
         }

      }
   }
}
