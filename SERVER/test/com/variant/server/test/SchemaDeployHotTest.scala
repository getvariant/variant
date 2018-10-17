package com.variant.server.test

import com.variant.core.impl.ServerError.SESSION_EXPIRED
import com.variant.core.UserError.Severity
import com.variant.core.schema.parser.error.SemanticError
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.core.util.IoUtils
import com.variant.core.util.StringUtils
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.test.controller.SessionTest
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.spec.TempSchemataDir.dirWatcherLatencyMsecs
import com.variant.server.test.spec.TempSchemataDir.sessionTimeoutSecs
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.util.ServerLogTailer
import play.api.Logger
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test.Helpers.route
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import com.variant.server.schema.SchemaGen.State._



/**
 * Test various schema deployment scenarios
 */
class SchemaDeployHotTest extends EmbeddedServerSpec with TempSchemataDir {
      
   private val logger = Logger(this.getClass)

   /**
    * 
    */
   "File System Schema Deployer" should {
 
      val emptyTargetingTrackerBody = "{\"tt\":[]}"

	   "startup with two schemata" in {
	      
	      server.schemata.size mustBe 2
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 
                  
         // Let the directory watcher thread start before copying any files.
	      Thread.sleep(100)
	   }
      
      "deploy a third schema" in {

	      IoUtils.fileCopy("schemata-test/monster.schema", s"${schemataDir}/monster.schema");

	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(dirWatcherLatencyMsecs);
	      
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 
         val monstrGen = server.schemata.get("monstrosity").get.liveGen.get
         monstrGen.getMeta.getName mustEqual "monstrosity" 
	   }
	   
	   "replace petclinic from same origin" in {
	      
	      val currentGen = server.schemata.get("petclinic").get.liveGen.get
	      
         IoUtils.fileCopy("distr/schemata/petclinic.schema", s"${schemataDir}/petclinic.schema");
         Thread.sleep(dirWatcherLatencyMsecs)
    
         val newGen =  server.schemata.get("petclinic").get.liveGen.get
         newGen.id mustNot be (currentGen.id)
         currentGen.state mustBe Dead
         newGen.state mustBe Live
         
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 
         val monstrGen = server.schemata.get("monstrosity").get.liveGen.get
         monstrGen.getMeta.getName mustEqual "monstrosity" 

	   }

	   "refuse to re-deploy petclinic from different origin" in {
	      
	      val currentGen = server.schemata.get("petclinic").get.liveGen.get
	      
         IoUtils.fileCopy("distr/schemata/petclinic.schema", s"${schemataDir}/petclinic2.schema")
         Thread.sleep(dirWatcherLatencyMsecs)
    
         val newGen =  server.schemata.get("petclinic").get.liveGen.get
         newGen.id must be (currentGen.id)
         newGen.state mustBe Live
         
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 
         val monstrGen = server.schemata.get("monstrosity").get.liveGen.get
         monstrGen.getMeta.getName mustEqual "monstrosity" 

         val logLines = ServerLogTailer.last(2)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${ServerErrorLocal.SCHEMA_CANNOT_REPLACE.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	   }

   	"re-deploy a schema with parse warnings" in {

	      val currentGen = server.schemata.get("monstrosity").get.liveGen.get

	      IoUtils.fileCopy("schemata-test-with-errors/monster-warning.schema", s"${schemataDir}/monster.schema")
         Thread.sleep(dirWatcherLatencyMsecs)
             
         val newGen =  server.schemata.get("monstrosity").get.liveGen.get
         newGen.id mustNot be (currentGen.id)
         currentGen.state mustBe Dead
         newGen.state mustBe Live

	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 
         val bgsGen = server.schemata.get("monstrosity").get.liveGen.get
         bgsGen.getMeta.getName mustEqual "monstrosity" 
	   }

   	"undeploy deleted monster.schema" in {
	      
	      server.schemata.get("monstrosity").get.liveGen.isDefined

	      // Create a session to keep the schema from being vacuumed after undeployment.
	      val sid = newSid
         assertResp(route(app, httpReq(POST, context + "/session/monstrosity/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               ssn.getSchema().getMeta().getName mustBe "monstrosity"
         }

	      IoUtils.delete(s"${schemataDir}/monster.schema");
         Thread.sleep(dirWatcherLatencyMsecs)
             
         server.schemata.get("monstrosity").get.liveGen.isDefined mustBe false
         
	      server.schemata.size mustBe 2
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 
	   }

	   "refuse to deploy a schema with syntax errors" in {
	      
	      IoUtils.fileCopy("schemata-test-with-errors/monster-error.schema", s"${schemataDir}/monster.schema")
         Thread.sleep(dirWatcherLatencyMsecs)
             
         val logLines = ServerLogTailer.last(2)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	      server.schemata.size mustBe 2
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 

	   }

	   "ignore deletion of an orphan file unbounded to a live schema" in {
	      
	      IoUtils.delete(s"${schemataDir}/monster.schema")
         Thread.sleep(dirWatcherLatencyMsecs)
             
	      server.schemata.size mustBe 2
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 

	   }

	   "refuse to re-deploy a schema with semantic errors" in {
	      
	      IoUtils.fileCopy("schemata-test-with-errors/petclinic.schema", s"${schemataDir}/petclinic2.schema")
         Thread.sleep(dirWatcherLatencyMsecs)
             
         val logLines = ServerLogTailer.last(2)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${SemanticError.CONTROL_EXPERIENCE_MISSING.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	      server.schemata.size mustBe 2
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 

	   }
      
      "redeploy the third schema" in {

	      IoUtils.fileCopy("schemata-test/monster.schema", s"${schemataDir}/monster2.schema");

	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(dirWatcherLatencyMsecs)
	      
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 
         val monstrGen = server.schemata.get("monstrosity").get.liveGen.get
         monstrGen.getMeta.getName mustEqual "monstrosity" 

	   }
	   	   
	   //val sessionJson = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
	   var sid = newSid()
	   
	   "create session in the third schema" in {
	   
	      assertResp(route(app, httpReq(POST, context + "/session/ParserConjointOkayBigTestNoHooks/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk
            .withBodySession { ssn =>
               ssn.getId mustNot be (sid)
               sid = ssn.getId
               ssn.getSchema().getMeta().getName mustBe "ParserConjointOkayBigTestNoHooks"
         }

	   }

	   "delete schema file ParserConjointOkayBigTestNoHooks" in {

	      val currentGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get

	      IoUtils.delete(s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
         Thread.sleep(dirWatcherLatencyMsecs)
    
         // Schema gen should be vacuumed.
         currentGen.state mustBe Dead
         server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen mustBe None
                  
	   }

	   "permit session read over draining connection" in {
	      
         assertResp(route(app, httpReq(GET, context + "/session/ParserConjointOkayBigTestNoHooks/" + sid)))
            .isOk
            .withBodyJson(json => json mustNot be (null))
	   }

	   "permit session update over draining connection" in {
	      
         val body: JsValue = Json.obj(
            "sid" -> sid,
            "name" -> "foo",
            "value" -> "bar"
         )
         assertResp(route(app, httpReq(PUT, context + "/session/attr").withBody(body.toString())))
            .isOk
            .withBodySession  { ssn =>
               ssn.getAttributes.get("foo") mustBe null
            }
         server.ssnStore.get(sid).get.getAttributes.get("foo") mustBe "bar"
	   }
	   
	   "expire existing session as normal in the undeployed schema" in {
         
         Thread.sleep(sessionTimeoutSecs * 1000);
         
         assertResp(route(app, httpReq(GET, context + "/session/ParserConjointOkayBigTestNoHooks/" + sid)))
            .isError(SESSION_EXPIRED, sid)

	   }
     	   
	   "confirm the 2 schemata" in {
	      
         IoUtils.fileCopy("conf-test/ParserConjointOkayBigTestNoHooks.json", s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
         IoUtils.fileCopy("distr/schemata/petclinic.schema", s"${schemataDir}/petclinic.schema");

         Thread.sleep(dirWatcherLatencyMsecs)
         
	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 
         val bgsGen = server.schemata.get("monstrosity").get.liveGen.get
         bgsGen.getMeta.getName mustEqual "monstrosity" 
         
	   }

      val sessionJsonBigCovar = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
	   
	   val sid1 = newSid()
	   
	   "create a session in schema ParserConjointOkayBigTestNoHooks" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid1)
         assertResp(route(app, httpReq(PUT, context + "/session/ParserConjointOkayBigTestNoHooks").withBody(body)))
            .isOk
            .withNoBody
      }

	   "redeploy schemata at once" in {
	      
         val currentGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get

	      // Override
	      IoUtils.fileCopy("conf-test/ParserConjointOkayBigTestNoHooks.json", s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
	      // New file
	      IoUtils.fileCopy("schemata-test/monster.schema", s"${schemataDir}/another-monster.schema");

	      // While we wait for the FS system to notify directory watcher, make sure
	      // the existing session is kept alive
         val halfExp = sessionTimeoutSecs * 500
         for ( wait <- Seq(halfExp, halfExp, halfExp, halfExp) ) {
            Thread.sleep(wait)
            assertResp(route(app, httpReq(GET, context + "/session/ParserConjointOkayBigTestNoHooks/" + sid1)))
               .isOk
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonBigCovar.expand("sid" -> sid1).toString())
               }
         }

         currentGen.state mustBe Dead

	      server.schemata.size mustBe 3
	      val bigGen = server.schemata.get("ParserConjointOkayBigTestNoHooks").get.liveGen.get
         bigGen.getMeta.getName mustEqual "ParserConjointOkayBigTestNoHooks"
         val petGen = server.schemata.get("petclinic").get.liveGen.get
         petGen.getMeta.getName mustEqual "petclinic" 
         val monstrGen = server.schemata.get("monstrosity").get.liveGen.get
         monstrGen.getMeta.getName mustEqual "monstrosity" 

	   }
	   
	   "create new session in the unaffected schema petclinic" in {
         
         assertResp(route(app, httpReq(POST, context + "/session/petclinic/" + newSid).withBody(emptyTargetingTrackerBody)))
            .isOk
      }
	   
	   "create new session in the new schema" in {
         
         assertResp(route(app, httpReq(POST, context + "/session/monstrosity/" + newSid).withBody(emptyTargetingTrackerBody)))
            .isOk
      }
   }
}
