package com.variant.server.test.spec

import scala.collection.JavaConversions.asScalaBuffer

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.OneAppPerSuite

import com.variant.core.UserError.Severity
import com.variant.server.play.VariantApplicationLoader
import com.variant.server.test.util.JdbcService

import play.api.Application
import play.api.Configuration
import play.api.Logger
import play.api.inject.guice.GuiceApplicationBuilder

/**
 * All tests using an embedded server inherint from here.
 */
object EmbeddedServerSpec {
   private var sqlSchemaCreated = false
}

class EmbeddedServerSpec extends BaseServerSpec with OneAppPerSuite with BeforeAndAfterAll {

   import EmbeddedServerSpec._
   
   private val logger = Logger(this.getClass)
   
   // Custom application builder uses test specific config in front of the regular one.  
   implicit override lazy val app: Application = {
      
      logger.info("Rebuilding application...")

      // in case some other test set it.
      sys.props -= "variant.schemata.dir"

      new GuiceApplicationBuilder()
         .configure(new Configuration(VariantApplicationLoader.config))
         .build()
   }

   /**
	 * @throws Exception 
	 * 
	 */
	private def recreateDatabase() {
	   // Assuming the there's always the petclinic_experiments schema and that it has the right event writer.
	   server.schemata.getLiveGen("petclinic") match {
	      
	      case Some(schema) =>
      		val jdbc = new JdbcService(schema.eventWriter)
      		try {			
      			jdbc.getVendor match {
         			case JdbcService.Vendor.POSTGRES =>
         			   jdbc.recreateSchema()
         			   logger.info("Recreated PostgreSQL schema")
         			
      	   		case JdbcService.Vendor.H2 => 
      	   		   jdbc.createSchema()
         			   logger.info("Recreated H2 schema")
         			   
      		   }

      		}
      		catch {
      		   case _: ClassCastException => 
      		   case e: Throwable => throw e		
      		}
      		
	      case None => println(" *** Running withut a database *** ")
	   }
	}
	
	override protected def application = app
   
   // Print deployment errors, if any
   server.schemaDeployer.parserResponses.foreach { resp =>
	   if (resp.hasMessages(Severity.ERROR)) {
	      println(s"***** PARSE ERRORS IN SCHEMA [${resp.getSchemaName}] *****")
         resp.getMessages.foreach {println(_)}
	   }
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
   
   
}
