package com.variant.server.test.spec

/**
 * Run embedded server with the schemata directory in arbitrary place on FS,
 * default /tmp/schemata.
 */
import scala.sys.process._
import org.scalatest.BeforeAndAfterAll
import akka.http.scaladsl.testkit.ScalatestRouteTest

/**
 * Tests which wish to operate on a temporary schemata directory
 * should mix this in.
 */
trait TempSchemataDir extends BaseSpec with BeforeAndAfterAll {

   self: EmbeddedServerSpec =>

   val dirWatcherLatencyMsecs = 10000 // takes this long for FS to notify the directory watcher service.
   val sessionTimeoutSecs = 15 // Override test default of 1. We want sessions survive our waiting for the directory watcher.
   val schemataDir = "/tmp/schemata"

   // Subclasses may override this.
   lazy val schemata = Set[String](
      "schemata/monster.schema",
      "schemata/petclinic.schema")

   s"rm -rf ${schemataDir}" !;
   s"mkdir ${schemataDir}" !

   schemata.foreach { f =>
      s"cp ${f} ${schemataDir}" !
   }

   new ServerBuilder()
      .withConfig(
         Map(
            "variant.schemata.dir" -> schemataDir,
            "variant.session.timeout" -> sessionTimeoutSecs))
      .reboot()

   /**
    * Cleanup
    */
   override def afterAll() {
      s"rm -rf ${schemataDir}".!!
      super.afterAll();
   }
}