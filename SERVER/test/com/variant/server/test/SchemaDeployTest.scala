package com.variant.server.test

import scala.util.Random
import org.scalatestplus.play._
import play.api.Application
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.UserError
import com.variant.server.test.util.ParamString
import com.variant.server.ServerPropertiesKey._
import com.variant.server.test.util.EventReader
import com.variant.server.ServerPropertiesKey._
import com.variant.server.session.SessionStore
import com.variant.server.boot.VariantServer
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.TestData
import com.variant.core.exception.Error
import com.variant.server.ServerErrorException
import com.variant.server.ServerError
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Test various schema deployment scenarios
 */
class SchemaDeployTest extends PlaySpec with OneAppPerTest {
   
   implicit override def newAppForTest(testData: TestData): Application = {
      new GuiceApplicationBuilder().build()
   }
 
   "Schema should deploy from config property variant.schemas.dir" in {
      val server = app.injector.instanceOf[VariantServer]
      server.isUp mustBe true
      server.schema.isDefined mustBe true
      server.startupErrorLog.size mustEqual 0
      server.schema.get.name mustEqual "big-covar-schema"
      
   }
/*
   "System property variant.schemas.dir" should {

      FileUtils.copyFile(new File("test-schemas/big-covar-schema.json"), new File("/tmp/test-schemas-override/test-schema.json"))
      sys.props.contains("variant.schemas.dir") must be (false)
      sys.props.+=(("variant.schemas.dir","/tmp/test-schemas-override"))
      
      "override the config property by the same name" in {
         val server = app.injector.instanceOf[VariantServer]
         server.isUp mustBe true
         server.schema.isDefined mustBe true
         server.startupErrorLog.size mustEqual 0

      }
      
   }
*/ 
}
