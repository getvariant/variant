package com.variant.server.test.spec

import org.scalatest.BeforeAndAfterAll

import com.typesafe.scalalogging.LazyLogging
import com.variant.server.test.util.JdbcService
import com.variant.server.test.util.TraceEventReader

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

trait TraceEventsSpec extends LazyLogging with BeforeAndAfterAll { 
   
   this: EmbeddedServerSpec =>

   import TraceEventSpec._

   // Tests may override this if monstrosity is unavailable.
   protected lazy val schemaName = "monstrosity" 
   
   private[this] var _eventReader: TraceEventReader = null
   
   protected lazy val eventReader: TraceEventReader = _eventReader

   /**
    */
   def recreateDatabase() {
      // Assuming the there's always the monstrosity schema and that it has the right event writer.
      server.schemata.getLiveGen(schemaName) match {

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
               
               _eventReader = TraceEventReader(schema.flusherService.getFlusher)

            } catch {
               case _: ClassCastException =>
               case e: Throwable => throw e
            }

         case None => println(" *** Running withut a database *** ")
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
