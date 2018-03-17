package com.variant.server.test

import scala.util.Random
import org.scalatestplus.play._
import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.EventReader
import com.variant.server.conn.SessionStore
import com.variant.server.boot.VariantServer
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.TestData
import java.io.File
import play.api.Configuration
import com.variant.server.boot.VariantApplicationLoader
import com.variant.core.util.IoUtils
import org.scalatest.BeforeAndAfterAll
import play.api.Logger
import com.variant.server.schema.State
import scala.sys.process._
import com.variant.server.test.util.LogSniffer
import com.variant.core.UserError.Severity
import com.variant.server.boot.ServerErrorLocal
import com.variant.core.schema.parser.error.ParserError
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.core.schema.parser.error.SemanticError

/**
 * Test various schema deployment scenarios
 */
class SchemaDeployHotTest extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
      
   val tmpDir = "/tmp/test-schemata"
   
   private val logger = Logger(this.getClass)   
   private val server = app.injector.instanceOf[VariantServer]
   private val DirWatcherLatencyMsecs = 10000   // takes this long for the directory watcher service to be notified.
	
   // Custom application builder.  
   implicit override lazy val app: Application = {
      IoUtils.delete(tmpDir)
      IoUtils.fileCopy("conf-test/ParserCovariantOkayBigTestNoHooks.json", s"${tmpDir}/ParserCovariantOkayBigTestNoHooks.json");
      IoUtils.fileCopy("distr/schemata/petclinic-schema.json", s"${tmpDir}/petclinic-schema.json");
      sys.props +=("variant.schemata.dir" -> tmpDir)
      sys.props +=("variant.ext.dir" -> "distr/ext") // petclinic needs this.
      new GuiceApplicationBuilder()
         .configure(new Configuration(VariantApplicationLoader.config))
         .build()
   }

   /**
    * Cleanup
    */
   override def afterAll() {
      IoUtils.delete(tmpDir)
      super.afterAll();
   }

   "Schema deployer" should {
      
	   "startup with two schemata" in {
	      
	      server.schemata.size mustBe 2
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
                  
         // Let the directory watcher thread start before copying any files.
	      Thread.sleep(100)
	   }

	   "parse a third schema" in {

	      // Add a 3rd schema
	      IoUtils.fileCopy("test-schemata/big-covar-schema.json", s"${tmpDir}/another-big-test-schema.json");

	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(DirWatcherLatencyMsecs);
	      
	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      
	   }
	   
	   "replace petclinic from same origin" in {
	      
	      val currentSchema = server.schemata.get("petclinic").get
	      
         IoUtils.fileCopy("distr/schemata/petclinic-schema.json", s"${tmpDir}/petclinic-schema.json");
         Thread.sleep(DirWatcherLatencyMsecs)
    
         currentSchema.state mustBe State.Gone
         
	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").get.getId mustNot equal (currentSchema.getId())
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      

	   }

	   "refuse to re-deploy petclinic from different origin" in {
	      
	      val currentSchema = server.schemata.get("petclinic").get
	      
         IoUtils.fileCopy("distr/schemata/petclinic-schema.json", s"${tmpDir}/petclinic-schema2.json")
         Thread.sleep(DirWatcherLatencyMsecs)
    
         currentSchema.state mustBe State.Deployed
         
	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").get.getId must equal (currentSchema.getId())
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      

         val logLines = LogSniffer.last(2)
         logLines(0).severity mustBe Severity.WARN
         logLines(0).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")
         logLines(1).severity mustBe Severity.ERROR
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_CANNOT_REPLACE.getCode}]")

	   }

   	"re-deploy a schema with parse warnings" in {

   	   val currentSchema = server.schemata.get("big_covar_schema").get
         currentSchema.state mustBe State.Deployed

	      IoUtils.fileCopy("test-schemata-with-errors/big-covar-schema-warning.json", s"${tmpDir}/another-big-test-schema.json")
         Thread.sleep(DirWatcherLatencyMsecs)
             
         currentSchema.state mustBe State.Gone

	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getId mustNot equal (currentSchema.getId)
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      

	   }

   	"undeploy deleted another-big-test-schema.json" in {
	      
	      val currentSchema = server.schemata.get("big_covar_schema").get
         currentSchema.state mustBe State.Deployed

	      IoUtils.delete(s"${tmpDir}/another-big-test-schema.json");
         Thread.sleep(DirWatcherLatencyMsecs)
    
         currentSchema.state mustBe State.Gone
         
	      server.schemata.size mustBe 2
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed

	   }

	   "refuse to deploy a schema with syntax errors" in {
	      
	      IoUtils.fileCopy("test-schemata-with-errors/big-covar-schema-error.json", s"${tmpDir}/another-big-test-schema.json")
         Thread.sleep(DirWatcherLatencyMsecs)
             
         val logLines = LogSniffer.last(2)
         logLines(0).severity mustBe Severity.WARN
         logLines(0).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")
         logLines(1).severity mustBe Severity.ERROR
         logLines(1).message must startWith (s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")

	      server.schemata.size mustBe 2
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed

	   }


   	"refuse to re-deploy a schema with semantic errors" in {
	      
	      IoUtils.fileCopy("test-schemata-with-errors/petclinic-schema.json", s"${tmpDir}/petclinic-schema2.json")
         Thread.sleep(DirWatcherLatencyMsecs)
             
         val logLines = LogSniffer.last(2)
         logLines(0).severity mustBe Severity.WARN
         logLines(0).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")
         logLines(1).severity mustBe Severity.ERROR
         logLines(1).message must startWith (s"[${SemanticError.CONTROL_EXPERIENCE_MISSING.getCode}]")

	      server.schemata.size mustBe 2
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed

	   }

   }
   
}
