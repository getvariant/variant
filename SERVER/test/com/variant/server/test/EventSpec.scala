package com.variant.server.test

import com.variant.core.impl.CoreSessionImpl
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import com.variant.server.UserError
import com.variant.server.test.util.ParamString
import scala.util.Random
import scala.concurrent.duration._

/*
 * Reusable event JSON objects. 
 */
object EventSpec {

   /*
   val foo = JsObject(Seq(
        "sid" -> JsString("SID"),
        "name" -> JsString("Event Name"),
        "value" -> JsString("Event Value"),
        "ts" -> JsNumber(System.currentTimeMillis()),
        "params" -> JsArray(Seq(
           JsObject(Seq(   
              "name" -> JsString("Param One"),
              "value" -> JsString("Pram One Value")
           )),
           JsObject(Seq(   
              "name" -> JsString("Param One"),
              "value" -> JsString("Pram One Value")
           ))
        ))
   ))
*/
   val body = ParamString("""
      {"sid":"${sid:SID}",
       "name":"NAME",
       "value":"VALUE",
       "ts":%d,
       "params":[{"name":"Name One","value":"Value One"},{"name":"Name Two","value":"Value Two"}]
      }
   """.format(System.currentTimeMillis()))
      
   val bodyNoSid = """{"name":"NAME","value":"VALUE"}"""
   val bodyNoName = """{"sid":"SID","value":"VALUE"}"""
   val bodyNoParamName = ParamString("""
      {"sid":"${sid:SID}",
       "name":"NAME",
       "value":"VALUE",
       "ts":%d,
       "params":[{"namee":"Name One","value":"Value One"}]
      }
   """.format(System.currentTimeMillis()))

}

/**
 * Event Controller
 */
class EventSpec extends VariantSpec {
   
   val endpoint = context + "/event"

   "EventController" should {

      "return 404 on GET" in {
         val resp = route(app, FakeRequest(GET, endpoint)).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }
      
      "return 404 on PUT" in {
         val resp = route(app, FakeRequest(PUT, endpoint)).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }


      "return  400 and error on POST with no body" in {
         val resp = route(app, FakeRequest(POST, endpoint).withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe BAD_REQUEST
         contentAsString(resp) must startWith ("JSON parsing error")        
         contentAsString(resp) must include ("No content")        
     }
      
      "return  400 and error on POST with invalid JSON" in {
         val resp = route(app, FakeRequest(POST, endpoint).withBody("bad json").withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe BAD_REQUEST
         contentAsString(resp) must startWith ("JSON parsing error")        
         contentAsString(resp) must include ("Unrecognized token")        
     }

      "return  400 and error on POST with no sid" in {
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(EventSpec.bodyNoSid)).get
         status(resp) mustBe BAD_REQUEST
         contentAsString(resp) mustBe UserError.errors(UserError.MissingProperty).asMessage("sid")
      }

      "return  400 and error on POST with no name" in {
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(EventSpec.bodyNoName)).get
         status(resp) mustBe BAD_REQUEST
         contentAsString(resp) mustBe UserError.errors(UserError.MissingProperty).asMessage("name")
      }
      
      "return  403 and error on POST with non-existent session" in {
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(EventSpec.body.expand())).get
         status(resp) mustBe FORBIDDEN
         contentAsString(resp) mustBe UserError.errors(UserError.SessionExpired).asMessage("name")
      }
     
      "return 400 and error on POST with missing param name" in {
         val eventBody = EventSpec.bodyNoParamName.expand()
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(eventBody)).get
         status(resp)mustBe BAD_REQUEST
         contentAsString(resp) mustBe UserError.errors(UserError.MissingParamName).asMessage()
      }

      "return 200 and create event with existent session" in {
         //status(resp) mustBe (OK)
         val sid = Random.nextInt(100000).toString
         // PUT session.
         val ssnBody = SessionSpec.body.expand("sid" -> sid)
         val ssnResp = route(app, FakeRequest(PUT, context + "/session/" + sid).withTextBody(ssnBody)).get
         status(ssnResp) mustBe OK
         contentAsString(ssnResp) mustBe empty
         val ssn = store.asSession(sid)
         println("**** " + ssn.get.getStateRequest)
         // POST event
         val eventBody = EventSpec.body.expand("sid" -> sid)
         val resp = route(app, FakeRequest(POST, endpoint).withTextBody(eventBody)).get
         //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
         contentAsString(resp) mustBe empty
      }
      
   }
}
