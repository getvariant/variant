package com.variant.server.test

import scala.util.Random
import org.scalatestplus.play._
import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.EventReader
import com.variant.server.boot.SessionStore
import com.variant.server.boot.VariantServer
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.TestData
import org.apache.commons.io.FileUtils
import java.io.File
import play.api.Configuration
import com.variant.server.boot.VariantApplicationLoader

/**
 * Test various schema deployment scenarios
 */
class SchemaDeployColdTest extends PlaySpec with OneAppPerTest {
   
   implicit override def newAppForTest(testData: TestData): Application = {
      
      if (testData.name.startsWith("1.")) {
         sys.props +=("variant.ext.dir" -> "distr/ext")  // petclinic_experiments needs this.
         // Just application property
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(
               Map("variant.schemata.dir" -> "schemata-test")) 
            .build()
      }
      else if (testData.name.startsWith("2.")) {
         // Override with system property. In order for the distribution version of the petclinic_experiments schema
         // to parse, we need to give a custom variant.ext.dir location
         sys.props.contains("variant.schemata.dir") must be (false)
         sys.props +=("variant.schemata.dir" -> "distr/schemata")  // only has petclinic_experiments schema.
         sys.props +=("variant.ext.dir" -> "distr/ext") // petclinic_experiments needs this.
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
      server.schemata.get("big_conjoint_schema").get.liveGen.get.getName mustEqual "big_conjoint_schema"
      server.schemata.get("petclinic_experiments").isDefined mustBe true
      server.schemata.get("petclinic_experiments").get.liveGen.get.getName mustEqual "petclinic_experiments"
   }
   
   "2. Schema should deploy from system property variant.schemata.dir" in {
      
      val server = app.injector.instanceOf[VariantServer]
      server.startupErrorLog.size mustEqual 0
      server.schemata.size mustBe 1
      server.schemata.get("petclinic_experiments").isDefined mustBe true
      server.schemata.get("petclinic_experiments").get.liveGen.get.getName mustEqual "petclinic_experiments"
   }

}
