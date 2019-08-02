package com.variant.server.test

import java.io.File
import scala.reflect.io.Path
import scala.util.Try
import com.variant.server.impl.ConfigKeys
import com.variant.server.api.ServerException
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.boot.VariantServer
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.boot.ServerMessageLocal
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.boot.ServerExceptionLocal

/**
 * Test various schema deployment error scenarios
 */
class ServerBootExceptionTest extends EmbeddedServerSpec with ConfigKeys {

   /*
   /**
    * This will implicitly rebuild the server before each test.
    */
   implicit override def newAppForTest(testData: TestData): Application = {

      if (testData.name.contains("CONFIG_PROPERTY_NOT_SET")) {
         _app = new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config.withoutPath("variant.variant.schemata.dir")))
            .build()
         _app
      } else if (testData.name.contains("SCHEMATA_DIR_MISSING")) {
         _app = new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(Map("variant.variant.schemata.dir" -> "non-existent"))
            .build()
         _app
      } else if (testData.name.contains("SCHEMATA_DIR_NOT_DIR")) {
         _app = new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure("variant.variant.schemata.dir" -> "test-schemata-file")
            .build()
         _app
      } else if (testData.name.contains("SCHEMA_NAME_DUPE")) {
         // Delete directory
         val path: Path = Path("/tmp/schemata-test")
         Try(path.deleteRecursively())

         FileUtils.copyFile(new File("conf-test/ParserConjointOkayBigTestNoHooks.json"), new File("/tmp/schemata-test/schema1.json"))
         FileUtils.copyFile(new File("conf-test/ParserConjointOkayBigTestNoHooks.json"), new File("/tmp/schemata-test/schema2.json"))
         _app = new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure("variant.variant.schemata.dir" -> "/tmp/schemata-test")
            .build()
         _app
      } else {
         _app = new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .build()
         _app
      }
   }
*/

   "Server boot" should {

      "emit CONFIG_PROPERTY_NOT_SET if no schema.dir config" in {

         new ServerBuilder()
            .withoutKeys("variant.schemata.dir")
            .reboot()

         server.isUp mustBe false
         server.schemata.size mustBe 0
         server.bootExceptions.size mustEqual 1
         val ex = server.bootExceptions.head
         ex.getMessage mustEqual CONFIG_PROPERTY_NOT_SET.asMessage(SCHEMATA_DIR)
         server.isUp mustBe false
      }
   }

   "emit SCHEMATA_DIR_MISSING if schemata dir does not exist" in {

      new ServerBuilder()
         .withConfig(Map("variant.schemata.dir" -> "non-existent"))
         .reboot()

      server.isUp mustBe false
      server.schemata.size mustBe 0
      server.bootExceptions.size mustEqual 1
      val ex = server.bootExceptions.head
      ex mustEqual ServerExceptionLocal(SCHEMATA_DIR_MISSING, "non-existent")
      server.isUp mustBe false
   }
   /*
   "return 503 in every http request after SCHEMATA_DIR_MISSING" in {

      server.isUp mustBe false

      assertResp(route(app, httpReq(PUT, "/session/foo")))
         .is(SERVICE_UNAVAILABLE)
         .withNoBody

      assertResp(route(app, httpReq(GET, "/session/foo/bar")))
         .is(SERVICE_UNAVAILABLE)
         .withNoBody

      assertResp(route(app, httpReq(POST, "/session/foo/bar")))
         .is(SERVICE_UNAVAILABLE)
         .withNoBody
   }

   "Schemata dir which is not a dir" should {

      "cause server to throw SCHEMATA_DIR_NOT_DIR" in {

         server.schemata.size mustBe 0
         server.startupErrorLog.size mustEqual 1
         val ex = server.startupErrorLog.head
         ex.getSeverity mustEqual FATAL
         ex.getMessage mustEqual new ServerExceptionLocal(SCHEMATA_DIR_NOT_DIR, "test-schemata-file").getMessage
         server.isUp mustBe false
      }

      "return 503 in every http request after SCHEMATA_DIR_NOT_DIR" in {

         server.isUp mustBe false

         assertResp(route(app, httpReq(PUT, "/session/foo")))
            .is(SERVICE_UNAVAILABLE)
            .withNoBody

         assertResp(route(app, httpReq(GET, "/session/foo/bar")))
            .is(SERVICE_UNAVAILABLE)
            .withNoBody

         assertResp(route(app, httpReq(POST, "/session/foo/bar")))
            .is(SERVICE_UNAVAILABLE)
            .withNoBody

      }
   }

   "Multiple schemata with duplicate schema name" should {

      "cause server to throw SCHEMA_NAME_DUPE" in {

         val logTail = ServerLogTailer.last(3)
         server.schemata.size mustBe 1
         val errLine = logTail(0)
         errLine.severity mustBe ERROR
         errLine.message must include(SCHEMA_CANNOT_REPLACE.asMessage("ParserConjointOkayBigTestNoHooks", "schema1.json", "schema2.json"))
         server.isUp mustBe true
      }
   }
   *
   */
}
