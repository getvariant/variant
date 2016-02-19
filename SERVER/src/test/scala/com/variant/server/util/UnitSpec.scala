package com.variant.server.util

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.Inspectors
import org.scalatest.Inside
import org.scalatest.BeforeAndAfterAll
import com.variant.server.core.VariantCore
import net.liftweb.http.testing.TestKit


/**
 * 
 */
object UnitSpec {
   
   // Use statc count to deal with SBT's parallel execution of tests.
   var upCount = 0;
}

/**
 * 
 */
abstract class UnitSpec extends FlatSpec with JettyStartupAndShutdown  with TestKit
   with Matchers with OptionValues with Inside with Inspectors with BeforeAndAfterAll  {
   
   lazy val schema = VariantCore.api.getSchema()
   
   /**
    * 
    */
   override def beforeAll() = {
      UnitSpec.synchronized {
         if (UnitSpec.upCount == 0) {
            // Actual setup - only if we havent setup yet.
            start()
            val parserResp = VariantCore.api.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"))
            parserResp.getMessages should have size (0)
         }
         UnitSpec.upCount += 1
      }
   }
   
   /**
    * 
    */
   override def afterAll() = {
      UnitSpec.synchronized {
         if (UnitSpec.upCount == 1) {
            // Actual breakdown - only if the caller is the last one.
            stop()
            //VariantCore.api.shutdown
         }
         UnitSpec.upCount -= 1
      }
   }

   /**
    * 
    */
   def openResourceAsInputStream(name: String) = {
      val result = this.getClass.getResourceAsStream(name);
      if (result == null) {
         throw new RuntimeException("Classpath resource '" + name + "' does not exist.");
      }
      result
   }
}
  
   
object JettyTestServer {
   
   private val server: Server = {
      val svr = new Server
      val connector = new SelectChannelConnector
      connector.setMaxIdleTime(30000);
      val context = new WebAppContext
      context.setServer(svr)
      context.setContextPath("/")
      context.setWar("src/main/webapp")
      svr.setConnectors(Array(connector));
      svr.setHandler(context)
      svr
   }
   lazy val port = server.getConnectors.head.getLocalPort
   lazy val url = "http://localhost:" + port
   
   def baseUrl = url
   
   /**
    * Only start if not yet started.
    */
    def start() { 
       server.start()
   }
   
   /**
    * Only stop if upcount is down to 1,
    * i.e. only one starter is still out there.
    */
   def stop() {
      server.stop()
      server.join()
   }
}

/**
 * 
 */
trait JettyStartupAndShutdown extends FlatSpec {

   
   def start() = {
      val now = System.currentTimeMillis()
      JettyTestServer.start
      info("Jetty started in " + (System.currentTimeMillis() - now) + " ms.")
   }
   
   def stop() = {
      val now = System.currentTimeMillis()
      JettyTestServer.stop
      info("Jetty stopped in " + (System.currentTimeMillis() - now) + " ms.")
   }
}