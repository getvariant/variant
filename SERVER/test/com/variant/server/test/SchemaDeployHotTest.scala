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
import org.apache.commons.io.FileUtils
import java.io.File
import play.api.Configuration
import com.variant.server.boot.VariantApplicationLoader
import java.nio.file.Files
import java.nio.file.Path
import com.variant.core.util.IoUtils
import org.scalatest.BeforeAndAfterAll
import play.api.Logger

/**
 * Test various schema deployment scenarios
 */
class SchemaDeployHotTest extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
      
   val tmpDir = "/tmp/test-schemata"
   
   private val logger = Logger(this.getClass)   

   // Custom application builder.  
   implicit override lazy val app: Application = {
      IoUtils.fileCopy("conf-test/ParserCovariantOkayBigTestNoHooks.json", s"${tmpDir}/big-test-schema.json");
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
   override def beforeAll() {
      IoUtils.delete(tmpDir)
      super.beforeAll();
   }

   "Server" should {
	   
	   "startup with two schemata" in {
	      
         val server = app.injector.instanceOf[VariantServer]
	      server.schemata.size mustBe 2
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
	   }
	   
	   "parse 3rd schema" in {
	      Thread.sleep(100)
         IoUtils.fileCopy("test-schemata/big-covar-schema.json", s"${tmpDir}/another-big-test-schema.json");

	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      // There is no way to force more frequent polling.
	      Thread.sleep(20000);
	   }
   }
   
}
