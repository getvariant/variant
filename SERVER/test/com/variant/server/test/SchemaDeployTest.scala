package com.variant.server.test

import scala.util.Random
import org.scalatestplus.play._
import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParamString
import com.variant.server.test.util.EventReader
import com.variant.server.conn.SessionStore
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
class SchemaDeployTest extends PlaySpec with OneAppPerTest {
   
   implicit override def newAppForTest(testData: TestData): Application = {
      
      if (testData.name.startsWith("1.")) {
         // Just application property
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(
               Map("variant.schemas.dir" -> "test-schemas")) 
            .build()
      }
      else if (testData.name.startsWith("2.")) {
         // Just system property
         FileUtils.copyFile(new File("conf-test/ParserCovariantOkayBigTest.json"), new File("/tmp/test-schemas-override/test-schema.json"))
         sys.props.contains("variant.schemas.dir") must be (false)
         sys.props +=(("variant.schemas.dir","/tmp/test-schemas-override"))
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .build()
      }
      else if (testData.name.startsWith("3.")) {
         // Both application and system property
         FileUtils.copyFile(new File("conf-test/ParserCovariantOkayBigTest.json"), new File("/tmp/test-schemas-override/test-schema.json"))
         sys.props -= ("variant.schemas.dir")
         sys.props.contains("variant.schemas.dir") must be (false)
         sys.props += (("variant.schemas.dir","/tmp/test-schemas-override"))
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .configure(
               Map("variant.schemas.dir" -> "test-schemas")) 
            .build()
      }
      else {
         // All defaults.
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .build()
      }

   }
 
   "1. Schema should deploy from config property variant.schemas.dir" in {
      val server = app.injector.instanceOf[VariantServer]
      server.isUp mustBe true
      server.schema.isDefined mustBe true
      server.startupErrorLog.size mustEqual 0
      server.schema.get.getName mustEqual "big_covar_schema"
   }
   
   "2. Schema should deploy from system property variant.schemas.dir" in {
      
      val server = app.injector.instanceOf[VariantServer]
      server.isUp mustBe true
      server.schema.isDefined mustBe true
      server.startupErrorLog.size mustEqual 0
      server.schema.get.getName mustEqual "parser_covariant_okay_big_test"
   }

   "3. Schema should deploy from system property variant.schemas.dir" in {
      
      val server = app.injector.instanceOf[VariantServer]
      server.isUp mustBe true
      server.schema.isDefined mustBe true
      server.startupErrorLog.size mustEqual 0
      server.schema.get.getName mustEqual "parser_covariant_okay_big_test"
   }

}
