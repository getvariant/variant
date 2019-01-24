package com.variant.server.test

import org.scalatest.TestData
import org.scalatestplus.play.OneAppPerTest
import org.scalatestplus.play.PlaySpec

import com.variant.server.boot.VariantServer
import com.variant.server.play.VariantApplicationLoader

import play.api.Application
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder

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
         sys.props +=("variant.schemata.dir" -> "schemata-test-with-errors") 
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
      server.schemata.get("monstrosity").isDefined mustBe true
      server.schemata.get("monstrosity").get.liveGen.get.getMeta.getName mustEqual "monstrosity"
      server.schemata.get("petclinic").isDefined mustBe true
      server.schemata.get("petclinic").get.liveGen.get.getMeta.getName mustEqual "petclinic"
   }
   
   "2. Schema should deploy from system property variant.schemata.dir" in {
      
      val server = app.injector.instanceOf[VariantServer]
      server.startupErrorLog.size mustEqual 0
      server.schemata.size mustBe 1
      server.schemata.get("monstrosity").isDefined mustBe true
      server.schemata.get("monstrosity").get.liveGen.get.getMeta.getName mustEqual "monstrosity"
   }

}
