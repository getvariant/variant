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
import com.variant.server.test.spec.BaseSpec
import com.variant.server.test.util.LogSniffer

/**
 * Test various schema deployment error scenarios
 */
class SchemaDeployExceptionTest extends BaseSpec with OneAppPerTest {
   
  /**
   * This will implicitly rebuild the server before each test.
   */
   implicit override def newAppForTest(testData: TestData): Application = {

      if (testData.name.contains("CONFIG_PROPERTY_NOT_SET"))  {
            _app = new GuiceApplicationBuilder()
               .configure(new Configuration(VariantApplicationLoader.config.withoutPath("variant.schemata.dir")))
               .build()
            _app
      }
      else if (testData.name.contains("SCHEMATA_DIR_MISSING")) {
         _app = new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(Map("variant.schemata.dir" -> "non-existent"))
            .build()
            _app
      }
      else if (testData.name.contains("SCHEMATA_DIR_NOT_DIR")) {
         _app = new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure("variant.schemata.dir" -> "test-schemata-file")
            .build()
         _app
      }
      else if (testData.name.contains("SCHEMA_NAME_DUPE")) {
         // Delete directory
         val path: Path = Path ("/tmp/schemata-test")
         Try(path.deleteRecursively())
         
         FileUtils.copyFile(new File("conf-test/ParserCovariantOkayBigTestNoHooks.json"), new File("/tmp/schemata-test/schema1.json"))
         FileUtils.copyFile(new File("conf-test/ParserCovariantOkayBigTestNoHooks.json"), new File("/tmp/schemata-test/schema2.json"))
         _app = new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure("variant.schemata.dir" -> "/tmp/schemata-test")
            .build()
         _app
      }
      else {
         _app = new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .build()
         _app
      }
   }

   private var _app: Application = null
   
   override protected def application = _app

   "Missing variant.schemata.dir property" should {
      
      "throw CONFIG_PROPERTY_NOT_SET" in {
         val server = app.injector.instanceOf[VariantServer]
         server.schemata.size mustBe 0
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         ex.getMessage mustEqual
            new ServerException.User(CONFIG_PROPERTY_NOT_SET, ConfigKeys.SCHEMATA_DIR).getMessage
   		server.isUp mustBe false
      }
   }

   "Missing schemata dir" should {
      
      "cause server to throw SCHEMATA_DIR_MISSING" in {
         server.schemata.size mustBe 0
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         ex.getSeverity mustEqual SCHEMATA_DIR_MISSING.getSeverity
         ex.getMessage mustEqual new ServerException.User(SCHEMATA_DIR_MISSING, "non-existent").getMessage
   		server.isUp mustBe false
      }
      
      "return 503 in every http request after SCHEMATA_DIR_MISSING" in {

         server.isUp mustBe false

         assertResp(route(app, connectionRequest("schema")))
            .is(SERVICE_UNAVAILABLE)
            .withNoConnStatusHeader
            .withNoBody

         assertResp(route(app, connectedRequest(PUT, context + "/session", "cid")))
            .is(SERVICE_UNAVAILABLE)
            .withNoConnStatusHeader
            .withNoBody
      }
}

   "Schemata dir which is not a dir" should {
      
      "cause server to throw SCHEMATA_DIR_NOT_DIR" in {

         server.schemata.size mustBe 0
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         ex.getSeverity mustEqual FATAL
         ex.getMessage mustEqual new ServerException.User(SCHEMATA_DIR_NOT_DIR, "test-schemata-file").getMessage
   		server.isUp mustBe false
      }
      
      "return 503 in every http request after SCHEMATA_DIR_NOT_DIR" in {

         assertResp(route(app, FakeRequest(GET, context + "/session/foo")))
            .is(SERVICE_UNAVAILABLE)
            .withNoConnStatusHeader
            .withNoBody

   		server.isUp mustBe false
      }
   }

   "Multiple schemata with duplicate schema name" should {
      
      "cause server to throw SCHEMA_NAME_DUPE" in {

         val logTail = LogSniffer.last(3)
         server.schemata.size mustBe 1
         val errLine = logTail(0)
         errLine.severity mustBe Severity.ERROR
   		errLine.message must include (ServerErrorLocal.SCHEMA_CANNOT_REPLACE.asMessage("ParserCovariantOkayBigTestNoHooks", "schema1.json", "schema2.json"))
   		server.isUp mustBe true         
      }  
   }
}
