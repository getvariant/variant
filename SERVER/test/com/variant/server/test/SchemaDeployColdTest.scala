package com.variant.server.test

import scala.util.Random
import org.scalatestplus.play._
import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.TraceEventReader
import com.variant.server.boot.SessionStore
import com.variant.server.boot.VariantServer
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.TestData
import org.apache.commons.io.FileUtils
import java.io.File
import play.api.Configuration
import com.variant.server.play.VariantApplicationLoader

/**
 * Test various schema deployment scenarios
 */
class SchemaDeployColdTest extends PlaySpec with OneAppPerTest {
   
   implicit override def newAppForTest(testData: TestData): Application = {
      
      if (testData.name.startsWith("1.")) {
         // Just application property
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(
               Map("variant.schemata.dir" -> "schemata-test")) 
            .build()
      }
      else if (testData.name.startsWith("2.")) {
         // Override with system property.
         sys.props.contains("variant.schemata.dir") must be (false)
         sys.props +=("variant.schemata.dir" -> "distr/schemata")  // only has petclinic_experiments schema.
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .build()
      }
      else {
         // All defaults.
         throw new RuntimeException("Dunno what to do")
      }

   }
 
   "1. Schema should deploy from config property variant.schemata.dir" in {
   
      val server = app.injector.instanceOf[VariantServer]
      server.startupErrorLog.size mustEqual 0
      server.schemata.size mustBe 2
      server.schemata.get("big_conjoint_schema").isDefined mustBe true
      server.schemata.get("big_conjoint_schema").get.liveGen.get.getMeta.getName mustEqual "big_conjoint_schema"
      server.schemata.get("petclinic_experiments").isDefined mustBe true
      server.schemata.get("petclinic_experiments").get.liveGen.get.getMeta.getName mustEqual "petclinic_experiments"
   }
   
   "2. Schema should deploy from system property variant.schemata.dir" in {
      
      val server = app.injector.instanceOf[VariantServer]
      server.startupErrorLog.size mustEqual 0
      server.schemata.size mustBe 2
      server.schemata.get("petclinic_experiments").isDefined mustBe true
      server.schemata.get("petclinic_experiments").get.liveGen.get.getMeta.getName mustEqual "petclinic_experiments"
      server.schemata.get("petclinic_toggles").isDefined mustBe true
      server.schemata.get("petclinic_toggles").get.liveGen.get.getMeta.getName mustEqual "petclinic_toggles"
   }

}
