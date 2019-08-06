package com.variant.server.test.spec

/**
 * Run embedded server with the schemata directory in arbitrary place on FS,
 * default /tmp/schemata.
 */
import scala.sys.process._
import org.scalatest.BeforeAndAfterAll

/**
 * Tests which wish to operate on a temporary schemata directory
 * should mix this in.
 */
trait TempSchemataDir extends EmbeddedServerSpec with BeforeAndAfterAll {

   val sessionTimeoutSecs = 15 // Override test default of 1
   val dirWatcherLatencyMsecs = 10000 // takes this long for FS to notify the directory watcher service.
   val schemataDir = "/tmp/schemata"

   // Subclasses may override this.
   def schemata = Set[String](
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

   server.bootExceptions.size mustEqual 0
   server.schemata.size mustBe 2

   /**
    * Cleanup
    */
   override def afterAll() {
      s"rm -rf ${schemataDir}".!!
      super.afterAll();
   }
}