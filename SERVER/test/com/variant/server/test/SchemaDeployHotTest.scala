package com.variant.server.test

import com.variant.core.impl.ServerError.SessionExpired
import com.variant.core.UserError.Severity
import com.variant.core.schema.parser.error.SemanticError
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.core.util.IoUtils
import com.variant.core.util.StringUtils
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.test.controller.SessionTest
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.spec.TempSchemataDir.dirWatcherLatencyMsecs
import com.variant.server.test.spec.TempSchemataDir.schemataDir
import com.variant.server.test.spec.TempSchemataDir.sessionTimeoutSecs
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.util.ServerLogTailer
import play.api.Logger
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.test.Helpers._
import play.api.test.Helpers.route
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import com.variant.server.schema.SchemaGen.State._



/**
 * Test various schema deployment scenarios
 */
class SchemaDeployHotTest extends BaseSpecWithServer with TempSchemataDir {
      
   private val logger = Logger(this.getClass)

   /**
    * 
    */
   "File System Schema Deployer" should {
 
	   "startup with two schemata" in {
	      
	      server.schemata.size mustBe 2
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
                  
         // Let the directory watcher thread start before copying any files.
	      Thread.sleep(100)
	   }
      
      "deploy a third schema" in {

	      IoUtils.fileCopy("schemata-test/big-conjoint-schema.json", s"${schemataDir}/another-big-test-schema.json");

	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(dirWatcherLatencyMsecs);
	      
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
         val bgsGen = server.schemata.get("big_conjoint_schema").get.liveGen.get
         petGen.getName mustEqual "big_conjoint_schema" 
	   }
	   
	   "replace petclinic_experiments from same origin" in {
	      
	      val currentGen = server.schemata.get("petclinic_experiments").get.liveGen.get
	      
         IoUtils.fileCopy("distr/schemata/petclinic-experiments.json", s"${schemataDir}/petclinic-experiments.json");
         Thread.sleep(dirWatcherLatencyMsecs)
    
         val newGen =  server.schemata.get("petclinic_experiments").get.liveGen.get
         newGen.getId mustNot be (currentGen.getId)
         currentGen.state mustBe Dead
         newGen.state mustBe Live
         
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
         val bgsGen = server.schemata.get("big_conjoint_schema").get.liveGen.get
         petGen.getName mustEqual "big_conjoint_schema" 

	   }

	   "refuse to re-deploy petclinic_experiments from different origin" in {
	      
	      val currentGen = server.schemata.get("petclinic_experiments").get.liveGen.get
	      
         IoUtils.fileCopy("distr/schemata/petclinic-experiments.json", s"${schemataDir}/petclinic-schema2.json")
         Thread.sleep(dirWatcherLatencyMsecs)
    
         val newGen =  server.schemata.get("petclinic_experiments").get.liveGen.get
         newGen.getId must be (currentGen.getId)
         newGen.state mustBe Live
         
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
         val bgsGen = server.schemata.get("big_conjoint_schema").get.liveGen.get
         petGen.getName mustEqual "big_conjoint_schema" 

         val logLines = ServerLogTailer.last(2)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${ServerErrorLocal.SCHEMA_CANNOT_REPLACE.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	   }

   	"re-deploy a schema with parse warnings" in {

	      val currentGen = server.schemata.get("petclinic_experiments").get.liveGen.get

	      IoUtils.fileCopy("schemata-test-with-errors/big-conjoint-schema-warning.json", s"${schemataDir}/another-big-test-schema.json")
         Thread.sleep(dirWatcherLatencyMsecs)
             
         val newGen =  server.schemata.get("petclinic_experiments").get.liveGen.get
         newGen.getId mustNot be (currentGen.getId)
         currentGen.state mustBe Dead
         newGen.state mustBe Live

	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
         val bgsGen = server.schemata.get("big_conjoint_schema").get.liveGen.get
         petGen.getName mustEqual "big_conjoint_schema" 

	   }

   	"undeploy deleted another-big-test-schema.json" in {
	      
	      val currentGen = server.schemata.get("big_conjoint_schemata").get.liveGen.get

	      IoUtils.delete(s"${schemataDir}/another-big-test-schema.json");
         Thread.sleep(dirWatcherLatencyMsecs)
    
         val newGen =  server.schemata.get("petclinic_experiments").get.liveGen.get
         newGen.getId must be (currentGen.getId)
         newGen.state mustBe Dead
         
	      server.schemata.size mustBe 2
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 

	   }

	   "refuse to deploy a schema with syntax errors" in {
	      
	      IoUtils.fileCopy("schemata-test-with-errors/big-conjoint-schema-error.json", s"${schemataDir}/another-big-test-schema.json")
         Thread.sleep(dirWatcherLatencyMsecs)
             
         val logLines = ServerLogTailer.last(2)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	      server.schemata.size mustBe 2
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
         val bgsGen = server.schemata.get("big_conjoint_schema").get.liveGen.get
         petGen.getName mustEqual "big_conjoint_schema" 

	   }


   	"refuse to re-deploy a schema with semantic errors" in {
	      
	      IoUtils.fileCopy("schemata-test-with-errors/petclinic-experiments.json", s"${schemataDir}/petclinic-schema2.json")
         Thread.sleep(dirWatcherLatencyMsecs)
             
         val logLines = ServerLogTailer.last(2)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${SemanticError.CONTROL_EXPERIENCE_MISSING.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
         val bgsGen = server.schemata.get("big_conjoint_schema").get.liveGen.get
         petGen.getName mustEqual "big_conjoint_schema" 

	   }
      
      "redeploy the third schema" in {

	      IoUtils.fileCopy("schemata-test/big-conjoint-schema.json", s"${schemataDir}/another-big-test-schema.json");

	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(dirWatcherLatencyMsecs)
	      
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
         val bgsGen = server.schemata.get("big_conjoint_schema").get.liveGen.get
         petGen.getName mustEqual "big_conjoint_schema" 

	   }
	   	   
	   val sessionJson = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
	   var sid = newSid()
	   
	   "create a new session in schema ParserConjointOkayBigTestNoHooks" in {
         
         assertResp(route(app, httpReq(POST, context + "/session")))
            .is(OK)
            .withNoBody
      }

	   "delete schema ParserConjointOkayBigTestNoHooks" in {

	      val currentGen = server.schemata.get("big_conjoint_schemata").get.liveGen.get

	      IoUtils.delete(s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
         Thread.sleep(dirWatcherLatencyMsecs)
    
         // Schema is gone
         currentGen.state mustBe Dead
         
         server.schemata.size mustBe 2
         server.schemata.get("ParserConjointOkayBigTestNoHooks") mustBe None
         
	   }


	   "permit session read over draining connection" in {
	      
         assertResp(route(app, httpReq(GET, context + "/session/" + sid)))
            .isOk
            .withBodyJson { json => 
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(sessionJson.expand("sid" -> sid).toString())
            }
	   }

	   "permit session update over draining connection" in {
	      
         val body = sessionJson.expand("sid" -> sid)
         assertResp(route(app, httpReq(PUT, context + "/session").withBody(body)))
            .isOk
            .withNoBody
	   }
	   
	   "expire existing session as normal in the undeployed schema" in {
         
         Thread.sleep(sessionTimeoutSecs * 1000);
         
         assertResp(route(app, httpReq(GET, context + "/session/" + sid)))
            .isError(SessionExpired, sid)

	   }
     	   
	   "confirm the 3 schemata" in {
	      
         IoUtils.fileCopy("conf-test/ParserConjointOkayBigTestNoHooks.json", s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
         IoUtils.fileCopy("distr/schemata/petclinic-experiments.json", s"${schemataDir}/petclinic-experiments.json");

         Thread.sleep(dirWatcherLatencyMsecs)
         
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
         val bgsGen = server.schemata.get("big_conjoint_schema").get.liveGen.get
         petGen.getName mustEqual "big_conjoint_schema" 
         
	   }

      val sessionJsonBigCovar = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
	   
	   val sid1 = newSid()
	   
	   "create a session in schema ParserConjointOkayBigTestNoHooks" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid1)
         assertResp(route(app, httpReq(PUT, context + "/session").withBody(body)))
            .isOk
            .withNoBody
      }

	   "redeploy schemata at once" in {
	      
         val currentGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get

	      // Override
	      IoUtils.fileCopy("conf-test/ParserConjointOkayBigTestNoHooks.json", s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
	      // New file
	      IoUtils.fileCopy("schemata-test/big-conjoint-schema.json", s"${schemataDir}/another-big-test-schema.json");

	      // While we wait for the FS system to notify directory watcher, make sure
	      // the existing session is kept alive
         val halfExp = sessionTimeoutSecs * 500
         for ( wait <- Seq(halfExp, halfExp, halfExp, halfExp) ) {
            Thread.sleep(wait)
            assertResp(route(app, httpReq(GET, context + "/session/" + sid1)))
               .isOk
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonBigCovar.expand("sid" -> sid1).toString())
               }
         }

         currentGen.state mustBe Dead

	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic_experiments").get.liveGen.get
         petGen.getName mustEqual "petclinic_experiments" 
         val bgsGen = server.schemata.get("big_conjoint_schema").get.liveGen.get
         petGen.getName mustEqual "big_conjoint_schema" 

	   }

	   val sid2 = newSid()
	   
      val sessionJsonPetclinic = ParameterizedString(SessionTest.sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))

	   "create new session in the unaffected schema petclinic_experiments" in {
         
         val body = sessionJsonPetclinic.expand("sid" -> sid2)
         assertResp(route(app, httpReq(PUT, context + "/session").withBody(body)))
            .isOk
            .withNoBody
      }

      val sid3 = newSid()
	   
	   "create new session in the new schema" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid3)
         assertResp(route(app, httpReq(PUT, context + "/session").withBody(body)))
            .isOk
            .withNoBody
      }
   }
}
