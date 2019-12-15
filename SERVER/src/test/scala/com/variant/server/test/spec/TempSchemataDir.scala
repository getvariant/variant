package com.variant.server.test.spec

/**
 * Run embedded server with the schemata directory in arbitrary place on FS,
 * default /tmp/schemata.
 */
import scala.sys.process._
import org.scalatest.BeforeAndAfterAll
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.variant.server.boot.VariantServer
import com.typesafe.scalalogging.LazyLogging

/**
 * Tests which wish to operate on a temporary schemata directory
 * should mix this in.
 */
object TempSchemataDir {

   // takes this long (!) for FS to notify the directory watcher service.
   // the latency is particularly pronounced when running the entire harness with `test`
   private val dirWatcherLatencySecs = 15

   // Override test default of 1. We want sessions survive our waiting for the directory watcher.
   private val sessionTimeoutSecs = dirWatcherLatencySecs + 2

   private val schemataDirDefault = "/tmp/schemata"
}

trait TempSchemataDir extends BeforeAndAfterAll with LazyLogging { self: EmbeddedServerSpec =>

   import TempSchemataDir._

   val dirWatcherLatencyMillis = dirWatcherLatencySecs * 1000

   val sessionTimeoutMillis = sessionTimeoutSecs * 1000

   /**
    * Subclasses may override the temp schemata directory.
    */
   protected lazy val schemataDir = schemataDirDefault

   /**
    *  Subclasses may override what files will be copied into the temp schemata directory.
    */
   protected lazy val schemata = Set[String](
      "schemata/monster.schema",
      "schemata/petclinic.schema")

   /**
    * Individual tests may override server builder to control initial
    */
   override lazy val serverBuilder: Unit => VariantServer.Builder = { _ =>

      // Recreate the temp schema directory so that the server comes up without errors.
      s"rm -rf ${schemataDir}" !;
      s"mkdir ${schemataDir}" !

      schemata.foreach { f =>
         s"cp ${f} ${schemataDir}" !
      }

      VariantServer.builder.headless
         .withConfiguration(
            Map(
               "variant.schemata.dir" -> TempSchemataDir.schemataDirDefault,
               "variant.session.timeout" -> sessionTimeoutSecs))
   }

   /**
    * Cleanup
    */
   override def afterAll() {
      s"rm -rf ${schemataDir}".!!
      logger.info(s"Removed temp schemata directory $schemataDir")
      super.afterAll();
   }
}