package com.variant.server.test

import scala.util.Random
import org.scalatestplus.play._
import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.UserError
import com.variant.server.test.util.ParamString
import com.variant.server.ServerPropertiesKey._
import com.variant.server.test.util.EventReader
import com.variant.server.ServerPropertiesKey._
import com.variant.server.session.SessionStore
import com.variant.server.boot.VariantServer
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.TestData
import com.variant.core.exception.Error
import com.variant.server.ServerErrorException
import com.variant.server.ServerError

/**
 * Test various schema deployment error scenarios
 */
class SchemaDeployExceptionTest extends PlaySpec with OneAppPerTest {
   
   implicit override def newAppForTest(testData: TestData): Application = {
      new GuiceApplicationBuilder()
         .configure(
            Map(
                  "variant.schemas.dir" -> {
                     if (testData.name.endsWith("SCHEMAS_DIR_MISSING")) "non-existent" 
                     else if (testData.name.endsWith("SCHEMAS_DIR_NOT_DIR")) "test-schemas-file"
                     else if (testData.name.endsWith("MULTIPLE_SCHEMAS_NOT_SUPPORTED")) "test-schemas-multi"
                     else "don't know what to do"
                  }
            ))
       .build()
   }
 
   "Missing schemas dir" should {
      
      "cause server to throw SCHEMAS_DIR_MISSING" in {
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         server.schema.isDefined mustBe false
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         ex.getSeverity mustEqual Error.Severity.FATAL
         ex.getMessage mustEqual new ServerErrorException(ServerError.SCHEMAS_DIR_MISSING, "non-existent").getMessage
      }
      
      "return 503 in every http request" in {
         val context = app.configuration.getString("play.http.context").get
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         val resp = route(app, FakeRequest(GET, context + "/session/foo")).get
         status(resp) mustBe SERVICE_UNAVAILABLE
         contentAsString(resp) mustBe empty
      }
   }
   
   "Schemas dir which is not a dir" should {
      
      "cause server to throw SCHEMAS_DIR_NOT_DIR" in {
         val server = app.injector.instanceOf[VariantServer]
         server.schema.isDefined mustBe false
         server.isUp mustBe false
         server.schema.isDefined mustBe false
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         ex.getSeverity mustEqual Error.Severity.FATAL
         ex.getMessage mustEqual new ServerErrorException(ServerError.SCHEMAS_DIR_NOT_DIR, "test-schemas-file").getMessage
      }
      
      "return 503 in every http request" in {
         val context = app.configuration.getString("play.http.context").get
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         val resp = route(app, FakeRequest(GET, context + "/session/foo")).get
         status(resp) mustBe SERVICE_UNAVAILABLE
         contentAsString(resp) mustBe empty
      }
   }
   
   "Schemas dir with multiple files" should {

      "cause server to throw MULTIPLE_SCHEMAS_NOT_SUPPORTED" in {
         val server = app.injector.instanceOf[VariantServer]
         server.schema.isDefined mustBe false
         server.isUp mustBe false
         server.schema.isDefined mustBe false
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         ex.getSeverity mustEqual Error.Severity.FATAL
         ex.getMessage mustEqual new ServerErrorException(ServerError.MULTIPLE_SCHEMAS_NOT_SUPPORTED, "test-schemas-multi").getMessage
      }

      "return 503 in every http request" in {
         val context = app.configuration.getString("play.http.context").get
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         val resp = route(app, FakeRequest(GET, context + "/session/foo")).get
         status(resp) mustBe SERVICE_UNAVAILABLE
         contentAsString(resp) mustBe empty
      }
   }
}
