package com.variant.server.test.spec

import scala.sys.process._

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec

import com.variant.core.util.IoUtils
import com.variant.server.play.VariantApplicationLoader

import play.api.Application
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder

/**
 * 
 */
object TempSchemataDir {
   val sessionTimeoutSecs = 15          // Override test default of 1
   val dirWatcherLatencyMsecs = 10000   // takes this long for FS to notify the directory watcher service.
   val schemataDir = "/tmp/schemata"  
}


/**
 * Tests which wish to operate on a temporary schemata directory
 * should mix this in.
 */
trait TempSchemataDir extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
            
   val schemataDir = TempSchemataDir.schemataDir 

   // Subclasses may override this.
   def schemata = Set[String](
         "schemata-test/monster.schema",
         "schemata-test/petclinic.schema")
   
   // Custom application builder.  
   // val references must be static because this is an implicit method.
   implicit override lazy val app: Application = {

      s"rm -rf ${TempSchemataDir.schemataDir}".!!
      s"mkdir ${TempSchemataDir.schemataDir}".!!

      schemata.foreach { f => 
         s"cp ${f} ${TempSchemataDir.schemataDir}".!! 
      }

      new GuiceApplicationBuilder()
         .configure(new Configuration(VariantApplicationLoader.config))
         .configure("variant.schemata.dir" -> TempSchemataDir.schemataDir)
         .configure("variant.session.timeout" -> TempSchemataDir.sessionTimeoutSecs)
         .build()
   }

   /**
    * Cleanup
    */
   override def afterAll() {
      s"rm -rf ${schemataDir}".!!
      super.afterAll();
   }
}