package com.variant.server.test

import scala.sys.process._
import com.variant.core.UserError.Severity
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
      "schemata-test-with-errors/big-conjoint-schema-error.json")
   
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

         s"touch ${schemataDir}/big-conjoint-schema-error.json".!!	      	      	         
         
	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(TempSchemataDir.dirWatcherLatencyMsecs);

	      server.schemata.size mustBe 0

	      val logLines = ServerLogTailer.last(3)
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message must startWith (s"Deploying schema from file [${schemataDir}/big-conjoint-schema-error.json]")
         logLines(1).severity mustBe Severity.ERROR
         logLines(1).message must startWith (s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(2).severity mustBe Severity.WARN
         logLines(2).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	   }

	   "detect the fixed file and deploy the schema" in {

	      // Fix the bad schema by adding the quite at the end of comment.
         ("sed -E s/^[[:space:]]*'comment'.*$/'comment':'fixed_comment'/ " + s"${schemataDir}/big-conjoint-schema-error.json") #> new File("/tmp/big-conjoint-schema-error.json") !!;      	
         s"cp /tmp/big-conjoint-schema-error.json ${schemataDir}" !!
         
	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(TempSchemataDir.dirWatcherLatencyMsecs);

         server.schemata.size mustBe 1
         val schId = server.schemata.getLiveGen("big_conjoint_schema").get.id
         val logLines = ServerLogTailer.last(1)
         println(logLines)
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message must startWith (s"Deployed schema [big_conjoint_schema] gen ID [${schId}], from [big-conjoint-schema-error.json]")

	   }

   }
}
