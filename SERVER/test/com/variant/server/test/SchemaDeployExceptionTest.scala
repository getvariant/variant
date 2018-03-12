package com.variant.server.test

import scala.util.Random
import org.scalatestplus.play._
import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.server.test.util.EventReader
import com.variant.server.api.ConfigKeys
import com.variant.server.conn.SessionStore
import com.variant.server.boot.VariantServer
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.TestData
import play.api.Configuration
import com.variant.server.boot.VariantApplicationLoader
import com.variant.core.RuntimeError._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.server.api.ServerException
import org.apache.commons.io.FileUtils
import java.io.File
import scala.util.Try
import scala.reflect.io.Path
import com.variant.core.UserError.Severity
import com.variant.server.boot.ServerErrorLocal

/**
 * Test various schema deployment error scenarios
 */
class SchemaDeployExceptionTest extends PlaySpec with OneAppPerTest {
   
  /**
   * This will implicitly rebuild the server before each test.
   */
   implicit override def newAppForTest(testData: TestData): Application = {

      if (testData.name.contains("CONFIG_PROPERTY_NOT_SET")) 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config.withoutPath("variant.schemata.dir")))
            .build() 
      else if (testData.name.contains("SCHEMATA_DIR_MISSING")) 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(Map("variant.schemata.dir" -> "non-existent"))
            .build()
      else if (testData.name.contains("SCHEMATA_DIR_NOT_DIR")) 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(Map("variant.schemata.dir" -> "test-schemata-file"))
            .build()
      else if (testData.name.contains("SCHEMA_NAME_DUPE")) {
         // Delete directory
         val path: Path = Path ("/tmp/test-schemata")
         Try(path.deleteRecursively())
         
         FileUtils.copyFile(new File("conf-test/ParserCovariantOkayBigTestNoHooks.json"), new File("/tmp/test-schemata/schema1.json"))
         FileUtils.copyFile(new File("conf-test/ParserCovariantOkayBigTestNoHooks.json"), new File("/tmp/test-schemata/schema2.json"))
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(
               Map("variant.schemata.dir" -> "/tmp/test-schemata")) 
            .build()
      }
      else 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .build()
   }

   
   "Missing variant.schemata.dir property" should {
      "throw CONFIG_PROPERTY_NOT_SET" in {
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         server.schemata.size mustBe 0
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         //ex.getSeverity mustEqual FATAL
         ex.getMessage mustEqual 
            new ServerException.User(CONFIG_PROPERTY_NOT_SET, ConfigKeys.SCHEMATA_DIR).getMessage
      }
   }

   "Missing schemata dir" should {
      
      "cause server to throw SCHEMATA_DIR_MISSING" in {
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         server.schemata.size mustBe 0
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         //ex.getSeverity mustEqual FATAL
         ex.getMessage mustEqual new ServerException.User(SCHEMATA_DIR_MISSING, "non-existent").getMessage
      }
      
      "return 503 in every http request after SCHEMATA_DIR_MISSING" in {
         val context = app.configuration.getString("play.http.context").get
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false 
         val resp = route(app, FakeRequest(GET, context + "/session/foo")).get
         status(resp) mustBe SERVICE_UNAVAILABLE
         contentAsString(resp) mustBe empty
      }
   }
   
   "Schemata dir which is not a dir" should {
      
      "cause server to throw SCHEMATA_DIR_NOT_DIR" in {
         val server = app.injector.instanceOf[VariantServer]
         server.schemata.size mustBe 0
         server.isUp mustBe false
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         //ex.getSeverity mustEqual FATAL
         ex.getMessage mustEqual new ServerException.User(SCHEMATA_DIR_NOT_DIR, "test-schemata-file").getMessage
      }
      
      "return 503 in every http request after SCHEMATA_DIR_NOT_DIR" in {
         val context = app.configuration.getString("play.http.context").get
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe false
         val resp = route(app, FakeRequest(GET, context + "/session/foo")).get
         status(resp) mustBe SERVICE_UNAVAILABLE
         contentAsString(resp) mustBe empty
      }
   }

   "Multiple schemata with duplicate schema name" should {
      
      "cause server to throw SCHEMA_NAME_DUPE" in {
         val server = app.injector.instanceOf[VariantServer]
         server.schemata.size mustBe 1
         server.isUp mustBe true
         server.startupErrorLog.size mustEqual 0
         val resp1 = server.schemaDeployer.parserResponses(0)
         resp1.hasMessages() mustBe false
         val resp2 = server.schemaDeployer.parserResponses(1)
         resp2.getMessages().size() mustBe 1
         val msg = resp2.getMessages().get(0)
         msg.getSeverity mustBe Severity.ERROR
   		msg.getText must include (ServerErrorLocal.SCHEMA_CANNOT_REPLACE.asMessage("ParserCovariantOkayBigTestNoHooks", "foo", "bar"))
      }
      
   }

}
