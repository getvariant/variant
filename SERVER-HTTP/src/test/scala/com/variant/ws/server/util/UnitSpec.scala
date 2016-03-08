package com.variant.ws.server.util

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
import com.variant.ws.server.core.VariantCore
import net.liftweb.http.testing.TestKit
import net.liftweb.http.testing.ReportFailure
import com.variant.core.Variant
import com.variant.core.jdbc.JdbcService


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
   
   import UnitSpec._
   
   lazy val baseUrl = JettyTestServer.baseUrl
   lazy val api = VariantCore.api

   /**
    * 
    */
   implicit val reportError = new ReportFailure {
      def fail(msg: String) = {
         this.fail(msg)
         
      }
   }
   
   /**
    * 
    */
   override def beforeAll() = {
      UnitSpec.synchronized {
         // Actual setup - only if we haven't done it yet
         if (upCount == 0) jvmSetup
         upCount += 1
      }
   }
   
   /**
    * 
    */
   override def afterAll() = {
      UnitSpec.synchronized {
         // Actual breakdown - only if the caller is the last one.
         if (upCount == 1) {
            // Wait for async event writer to complete if any queued up events
            Thread.sleep(500)
            jvmShutdown
         }
         upCount -= 1
      }
   }

   private def jvmSetup = {
      startJetty
      VariantCore.init("/variant-test.props")
      new JdbcService(VariantCore.api).createSchema
   }

   private def jvmShutdown = {
      stopJetty  
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

   
   def startJetty() = {
      val now = System.currentTimeMillis()
      JettyTestServer.start
      info("Jetty started in " + (System.currentTimeMillis() - now) + " ms.")
   }
   
   def stopJetty() = {
      val now = System.currentTimeMillis()
      JettyTestServer.stop
      info("Jetty stopped in " + (System.currentTimeMillis() - now) + " ms.")
   }
}