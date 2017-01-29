package com.variant.server.test

import scala.util.Random
import org.scalatestplus.play._
import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.server.test.util.ParamString
import com.variant.server.test.util.EventReader
import com.variant.server.ConfigKeys
import com.variant.server.conn.SessionStore
import com.variant.server.boot.VariantServer
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.TestData
import play.api.Configuration
import com.variant.server.boot.VariantApplicationLoader
import com.variant.core.CommonError._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.server.ServerException

/**
 * Test various schema deployment error scenarios
 */
class SchemaDeployExceptionTest extends PlaySpec with OneAppPerTest {
   
   implicit override def newAppForTest(testData: TestData): Application = {

      if (testData.name.contains("CONFIG_PROPERTY_NOT_SET")) 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config.withoutPath("variant.schemas.dir")))
            .build()
      else if (testData.name.contains("SCHEMAS_DIR_MISSING")) 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(Map("variant.schemas.dir" -> "non-existent"))
            .build()
      else if (testData.name.contains("SCHEMAS_DIR_NOT_DIR")) 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(Map("variant.schemas.dir" -> "test-schemas-file"))
            .build()
      else if (testData.name.contains("MULTIPLE_SCHEMAS_NOT_SUPPORTED")) 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(Map("variant.schemas.dir" -> "test-schemas-multi"))
            .build()
      else 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .build()
   }

   
   "Missing variant.schemas.dir property" should {
      "throw CONFIG_PROPERTY_NOT_SET" in {
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         server.schema.isDefined mustBe false
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         ex.getSeverity mustEqual FATAL
         ex.getMessage mustEqual 
            new ServerException.User(CONFIG_PROPERTY_NOT_SET, ConfigKeys.SCHEMAS_DIR).getMessage
      }
   }

   "Missing schemas dir" should {
      
      "cause server to throw SCHEMAS_DIR_MISSING" in {
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         server.schema.isDefined mustBe false
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         ex.getSeverity mustEqual FATAL
         ex.getMessage mustEqual new ServerException.User(SCHEMAS_DIR_MISSING, "non-existent").getMessage
      }
      
      "return 503 in every http request after SCHEMAS_DIR_MISSING" in {
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
         ex.getSeverity mustEqual FATAL
         ex.getMessage mustEqual new ServerException.User(SCHEMAS_DIR_NOT_DIR, "test-schemas-file").getMessage
      }
      
      "return 503 in every http request after SCHEMAS_DIR_NOT_DIR" in {
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
         ex.getSeverity mustEqual FATAL
         ex.getMessage mustEqual new ServerException.User(MULTIPLE_SCHEMAS_NOT_SUPPORTED, "test-schemas-multi").getMessage
      }

      "return 503 in every http request after MULTIPLE_SCHEMAS_NOT_SUPPORTED" in {
         val context = app.configuration.getString("play.http.context").get
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         server.schema.isDefined mustBe false
         val resp = route(app, FakeRequest(GET, context + "/session/foo")).get
         status(resp) mustBe SERVICE_UNAVAILABLE
         contentAsString(resp) mustBe empty
      }
   }
}
