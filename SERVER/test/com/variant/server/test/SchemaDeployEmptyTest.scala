package com.variant.server.test

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.OneAppPerTest
import org.scalatestplus.play.PlaySpec
import com.variant.core.util.IoUtils
import com.variant.server.boot.VariantApplicationLoader
import play.api.Application
import play.api.Configuration
import play.api.Logger
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.Helpers._
import play.api.test.Helpers.contentAsJson
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.route
import play.api.test.Helpers.status
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import play.api.test.Helpers.writeableOf_AnyContentAsJson
import com.variant.server.boot.VariantServer
import com.variant.server.test.util.LogSniffer
import com.variant.core.UserError.Severity
import com.variant.server.boot.ServerErrorLocal


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
         val lastTwoLines = LogSniffer.last(2)
         lastTwoLines(0).severity mustBe Severity.INFO
         lastTwoLines(0).message must startWith("[" + ServerErrorLocal.EMPTY_SCHEMATA.getCode + "]")
         lastTwoLines(1).severity mustBe Severity.INFO
         lastTwoLines(1).message must  startWith("[" + ServerErrorLocal.SERVER_BOOT_OK.getCode + "]")
	   }

	   "startup with two schemata" in {
	    /*
         server.schemata.size mustBe 2
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema"
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic"
*/
	   }
   }
   
}
