package com.variant.server.test

import scala.sys.process._
import com.variant.core.error.UserError.Severity
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.server.boot.ServerMessageLocal
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.util.ServerLogTailer
import java.io.File
import com.variant.core.schema.parser.error.ParserError
import com.variant.core.schema.parser.error.SemanticError

/**
 * Test various schema deployment scenarios
 */
class SchemaDeployHotExceptionTest extends EmbeddedServerSpec with TempSchemataDir {

   // Deploy bad schema alongside good schema for extra complexity.
   // They should be deployed in alphabetical order of file names.
   override lazy val schemata = Set[String](
      "schemata/petclinic.schema",
      "schemata-errata/monster-error.schema")

   /**
    *
    */
   "Server" should {

      "startup with no schemata due to a syntax error" in {

         server.schemata.size mustBe 1

         val logLines = ServerLogTailer.last(7)
         //logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).message mustBe ServerMessageLocal.SCHEMA_DEPLOYING.asMessage("/tmp/schemata/monster-error.schema")
         logLines(1).message must startWith(s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(2).message mustBe ServerMessageLocal.SCHEMA_FAILED.asMessage("?", "/tmp/schemata/monster-error.schema")

         // Let the directory watcher thread start before copying any files.
         Thread.sleep(100)
      }

      "detect the touched file and still fail due to the same error" in {

         s"touch ${schemataDir}/monster-error.schema".!!

         // Sleep awhile to let WatcherService.take() have a chance to detect.
         Thread.sleep(dirWatcherLatencyMsecs);

         server.schemata.size mustBe 1

         val logLines = ServerLogTailer.last(3)
         logLines(0).message mustBe ServerMessageLocal.SCHEMA_DEPLOYING.asMessage("/tmp/schemata/monster-error.schema")
         logLines(1).message must startWith(s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(2).message must startWith(s"[${ServerMessageLocal.SCHEMA_FAILED.getCode}]")

      }

      "detect the fixed file and deploy the schema" in {

         // Fix the bad schema by adding the quite at the end of comment.
         ("sed -E s/^[[:space:]]*'comment'.*$/'comment':'fixed_comment'/ " + s"${schemataDir}/monster-error.schema") #> new File("/tmp/monster-error.schema") !!;
         s"mv /tmp/monster-error.schema ${schemataDir}" !!

         // Sleep awhile to let WatcherService.take() have a chance to detect.
         Thread.sleep(dirWatcherLatencyMsecs);

         server.schemata.size mustBe 2
         val schId = server.schemata.getLiveGen("monstrosity").get.id
         val logLines = ServerLogTailer.last(1)
         //println(logLines)
         logLines(0).message mustBe ServerMessageLocal.SCHEMA_DEPLOYED.asMessage("monstrosity", "monster-error.schema");

      }

      "detect the updated petclinic and fail due to parse error" in {

         (s"""grep -v 'name':'petclinic' schemata/petclinic.schema""" #> new File(s"${schemataDir}/petclinic.schema")) !!

         // Sleep awhile to let WatcherService.take() have a chance to detect.
         Thread.sleep(dirWatcherLatencyMsecs);

         server.schemata.size mustBe 2
         val logLines = ServerLogTailer.last(3)
         //logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).message mustBe ServerMessageLocal.SCHEMA_DEPLOYING.asMessage(s"${schemataDir}/petclinic.schema")
         logLines(1).message mustBe SemanticError.NAME_MISSING.asMessage()
         logLines(2).message mustBe ServerMessageLocal.SCHEMA_FAILED.asMessage("?", s"${schemataDir}/petclinic.schema")
      }

      "redeploy corrected petclinic" in {

         s"cp schemata/petclinic.schema ${schemataDir}" !!

         // Sleep awhile to let WatcherService.take() have a chance to detect.
         Thread.sleep(dirWatcherLatencyMsecs);

         server.schemata.size mustBe 2
         val schId = server.schemata.getLiveGen("monstrosity").get.id
         val logLines = ServerLogTailer.last(1)
         //logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).message mustBe ServerMessageLocal.SCHEMA_DEPLOYED.asMessage("petclinic", "petclinic.schema")
      }

      "detect the updated petclinic and fail due to user defined class instntiation error" in {

         ("sed -E s/^[[:space:]]*'class':'com.variant.extapi.std.demo.UserQualifyingHook'.*$/'class':'com.variant.extapi.std.demo.BadClassName',/ schemata/petclinic.schema") #>
            new File("/tmp/petclinic.schema") !!;
         s"cp /tmp/petclinic.schema ${schemataDir}" !!

         // Sleep awhile to let WatcherService.take() have a chance to detect.
         Thread.sleep(dirWatcherLatencyMsecs);

         server.schemata.size mustBe 2
         val logLines = ServerLogTailer.last(3)
         //logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).message mustBe ServerMessageLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.demo.BadClassName", "java.lang.ClassNotFoundException")
         logLines(1).message mustBe ServerMessageLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.demo.BadClassName", "java.lang.ClassNotFoundException")
         logLines(2).message mustBe ServerMessageLocal.SCHEMA_FAILED.asMessage("petclinic", s"${schemataDir}/petclinic.schema")
      }

      "redeploy corrected petclinic again" in {

         s"cp schemata/petclinic.schema ${schemataDir}" !!

         // Sleep awhile to let WatcherService.take() have a chance to detect.
         Thread.sleep(dirWatcherLatencyMsecs);

         server.schemata.size mustBe 2
         val schId = server.schemata.getLiveGen("monstrosity").get.id
         val logLines = ServerLogTailer.last(1)
         //logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).message mustBe ServerMessageLocal.SCHEMA_DEPLOYED.asMessage("petclinic", "petclinic.schema")
      }

   }
}
