package com.variant.server.test

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec

import com.variant.core.UserError.Severity
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.core.util.IoUtils
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.boot.VariantApplicationLoader
import com.variant.server.boot.VariantServer
import com.variant.server.test.util.ServerLogTailer

import play.api.Application
import play.api.Configuration
import play.api.Logger
import play.api.inject.guice.GuiceApplicationBuilder


object SchemaDeployEmptyTest {
   val sessionTimeoutSecs = 15
   val schemataDir = "/tmp/schemata-test"  
   val rand = new java.util.Random()
  
}


/**
 * Test various schema deployment scenarios
 */
class SchemaDeployEmptyTest extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
      
   import SchemaDeployEmptyTest._
   
   private val logger = Logger(this.getClass)
   private val dirWatcherLatencyMsecs = 10000   // takes this long for FS to notify the directory watcher service.

   // Custom application builder.  
   implicit override lazy val app: Application = {
      IoUtils.emptyDir(schemataDir)
      sys.props +=("variant.ext.dir" -> "distr/ext")
      new GuiceApplicationBuilder()
         .configure(new Configuration(VariantApplicationLoader.config))
         .configure("variant.schemata.dir" -> schemataDir)
         .configure("variant.session.timeout" -> sessionTimeoutSecs)
         .build()
   }

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
 
      val server = app.injector.instanceOf[VariantServer]
            
	   "startup with empty schemata" in {
	      server.schemata mustBe empty
         server.startupErrorLog.size mustEqual 0
         val lastTwoLines = ServerLogTailer.last(2)
         lastTwoLines(0).severity mustBe Severity.INFO
         lastTwoLines(0).message must startWith("[" + ServerErrorLocal.EMPTY_SCHEMATA.getCode + "]")
         lastTwoLines(1).severity mustBe Severity.INFO
         lastTwoLines(1).message must  startWith("[" + ServerErrorLocal.SERVER_BOOT_OK.getCode + "]")
	   }

	   "refuse deploy schema with errors" in {

         IoUtils.fileCopy("schemata-test-with-errors/big-conjoint-schema-error.json", s"${schemataDir}/big-conjoint-schema-error.json");

	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(dirWatcherLatencyMsecs);

         server.schemata.size mustBe 0

         val lastTwoLines = ServerLogTailer.last(2, "logs/application.log")
         lastTwoLines(0).severity mustBe Severity.ERROR
         lastTwoLines(0).message must startWith("[" + SyntaxError.JSON_SYNTAX_ERROR.getCode + "]")
         lastTwoLines(1).severity mustBe Severity.WARN
         lastTwoLines(1).message must  startWith("[" + ServerErrorLocal.SCHEMA_FAILED.getCode + "]")

	   }
	   
	   "process deletion of a faulty schema file" in {

         IoUtils.delete( s"${schemataDir}/big-conjoint-schema-error.json");
         Thread.sleep(dirWatcherLatencyMsecs);
         server.schemata.size mustBe 0

	   }

	   "deply a good schema" in {

         IoUtils.fileCopy("conf-test/ParserConjointOkayBigTestNoHooks.json", s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
	      Thread.sleep(dirWatcherLatencyMsecs);
         server.schemata.size mustBe 1
	   }

   }
   
}
