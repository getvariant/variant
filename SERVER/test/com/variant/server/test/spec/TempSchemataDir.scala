package com.variant.server.test.spec

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import org.scalatest.BeforeAndAfterAll
import play.api.Application
import com.variant.core.util.IoUtils
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Configuration
import com.variant.server.boot.VariantApplicationLoader
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/**
 * 
 */
object TempSchemataDir {
   val sessionTimeoutSecs = 15         // Override test default of 1
   val dirWatcherLatencyMsecs = 10000   // takes this long for FS to notify the directory watcher service.
   val schemataDir = "/tmp/schemata"  
}


/**
 * Tests which wish to operate on a temporary schemata directory
 * should mix this in.
 */
trait TempSchemataDir extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {
      
   import TempSchemataDir._
   
   val config: String = "{}"
   
   // Custom application builder.  
   implicit override lazy val app: Application = {
      IoUtils.delete(schemataDir)
      IoUtils.fileCopy("conf-test/ParserConjointOkayBigTestNoHooks.json", s"${schemataDir}/ParserConjointOkayBigTestNoHooks.json");
      IoUtils.fileCopy("distr/schemata/petclinic-experiments.json", s"${schemataDir}/petclinic-experiments.json");
      sys.props +=("variant.ext.dir" -> "distr/ext")
      new GuiceApplicationBuilder()
         .configure(new Configuration(VariantApplicationLoader.config))
         .configure("variant.schemata.dir" -> schemataDir)
         .configure("variant.session.timeout" -> sessionTimeoutSecs)
         .configure(new Configuration(ConfigFactory.parseString(config)))
         .build()
   }

   /**
    * Cleanup
    */
   override def afterAll() {
      IoUtils.delete(schemataDir)
      super.afterAll();
   }
}