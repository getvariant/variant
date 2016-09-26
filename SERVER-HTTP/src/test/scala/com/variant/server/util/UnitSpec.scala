package com.variant.server.util

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.Inspectors
import org.scalatest.Inside
import org.scalatest.BeforeAndAfterAll
import net.liftweb.http.testing.TestKit
import net.liftweb.http.testing.ReportFailure
import com.variant.core.jdbc.JdbcService
import com.variant.core.impl.VariantCore
import scala.util.Random
import com.variant.core.impl.VariantComptime
import com.variant.server.ServerBoot
import com.variant.core.util.inject.Injector
import org.eclipse.jetty.server.ServerConnector

/**
 * Used by unit tests to run against an embedded Jetty server.
 * Jetty server looks at the unpacked version of the web app in the source tree.
 */
object UnitSpec {   
   
   val rand = new Random(System.currentTimeMillis())

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

   // We'll need the client side api too.
   Injector.setConfigNameAsResource("/com/variant/server/conf/injector.json")
   val clientCore = new VariantCore()
   clientCore.getComptime().registerComponent(VariantComptime.Component.CLIENT, "0.6.3");
   
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
      //ServerBoot.boot("/variant-test.props")
      new JdbcService(ServerBoot.getCore).createSchema
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
 
/**
 * 
 */
object JettyTestServer {
   
   val logger = Logger(LoggerFactory.getLogger(this.getClass))

   private val server: Server = {
      val svr = new Server
      val connector = new ServerConnector(svr)
      connector.setIdleTimeout(30000);
      connector.setPort(8080);
      val context = new WebAppContext
      context.setServer(svr)
      context.setContextPath("/")
      context.setWar("src/main/webapp")
      svr.setConnectors(Array(connector));
      svr.setHandler(context)
      svr
   }
   
   lazy val url = "http://localhost:8080"
   
   def baseUrl = url
   
   /**
    * Start Jetty server.
    */
    def start() { 
      val now = System.currentTimeMillis()
      server.start()
      logger.info("Jetty started in " + (System.currentTimeMillis() - now) + " ms.")
   }
   
   /**
    * Stop Jetty server
    */
   def stop() {
      val now = System.currentTimeMillis()
      server.stop()
      server.join()
      logger.info("Jetty stopped in " + (System.currentTimeMillis() - now) + " ms.")
   }
}

/**
 * 
 */
trait JettyStartupAndShutdown extends FlatSpec {
   
   def startJetty() = {
      JettyTestServer.start
   }
   
   def stopJetty() = {
      JettyTestServer.stop
   }
}