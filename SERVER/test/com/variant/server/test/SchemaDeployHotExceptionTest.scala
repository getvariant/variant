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
import com.variant.core.schema.parser.error.ParserError
import com.variant.core.schema.parser.error.SemanticError



/**
 * Test various schema deployment scenarios
 */
class SchemaDeployHotExceptionTest extends EmbeddedServerSpec with TempSchemataDir {
      
   private val logger = Logger(this.getClass)

   // Deploy bad schema alongside good schema for extra complexity.
   // They should be deployed in alphabetical order of file names.
   override def schemata = Set[String](
   	"schemata-test/petclinic.schema",
      "schemata-test-with-errors/monster-error.schema")
   
   /**
    *  
    */
   "Server" should {
 
 
	   "startup with no schemata due to a syntax error" in {
	      
	      server.schemata.size mustBe 1
	      	      
         val logLines = ServerLogTailer.last(8)
         // logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message mustBe ServerErrorLocal.SCHEMA_DEPLOYING.asMessage("/tmp/schemata/monster-error.schema")
         logLines(1).severity mustBe Severity.ERROR
         logLines(1).message must startWith (s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(2).severity mustBe Severity.WARN
         logLines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("?", "/tmp/schemata/monster-error.schema")

         // Let the directory watcher thread start before copying any files.
	      Thread.sleep(100)
	   }
      
	   "detect the touched file and still fail due to the same error" in {

         s"touch ${schemataDir}/monster-error.schema".!!	      	      	         
         
	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(TempSchemataDir.dirWatcherLatencyMsecs);

	      server.schemata.size mustBe 1

	      val logLines = ServerLogTailer.last(3)
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message mustBe ServerErrorLocal.SCHEMA_DEPLOYING.asMessage("/tmp/schemata/monster-error.schema")
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

         server.schemata.size mustBe 2
         val schId = server.schemata.getLiveGen("monstrosity").get.id
         val logLines = ServerLogTailer.last(1)
         //println(logLines)
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message mustBe ServerErrorLocal.SCHEMA_DEPLOYED.asMessage("monstrosity", "monster-error.schema");

	   }

	   "detect the updated petclinic and fail due to semantic error" in {
         
	   	(s"""grep -v 'name':'petclinic' schemata-test/petclinic.schema""" #> new File(s"${schemataDir}/petclinic.schema")) !!
         
	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(TempSchemataDir.dirWatcherLatencyMsecs);

         server.schemata.size mustBe 2
         val logLines = ServerLogTailer.last(3)
  	      //logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message mustBe ServerErrorLocal.SCHEMA_DEPLOYING.asMessage(s"${schemataDir}/petclinic.schema")
         logLines(1).severity mustBe Severity.ERROR
         logLines(1).message mustBe SemanticError.NAME_MISSING.asMessage()
         logLines(2).severity mustBe Severity.WARN
         logLines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("?", s"${schemataDir}/petclinic.schema")
	   }

	   "redeploy corrected petclinic" in {
         
         s"cp schemata-test/petclinic.schema ${schemataDir}" !!         
	      
         // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(TempSchemataDir.dirWatcherLatencyMsecs);

         server.schemata.size mustBe 2
         val schId = server.schemata.getLiveGen("monstrosity").get.id
         val logLines = ServerLogTailer.last(1)
  	      //logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message mustBe ServerErrorLocal.SCHEMA_DEPLOYED.asMessage("petclinic", "petclinic.schema")
	   }

	   "detect the updated petclinic and fail due to user defined class instntiation error" in {
         
         ("sed -E s/^[[:space:]]*'class':'com.variant.extapi.std.demo.UserQualifyingHook'.*$/'class':'com.variant.extapi.std.demo.BadClassName',/ schemata-test/petclinic.schema") #> 
         	new File("/tmp/petclinic.schema") !!;      	
         s"cp /tmp/petclinic.schema ${schemataDir}" !!
         
	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(TempSchemataDir.dirWatcherLatencyMsecs);

         server.schemata.size mustBe 2
         val logLines = ServerLogTailer.last(3)
  	      //logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.demo.BadClassName","java.lang.ClassNotFoundException")
         logLines(1).severity mustBe Severity.ERROR
         logLines(1).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.demo.BadClassName","java.lang.ClassNotFoundException")
         logLines(2).severity mustBe Severity.WARN
         logLines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("petclinic", s"${schemataDir}/petclinic.schema")
	   }

	   "redeploy corrected petclinic again" in {
         
         s"cp schemata-test/petclinic.schema ${schemataDir}" !!         
	      
         // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(TempSchemataDir.dirWatcherLatencyMsecs);

         server.schemata.size mustBe 2
         val schId = server.schemata.getLiveGen("monstrosity").get.id
         val logLines = ServerLogTailer.last(1)
  	      //logLines.foreach {l => println(s"********* [$l]") }
         logLines(0).severity mustBe Severity.INFO
         logLines(0).message mustBe ServerErrorLocal.SCHEMA_DEPLOYED.asMessage("petclinic", "petclinic.schema")
	   }

   }
}
