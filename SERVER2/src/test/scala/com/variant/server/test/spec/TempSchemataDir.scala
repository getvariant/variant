package com.variant.server.test.spec

/**
 * Run embedded server with the schemata directory in arbitrary place on FS,
 * default /tmp/schemata.
 */
import scala.sys.process._
import org.scalatest.BeforeAndAfterAll
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.variant.server.boot.VariantServer

/**
 * Tests which wish to operate on a temporary schemata directory
 * should mix this in.
 */
object TempSchemataDir {

   private val sessionTimeoutSecs = 17 // Override test default of 1. We want sessions survive our waiting for the directory watcher.
   private val schemataDirDefault = "/tmp/schemata"
}

trait TempSchemataDir extends BaseSpec with BeforeAndAfterAll { self: EmbeddedServerSpec =>

   import TempSchemataDir._

   val dirWatcherLatencyMsecs = 15000 // takes this long (!) for FS to notify the directory watcher service.
   val sessionTimeoutMillis = sessionTimeoutSecs * 1000

   /**
    * Subclasses may override the temp schemata directory.
    */
   protected lazy val schemataDir = schemataDirDefault

   /**
    *  Subclasses may override what files go into the temp schemata directory.
    */
   protected lazy val schemata = Set[String](
      "schemata/monster.schema",
      "schemata/petclinic.schema")

   /**
    * Individual tests may override server builder to control initial
    */
   override lazy val serverBuilder: Unit => VariantServer.Builder = { _ =>

      // create the temp schema directory so that the server comes up without errors.
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
      super.afterAll();
   }
}