package com.variant.server.test

import org.scalatest.TestData

import com.variant.server.boot.VariantServer

import com.variant.server.test.spec.EmbeddedSpec

/**
 * Test various schema deployment scenarios
 */
class SchemaDeployColdTest extends EmbeddedSpec {
   /*
   implicit override def newAppForTest(testData: TestData): Application = {

      if (testData.name.startsWith("1.")) {
         // Just application property
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.loadConfig))
            .configure(
               Map("variant.schemata.dir" -> "schemata-test"))
            .build()
      } else if (testData.name.startsWith("2.")) {
         // Override with system property.
         sys.props.contains("variant.schemata.dir") mustBe false
         sys.props += ("variant.schemata.dir" -> "schemata-test-with-errors")
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.loadConfig))
            .build()
      } else {
         // All defaults.
         throw new RuntimeException("Dunno what to do")
      }

   }
*/
   "Schema should deploy from config property variant.schemata.dir" in {

      new ServerBuilder()
         .withConfig(Map("schemata.dir" -> "schemata-test"))
         .build()

      server.bootExceptions.size mustEqual 0
      server.schemata.size mustBe 3
      server.schemata.get("monstrosity").isDefined mustBe true
      server.schemata.get("monstrosity").get.liveGen.get.getMeta.getName mustEqual "monstrosity"
      server.schemata.get("monstrosity0").isDefined mustBe true
      server.schemata.get("monstrosity0").get.liveGen.get.getMeta.getName mustEqual "monstrosity0"
      server.schemata.get("petclinic").isDefined mustBe true
      server.schemata.get("petclinic").get.liveGen.get.getMeta.getName mustEqual "petclinic"
   }

   "Schema should deploy from system property variant.schemata.dir" in {

      new ServerBuilder().build()

      sys.props.contains("schemata.dir") mustBe false
      sys.props += ("variant.schemata.dir" -> "schemata-test-with-errors")

      server.bootExceptions.size mustEqual 0
      server.schemata.size mustBe 1
      server.schemata.get("monstrosity").isDefined mustBe true
      server.schemata.get("monstrosity").get.liveGen.get.getMeta.getName mustEqual "monstrosity"
   }

}
