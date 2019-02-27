package com.variant.server.test

import scala.sys.process._
import com.variant.core.error.UserError.Severity
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.util.ServerLogTailer
import play.api.Logger
import java.io.File



/**
 * Test various schema deployment scenarios
 */
class SchemaDeployHotExceptionTest extends EmbeddedServerSpec with TempSchemataDir {
      
   private val logger = Logger(this.getClass)

   override def schemata = Set[String](
      "schemata-test-with-errors/monster-error.schema")
   
   /**
    *  
    */
   "Server" should {
 
      
	   "startup with no schemata due to a syntax error" in {
	      
	      server.schemata.size mustBe 0
	      	      
         val logLines = ServerLogTailer.last(3)
         println(logLines)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

         // Let the directory watcher thread start before copying any files.
	      Thread.sleep(100)
	   }
      
	   "detect the touched file and still fail due to the same error" in {

         s"touch ${schemataDir}/monster-error.schema".!!	      	      	         
         
	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(TempSchemataDir.dirWatcherLatencyMsecs);

	      server.schemata.size mustBe 0

	      val logLines = ServerLogTailer.last(3)
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message must startWith (s"Deploying schema from file [${schemataDir}/monster-error.schema]")
         logLines(1).severity mustBe Severity.ERROR
         logLines(1).message must startWith (s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(2).severity mustBe Severity.WARN
         logLines(2).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	   }

	   "detect the fixed file and deploy the schema" in {

	      // Fix the bad schema by adding the quite at the end of comment.
         ("sed -E s/^[[:space:]]*'comment'.*$/'comment':'fixed_comment'/ " + s"${schemataDir}/monster-error.schema") #> new File("/tmp/monster-error.schema") !!;      	
         s"cp /tmp/monster-error.schema ${schemataDir}" !!
         
	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(TempSchemataDir.dirWatcherLatencyMsecs);

         server.schemata.size mustBe 1
         val schId = server.schemata.getLiveGen("monstrosity").get.id
         val logLines = ServerLogTailer.last(1)
         println(logLines)
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message must startWith (s"[${ServerErrorLocal.SCHEMA_DEPLOYED.getCode}] Deployed schema [monstrosity] from [monster-error.schema]")

	   }

   }
}
