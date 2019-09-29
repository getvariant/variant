package com.variant.server.test.spec

import org.scalatest.BeforeAndAfterAll

import com.typesafe.scalalogging.LazyLogging
import com.variant.server.test.util.JdbcService

/**
 * Mixes in an embedded SQL database for the events.
 * The default test configuration writes events into an embedded H2 DB.
 * The tests that don't trigger events are free to not mixin this trait.
 * Can only be mixed-in to a class implementing an EmbeddedServerSpec.
 */
/**
 * Global state to help avoid recreating the schema in the same VM.
 * (HOw do I make the field private to this source file?)
 */
object TraceEventSpec {
   var sqlSchemaCreated = false
}

trait TraceEventsSpec extends LazyLogging with BeforeAndAfterAll { this: EmbeddedServerSpec =>

   import TraceEventSpec._

   /**
    * @throws Exception
    *
    */
   def recreateDatabase() {
      // Assuming the there's always the monstrosity schema and that it has the right event writer.
      server.schemata.getLiveGen("monstrosity") match {

         case Some(schema) =>

            val jdbc = new JdbcService(schema.flusherService.getFlusher)

            try {
               jdbc.getVendor match {
                  case JdbcService.Vendor.POSTGRES =>
                     jdbc.recreateSchema()
                     logger.info("Recreated PostgreSQL schema")

                  case JdbcService.Vendor.H2 =>
                     jdbc.createSchema()
                     logger.info("Recreated H2 schema")

                  case JdbcService.Vendor.MYSQL =>
                     jdbc.createSchema()
                     logger.info("Recreated MySQL schema")

               }

            } catch {
               case _: ClassCastException =>
               case e: Throwable => throw e
            }

         case None => println(" *** Running withut a database *** ")
      }
   }

   /* Print deployment errors, if any
    **** this doesn't seem to belong here ****
   server.schemaDeployer.parserResponses.foreach { resp =>
      if (resp.hasMessages(Severity.ERROR)) {
         println(s"***** PARSE ERRORS IN SCHEMA [${resp.getSchemaName}] *****")
         resp.getMessages.asScala.foreach { println(_) }
      }
   }
*/
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
