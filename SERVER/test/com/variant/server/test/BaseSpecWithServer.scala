package com.variant.server.test

import scala.collection.JavaConversions._
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import javax.inject.Inject
import com.variant.server.conn.SessionStore
import org.scalatest.BeforeAndAfterAll
import com.variant.server.jdbc.JdbcService
import com.variant.server.event.EventWriter
import com.variant.server.event.EventWriter
import com.variant.server.boot.VariantServer
import com.variant.server.api.Session
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.core.schema.Schema
import java.util.Random
import com.variant.core.util.StringUtils
import com.typesafe.config.ConfigFactory
import java.io.File
import com.variant.server.boot.VariantApplicationLoader
import play.api.Mode
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import play.api.Configuration
import com.variant.server.conn.ConnectionStore
import play.api.libs.json.JsValue
import com.variant.core.ServerError
import com.variant.server.impl.SessionImpl
import com.variant.server.test.util.ParameterizedString
import org.apache.commons.io.IOUtils
import play.api.Logger
import com.variant.core.util.Constants

/**
 * Common to all tests.
 * Builds a custom application.
 */
object BaseSpecWithServer {
   private var sqlSchemaCreated = false
}

class BaseSpecWithServer extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {

   import BaseSpecWithServer._
   
   private val logger = Logger(this.getClass)
   
   // Custom application builder uses test specific config in front of the regular one.  
   implicit override lazy val app: Application = {
      logger.info("Rebuilding application...")

      // in case some other test set it.
      sys.props -= "variant.schemata.dir"
      // hook in the ext directory from the distribution dir to make the petclinic schema parse.
      sys.props +=("variant.ext.dir" -> "distr/ext")

      new GuiceApplicationBuilder()
         .configure(new Configuration(VariantApplicationLoader.config))
         .build()
   }

   /**
	 * @throws Exception 
	 * 
	 */
	private def recreateDatabase() {
	   // Assuming the first schema in schemata has the right event writer
		val jdbc = new JdbcService(server.schemata.head._2.eventWriter)
		try {			
			jdbc.getVendor match {
   			case JdbcService.Vendor.POSTGRES => {
   			   jdbc.recreateSchema()
   			   logger.info("Recreated PostgreSQL schema")
   			}
	   		case JdbcService.Vendor.H2 => 
	   		   jdbc.createSchema()
   			   logger.info("Recreated H2 schema")
		   }
		}
		catch {
		   case _: ClassCastException => 
		   case e: Throwable => throw e		
		}
	}

   protected val context = app.configuration.getString("play.http.context").get
   protected val server = app.injector.instanceOf[VariantServer]
   protected val connStore = app.injector.instanceOf[ConnectionStore]
   protected val ssnStore = app.injector.instanceOf[SessionStore]
   
   "Server must come up with a valid schema" in {
      
      // Print deployment errors, if any
      server.schemaDeployer.parserResponses.foreach { 
         _.getMessages.foreach(msg => {println("*** UNEXPECTED ***" + msg)})
      }
      
      //server.schemata.size mustBe 2 
      //server.schemaDeployer.parserResponses.size mustBe 2
      server.schemaDeployer.parserResponses.foreach { _.getMessages.size() mustBe 0 }
   }
   
    /**
	 * Each test case runs in its own JVM. Each test runs in its
	 * own instance of the test case. We want the jdbc schema
	 * created only once per jvm, but the api be instance scoped.
	 * 
	 * @throws Exception
	 * Needed by the JUnit EventWriter test which is currently off.
    */
   override def beforeAll() {
		synchronized { // once per JVM
			if (!sqlSchemaCreated) {
				recreateDatabase()
				sqlSchemaCreated = true
			}
		}
	}
   
   /**
    * Parse an 400 error body
    */
   protected def parseError(body: JsValue): (Boolean, ServerError, Seq[String]) = {
      body mustNot be (null)
      val isInternal = (body \ "isInternal").as[Boolean]
      val code = (body \ "code").as[Int] 
      val args = (body \ "args").as[Seq[String]]
      (isInternal, ServerError.byCode(code), args)
   }
   
   /**
    * Create and add a targeting stabile to a session.
    */
   protected def setTargetingStabile(ssn: Session, experiences: String*) {
		val stabile = new SessionScopedTargetingStabile()
		experiences.foreach {e => stabile.add(experience(e, ssn.getSchema))}
		ssn.asInstanceOf[SessionImpl].coreSession.setTargetingStabile(stabile);
	}

   /**
    * Find experience object by its comma separated name.
    */
   protected def experience(name: String, schema: Schema) = {
		val tokens = name.split("\\.")
		assert(tokens.length == 2)
		schema.getTest(tokens(0)).getExperience(tokens(1))
	}

   /**
    * Generate a new random session ID.
    */
   protected def newSid() = 
      StringUtils.random64BitString(new Random(System.currentTimeMillis()))
   
   /**
    * Normalize a JSON string by removing any white space.
    */
   protected def normalJson(jsonIn: String):String = Json.stringify(Json.parse(jsonIn))

   /**
    * All connect route calls should use this. This is the only request
    * that does not have connection id in X-Connection-ID header.
    */
   protected def connectionRequest(schemaName: String) = {
      FakeRequest(POST, context + "/connection/" + schemaName).withHeaders("Content-Type" -> "text/plain")
   }
   
   /**
    * All route calls, emulating a connected API call.
    */
   protected def connectedRequest(method: String, uri: String, connId: String) = {
      FakeRequest(method, uri)
         .withHeaders("Content-Type" -> "text/plain", Constants.HTTP_HEADER_CONNID -> connId)
   }

}
