package com.variant.server.test

import com.variant.core.error.UserError.Severity
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.core.util.IoUtils
import com.variant.server.boot.ServerMessageLocal
import com.variant.server.boot.VariantServer
import com.variant.server.test.util.ServerLogTailer

import com.variant.server.test.spec.EmbeddedSpec
import org.scalatest.BeforeAndAfterAll
import java.nio.file.Paths

/**
 * Test various schema deployment scenarios
 */
class SchemaDeployEmptyTest extends EmbeddedSpec with BeforeAndAfterAll {

   private val schemataDir = "/tmp/schemata-test"
   private val dirWatcherLatencyMsecs = 15000 // takes this long for FS to notify the directory watcher service.
   // plus the logger seems to have noticeable lag.
   private val sessionTimeoutSecs = 15

   // Restart the server with the new schemata directory.
   IoUtils.emptyDir(schemataDir)
   new ServerBuilder()
      .withConfig(
         Map(
            "schemata.dir" -> schemataDir,
            "session.timeout" -> 15)) //
      .build()

   /**
    * Cleanup
    */
   override def afterAll() {
      IoUtils.delete(schemataDir)
      super.afterAll()
   }

   /**
    *
    */
   "File System Schema Deployer" should {

      "startup with empty schemata" in {
         server.schemata.size mustBe 0
         server.bootExceptions.size mustEqual 0
         val lastTwoLines = ServerLogTailer.last(2)
         lastTwoLines(0).level mustBe ServerLogTailer.Info
         lastTwoLines(0).message mustBe ServerMessageLocal.EMPTY_SCHEMATA.asMessage(schemataDir)
         lastTwoLines(1).level mustBe ServerLogTailer.Info
         lastTwoLines(1).message must startWith("[" + ServerMessageLocal.SERVER_BOOT_OK.getCode + "]")
      }

      "refuse deploy schema with errors" in {

         IoUtils.fileCopy("schemata-test-with-errors/monster-error.schema", s"${schemataDir}/monster-error.schema");

         // Sleep awhile to let WatcherService.take() have a chance to detect.
         Thread.sleep(dirWatcherLatencyMsecs);

         server.schemata.size mustBe 0

         val lastTwoLines = ServerLogTailer.last(2)
         lastTwoLines(0).level mustBe ServerLogTailer.Error
         lastTwoLines(0).message must startWith("[" + SyntaxError.JSON_SYNTAX_ERROR.getCode + "]")
         lastTwoLines(1).level mustBe ServerLogTailer.Warn
         lastTwoLines(1).message must startWith("[" + ServerMessageLocal.SCHEMA_FAILED.getCode + "]")

      }

      "process deletion of a faulty schema file" in {

         IoUtils.delete(s"${schemataDir}/monster-error.schema");
         Thread.sleep(dirWatcherLatencyMsecs);
         server.schemata.size mustBe 0

      }

      "deply a good schema" in {

         IoUtils.fileCopy("schemata-test/monster.schema", s"${schemataDir}/monster.schema");
         Thread.sleep(dirWatcherLatencyMsecs);
         server.schemata.size mustBe 1
      }

   }

}
