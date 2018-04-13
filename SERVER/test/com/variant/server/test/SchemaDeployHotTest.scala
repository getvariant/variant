package com.variant.server.test

import com.variant.core.ServerError._
import com.variant.core.UserError.Severity
import com.variant.core.schema.parser.error.SemanticError
import com.variant.core.schema.parser.error.SyntaxError
import com.variant.core.util.IoUtils
import com.variant.core.util.StringUtils
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.schema.State
import com.variant.server.test.controller.SessionTest
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.spec.TempSchemataDir.dirWatcherLatencyMsecs
import com.variant.server.test.spec.TempSchemataDir.schemataDir
import com.variant.server.test.spec.TempSchemataDir.sessionTimeoutSecs
import com.variant.server.test.util.LogSniffer
import com.variant.server.test.util.ParameterizedString
import play.api.Logger
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Json
import play.api.test.Helpers.BAD_REQUEST
import play.api.test.Helpers.GET
import play.api.test.Helpers.OK
import play.api.test.Helpers.PUT
import play.api.test.Helpers.contentAsJson
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.route
import play.api.test.Helpers.status
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import com.variant.core.ConnectionStatus._



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
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
                  
         // Let the directory watcher thread start before copying any files.
	      Thread.sleep(100)
	   }
      
      "deploy a third schema" in {

	      IoUtils.fileCopy("schemata-test/big-covar-schema.json", s"${schemataDir}/another-big-test-schema.json");

	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(dirWatcherLatencyMsecs);
	      
	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      
	   }
	   
	   "replace petclinic from same origin" in {
	      
	      val currentSchema = server.schemata.get("petclinic").get
	      
         IoUtils.fileCopy("distr/schemata/petclinic-schema.json", s"${schemataDir}/petclinic-schema.json");
         Thread.sleep(dirWatcherLatencyMsecs)
    
         currentSchema.state mustBe State.Gone
         
	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").get.getId mustNot equal (currentSchema.getId())
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      

	   }

	   "refuse to re-deploy petclinic from different origin" in {
	      
	      val currentSchema = server.schemata.get("petclinic").get
	      
         IoUtils.fileCopy("distr/schemata/petclinic-schema.json", s"${schemataDir}/petclinic-schema2.json")
         Thread.sleep(dirWatcherLatencyMsecs)
    
         currentSchema.state mustBe State.Deployed
         
	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").get.getId must equal (currentSchema.getId())
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      

         val logLines = LogSniffer.last(2)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${ServerErrorLocal.SCHEMA_CANNOT_REPLACE.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	   }

   	"re-deploy a schema with parse warnings" in {

   	   val currentSchema = server.schemata.get("big_covar_schema").get
         currentSchema.state mustBe State.Deployed

	      IoUtils.fileCopy("schemata-test-with-errors/big-covar-schema-warning.json", s"${schemataDir}/another-big-test-schema.json")
         Thread.sleep(dirWatcherLatencyMsecs)
             
         currentSchema.state mustBe State.Gone

	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getId mustNot equal (currentSchema.getId)
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      

	   }

   	"undeploy deleted another-big-test-schema.json" in {
	      
	      val currentSchema = server.schemata.get("big_covar_schema").get
         currentSchema.state mustBe State.Deployed

	      IoUtils.delete(s"${schemataDir}/another-big-test-schema.json");
         Thread.sleep(dirWatcherLatencyMsecs)
    
         currentSchema.state mustBe State.Gone
         
	      server.schemata.size mustBe 2
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed

	   }

	   "refuse to deploy a schema with syntax errors" in {
	      
	      IoUtils.fileCopy("schemata-test-with-errors/big-covar-schema-error.json", s"${schemataDir}/another-big-test-schema.json")
         Thread.sleep(dirWatcherLatencyMsecs)
             
         val logLines = LogSniffer.last(2)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${SyntaxError.JSON_SYNTAX_ERROR.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	      server.schemata.size mustBe 2
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed

	   }


   	"refuse to re-deploy a schema with semantic errors" in {
	      
	      IoUtils.fileCopy("schemata-test-with-errors/petclinic-schema.json", s"${schemataDir}/petclinic-schema2.json")
         Thread.sleep(dirWatcherLatencyMsecs)
             
         val logLines = LogSniffer.last(2)
         logLines(0).severity mustBe Severity.ERROR
         logLines(0).message must startWith (s"[${SemanticError.CONTROL_EXPERIENCE_MISSING.getCode}]")
         logLines(1).severity mustBe Severity.WARN
         logLines(1).message must startWith (s"[${ServerErrorLocal.SCHEMA_FAILED.getCode}]")

	      server.schemata.size mustBe 2
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed

	   }
      
      "redeploy the third schema" in {

	      IoUtils.fileCopy("schemata-test/big-covar-schema.json", s"${schemataDir}/another-big-test-schema.json");

	      // Sleep awhile to let WatcherService.take() have a chance to detect.
	      Thread.sleep(dirWatcherLatencyMsecs)
	      
	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      
	   }

      var cid: String = null
	   
      "open connection to ParserCovariantOkayBigTestNoHooks" in {
            
         assertResp(route(app, connectionRequest("ParserCovariantOkayBigTestNoHooks")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               (json \ "id").asOpt[String].isDefined mustBe true
               cid = (json \ "id").as[String]
            }
   	}
	   
	   val sessionJson = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
	   var sid = newSid()
	   
	   "create a new session in schema ParserCovariantOkayBigTestNoHooks" in {
         
         val body = sessionJson.expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid).withBody(body)))
            .is(OK)
            .withConnStatusHeader(OPEN)
            .withNoBody
      }

	   "delete schema ParserCovariantOkayBigTestNoHooks" in {

	      val currentSchema = server.schemata.get("ParserCovariantOkayBigTestNoHooks").get
         currentSchema.state mustBe State.Deployed

	      IoUtils.delete(s"${schemataDir}/ParserCovariantOkayBigTestNoHooks.json");
         Thread.sleep(dirWatcherLatencyMsecs)
    
         // Schema is gone
         currentSchema.state mustBe State.Gone
         server.schemata.get("ParserCovariantOkayBigTestNoHooks") mustBe None
         
	   }


	   "keep existing session alive" in {
	      
         assertResp(route(app, connectedRequest(GET, context + "/session" + "/" + sid, cid)))
            .isOk
            .withConnStatusHeader(CLOSED_BY_SERVER)
            .withBodyJson { json => 
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(sessionJson.expand("sid" -> sid).toString())
            }

	   }

	   "refuse to create a new session in the undeployed schema" in {
         
	      sid = newSid()
         val body = sessionJson.expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid).withBody(body)))
            .isError(UnknownConnection, cid)
            .withConnStatusHeader(CLOSED_BY_SERVER)
      }
	   
	   "expire existing session as normal in the undeployed schema" in {
         
         Thread.sleep(sessionTimeoutSecs * 1000);    
         assertResp(route(app, connectedRequest(GET, context + "/session" + "/" + sid, cid)))
            .isError(SessionExpired, sid)
            .withConnStatusHeader(OPEN)

	   }
     	   
	   "confirm the 3 schemata" in {
	      
         IoUtils.fileCopy("conf-test/ParserCovariantOkayBigTestNoHooks.json", s"${schemataDir}/ParserCovariantOkayBigTestNoHooks.json");
         IoUtils.fileCopy("distr/schemata/petclinic-schema.json", s"${schemataDir}/petclinic-schema.json");

         Thread.sleep(dirWatcherLatencyMsecs)
         
	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      
         
	   }

      var cid1, cid2, cid3: String = null
	   
      "open new connection to ParserCovariantOkayBigTestNoHooks" in {
            
         assertResp(route(app, connectionRequest("ParserCovariantOkayBigTestNoHooks")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               (json \ "id").asOpt[String].isDefined mustBe true
               cid1 = (json \ "id").as[String]
         }
   	}
	   
      "open connection to petclinic" in {
            
         assertResp(route(app, connectionRequest("petclinic")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               (json \ "id").asOpt[String].isDefined mustBe true
               cid2 = (json \ "id").as[String]
            }
   	}

      val sessionJsonBigCovar = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
	   
	   var sid1 = newSid()
	   
	   "create a session in schema ParserCovariantOkayBigTestNoHooks" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid1)
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid1).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
      }

	   "redeploy schemata at once" in {
	      
	      val oldBigSchema = server.schemata.get("ParserCovariantOkayBigTestNoHooks").get
         oldBigSchema.state mustBe State.Deployed

	      // Override
	      IoUtils.fileCopy("conf-test/ParserCovariantOkayBigTestNoHooks.json", s"${schemataDir}/ParserCovariantOkayBigTestNoHooks.json");
	      // New file
	      IoUtils.fileCopy("schemata-test/big-covar-schema.json", s"${schemataDir}/another-big-test-schema.json");

	      // While we wait for the FS system to notify directory watcher, make sure
	      // the existing session is kept alive
         val halfExp = sessionTimeoutSecs * 500
         for ( wait <- Seq(halfExp, halfExp, halfExp, halfExp) ) {
            Thread.sleep(wait)
            assertResp(route(app, connectedRequest(GET, context + "/session" + "/" + sid1, cid1)))
               .isOk
               .withConnStatusHeader(OPEN)
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonBigCovar.expand("sid" -> sid1).toString())
               }
         }

         oldBigSchema.state mustBe State.Gone

	      server.schemata.size mustBe 3
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
	      server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getId() mustNot equal (oldBigSchema.getId())
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.getName mustEqual "ParserCovariantOkayBigTestNoHooks"
         server.schemata.get("ParserCovariantOkayBigTestNoHooks").get.state mustEqual State.Deployed
         server.schemata.get("petclinic").isDefined mustBe true
         server.schemata.get("petclinic").get.getName mustEqual "petclinic" 
         server.schemata.get("petclinic").get.state mustEqual State.Deployed
         server.schemata.get("big_covar_schema").isDefined mustBe true
         server.schemata.get("big_covar_schema").get.getName mustEqual "big_covar_schema" 
         server.schemata.get("big_covar_schema").get.state mustEqual State.Deployed 	      
	   }

	   var sid2 = newSid()
	   
	   "refuse to create new session in the draining connection to ParserCovariantOkayBigTestNoHooks" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid2)
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid1).withBody(body)))
            .isError(UnknownConnection, cid1)
            .withConnStatusHeader(CLOSED_BY_SERVER)
      }

      val sessionJsonPetclinic = ParameterizedString(SessionTest.sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))

	   "create new session in the unaffected schema petclinic" in {
         
         val body = sessionJsonPetclinic.expand("sid" -> sid2)
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid2).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
      }

	   "open connection to the new big_covar_schema" in {
            
         assertResp(route(app, connectionRequest("big_covar_schema")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               (json \ "id").asOpt[String].isDefined mustBe true
               cid3 = (json \ "id").as[String]
            }
   	}

      var sid3 = newSid()
	   
	   "create new session in the new schema" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid3)
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid3).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
      }
   }
}
