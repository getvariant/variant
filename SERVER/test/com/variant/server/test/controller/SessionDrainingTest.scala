package com.variant.server.test

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import com.variant.core.util.IoUtils
import com.variant.core.util.StringUtils
import com.variant.server.boot.VariantApplicationLoader
import com.variant.server.boot.VariantServer
import com.variant.server.schema.State
import com.variant.server.test.controller.SessionTest
import com.variant.server.test.util.ParameterizedString
import play.api.Application
import play.api.Configuration
import play.api.Logger
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.test.Helpers.contentAsJson
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.route
import play.api.test.Helpers.status
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import play.api.test.Helpers.writeableOf_AnyContentAsJson
import com.variant.core.ServerError
import com.variant.server.test.util.LogSniffer
import com.variant.core.UserError.Severity
import com.variant.server.boot.ServerErrorLocal
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.core.schema.parser.error.SemanticError
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.spec.TempSchemataDir._


/**
 * Test session drainage.
 */
class SessionDrainingTest extends BaseSpecWithServer with TempSchemataDir {
      
   private val logger = Logger(this.getClass)

   /**
    * 
    */
   "File System Schema Deployer" should {
 
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
   }
}
