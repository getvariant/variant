package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import com.variant.server.UserError

/*
 * Reusable event JSON objects. 
 */
object EventSpec {

   val body = JsObject(Seq(
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

      // All tests try text and json bodies.
      for (contType <- List("text", "json")) {

         "return  400 and error on POST with no body and " + contType + " body" in {
   
            val resp = contType match {
               case "text" => route(app, FakeRequest(POST, endpoint).withHeaders("Content-Type" -> "text/plain")).get
               case "json" => route(app, FakeRequest(POST, endpoint).withHeaders("Content-Type" -> "application/json")).get
            }
            status(resp) mustBe BAD_REQUEST
            
            println("This should really return the no body error")
            contentAsString(resp) must include ("No content")        
        }
         
         "return  400 and error on POST with invalid JSON and " + contType + " body" in {
   
            val resp = contType match {
               case "text" => route(app, FakeRequest(POST, endpoint).withBody("bad json").withHeaders("Content-Type" -> "text/plain")).get
               case "json" => route(app, FakeRequest(POST, endpoint).withBody("bad json").withHeaders("Content-Type" -> "application/json")).get
            }
            status(resp) mustBe BAD_REQUEST
            
            println("This should really return the no body error")
            contentAsString(resp) must include ("Unrecognized token")        
        }

         "return  400 and error on POST with no sid and " + contType + " body" in {
   
            val body = EventSpec.body.transform((__ \ "sid").json.prune).get
            val resp = contType match {
               case "text" => route(app, FakeRequest(POST, endpoint).withTextBody(body.toString())).get
               case "json" => route(app, FakeRequest(POST, endpoint).withJsonBody(body)).get
            }
            status(resp) mustBe BAD_REQUEST
            contentAsString(resp) mustBe UserError.errors(UserError.MissingProperty).asMessage("sid")
         }

         "return  400 and error on POST with no name and " + contType + " body" in {
   
            val body = EventSpec.body.transform((__ \ "name").json.prune).get
            val resp = contType match {
               case "text" => route(app, FakeRequest(POST, endpoint).withTextBody(body.toString())).get
               case "json" => route(app, FakeRequest(POST, endpoint).withJsonBody(body)).get
            }
            status(resp) mustBe BAD_REQUEST
            contentAsString(resp) mustBe UserError.errors(UserError.MissingProperty).asMessage("name")
         }
         
         "return  403 and error on POST with non-existent session and " + contType + " body" in {
   
            val resp = contType match {
               case "text" => route(app, FakeRequest(POST, endpoint).withTextBody(EventSpec.body.toString())).get
               case "json" => route(app, FakeRequest(POST, endpoint).withJsonBody(EventSpec.body)).get
            }
            status(resp) mustBe FORBIDDEN
            contentAsString(resp) mustBe UserError.errors(UserError.SessionExpired).asMessage("name")
         }

      }
      
   }
}
