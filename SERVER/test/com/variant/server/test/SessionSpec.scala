package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder

/**
 * Session Controller
 */
class SessionSpec extends VariantSpec {

   val endpoint = context + "/session"
   val body = "Does note matter becasuse we don't parse eagarly and expect a text content type"
   
   "SessionController" should {

      "GET non-existent session should return 404" in {
       
         val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
    }
   
      "PUT non-existent session should return 200" in {
       
         val req = FakeRequest(PUT, endpoint + "/foo").withTextBody(body + 1)
         val resp = route(app, req).get
         println(contentAsString(resp))
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
    }

    "GET existing session should return it" in {
       
         val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe (body + 1)
    }
    
    "PUT existing session should replace it and return 200" in {
       
         val reqPut = FakeRequest(PUT, endpoint + "/foo").withTextBody(body + 2)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(respGet) mustBe OK
         contentAsString(respGet) mustBe (body + 2)
    }

    "PUT another non-existent session should create it and return 200" in {
       
         val reqPut = FakeRequest(PUT, endpoint + "/bar").withTextBody(body + 3)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, FakeRequest(GET, endpoint + "/bar")).get
         status(respGet) mustBe OK
         contentAsString(respGet) mustBe (body + 3)
    }

   "The old session should still be there" in {

         val resp = route(app, FakeRequest(GET, endpoint + "/foo")).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe (body + 2)
    }

   "Both sessions should expire after 2 seconds" in {
      app.configuration.getInt("variant.session.timeout.secs").get mustEqual 1
      app.configuration.getInt("variant.session.store.vacuum.interval.secs").get mustEqual 1
      
      Thread.sleep(2000);
      
      ("foo" :: "bar" :: Nil)
         .foreach(sid => status(route(app, FakeRequest(GET, endpoint + "/" + sid)).get) mustBe NOT_FOUND)  
   }
  }

/*
  "and" should {

    "return an increasing count" in {
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "0"
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "1"
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "2"
    }

  }
*/
}
