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
            
         val resp = route(app, connectionRequest("ParserCovariantOkayBigTestNoHooks")).get
         status(resp) mustBe OK
         val body = contentAsString(resp)
         body mustNot be (empty)
         val json = Json.parse(body)
         (json \ "id").asOpt[String].isDefined mustBe true
         cid = (json \ "id").as[String]
   	}
	   
	   val sessionJson = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
	   var sid = newSid()
	   
	   "create a new session in schema ParserCovariantOkayBigTestNoHooks" in {
         
         val body = sessionJson.expand("sid" -> sid)
         val resp = route(app, connectedRequest(PUT, context + "/session", cid).withBody(body)).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
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
	      
         val resp = route(app, connectedRequest(GET, context + "/session" + "/" + sid, cid)).get
         status(resp) mustBe OK
         val respAsJson = contentAsJson(resp)
         StringUtils.digest((respAsJson \ "session").as[String]) mustBe 
            StringUtils.digest(sessionJson.expand("sid" -> sid).toString())

	   }

	   "refuse to create a new session in the undeployed schema" in {
         
	      sid = newSid()
         val body = sessionJson.expand("sid" -> sid)
         val resp = route(app, connectedRequest(PUT, context + "/session", cid).withBody(body)).get
         status(resp) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(resp))
         isInternal mustBe false 
         error mustBe ServerError.UnknownConnection
         args mustBe Seq(cid)

      }
	   
	   "expire existing session as normal in the undeployed schema" in {
         
         Thread.sleep(sessionTimeoutSecs * 1000);    

         val resp = route(app, connectedRequest(GET, context + "/session" + "/" + sid, cid)).get
         status(resp) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(resp))
         isInternal mustBe false 
         error mustBe ServerError.SessionExpired
         args mustBe Seq(sid)

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
            
         val resp = route(app, connectionRequest("ParserCovariantOkayBigTestNoHooks")).get
         status(resp) mustBe OK
         val body = contentAsString(resp)
         body mustNot be (empty)
         val json = Json.parse(body)
         (json \ "id").asOpt[String].isDefined mustBe true
         cid1 = (json \ "id").as[String]
   	}
	   
      "open connection to petclinic" in {
            
         val resp = route(app, connectionRequest("petclinic")).get
         status(resp) mustBe OK
         val body = contentAsString(resp)
         body mustNot be (empty)
         val json = Json.parse(body)
         (json \ "id").asOpt[String].isDefined mustBe true
         cid2 = (json \ "id").as[String]
   	}

      val sessionJsonBigCovar = ParameterizedString(SessionTest.sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
	   
	   var sid1 = newSid()
	   
	   "create a session in schema ParserCovariantOkayBigTestNoHooks" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid1)
         val resp = route(app, connectedRequest(PUT, context + "/session", cid1).withBody(body)).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
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
            val resp = route(app, connectedRequest(GET, context + "/session" + "/" + sid1, cid1)).get
            status(resp) mustBe OK
            val respAsJson = contentAsJson(resp)
            StringUtils.digest((respAsJson \ "session").as[String]) mustBe 
               StringUtils.digest(sessionJsonBigCovar.expand("sid" -> sid1).toString())
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
	   
	   "refuse to create new session in the redeployed schema ParserCovariantOkayBigTestNoHooks" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid2)
         val resp = route(app, connectedRequest(PUT, context + "/session", cid1).withBody(body)).get
         status(resp) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(resp))
         isInternal mustBe false 
         error mustBe ServerError.UnknownConnection
         args mustBe Seq(cid1)
      }

      val sessionJsonPetclinic = ParameterizedString(SessionTest.sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))

	   "create new session in the unaffected schema petclinic" in {
         
         val body = sessionJsonPetclinic.expand("sid" -> sid2)
         val resp = route(app, connectedRequest(PUT, context + "/session", cid2).withBody(body)).get
         println(contentAsString(resp))
         status(resp) mustBe OK
      }

	   "open connection to the new big_covar_schema" in {
            
         val resp = route(app, connectionRequest("big_covar_schema")).get
         status(resp) mustBe OK
         val body = contentAsString(resp)
         body mustNot be (empty)
         val json = Json.parse(body)
         (json \ "id").asOpt[String].isDefined mustBe true
         cid3 = (json \ "id").as[String]
   	}

	   var sid3 = newSid()
	   
	   "create new session in the new schema" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> sid3)
         val resp = route(app, connectedRequest(PUT, context + "/session", cid3).withBody(body)).get
         status(resp) mustBe OK
      }

   }
   
}
