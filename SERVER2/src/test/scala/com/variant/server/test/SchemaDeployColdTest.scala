package com.variant.server.test

import org.scalatest.TestData

import com.variant.server.boot.VariantServer

import com.variant.server.test.spec.EmbeddedSpec

/**
 * Test various schema deployment scenarios.
 */
class SchemaDeployColdTest extends EmbeddedSpec {

   "Server should come up with no schemata" in {
      server.bootExceptions.size mustEqual 0
      server.schemata.size mustBe 0
   }

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

      sys.props.contains("schemata.dir") mustBe false
      sys.props += ("schemata.dir" -> "schemata-test-with-errors")

      new ServerBuilder().build()

      server.bootExceptions.size mustEqual 0
      server.schemata.size mustBe 1
      server.schemata.get("monstrosity").isDefined mustBe true
      server.schemata.get("monstrosity").get.liveGen.get.getMeta.getName mustEqual "monstrosity"
   }

}
