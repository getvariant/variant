package com.variant.server.util

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import org.specs2.Specification
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.Inspectors
import org.scalatest.Inside


/**
 * 
 */
abstract class UnitSpec extends FlatSpec with Matchers with
  OptionValues with Inside with Inspectors
  
   
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
   
   lazy val start = server.start()
   
   def stop() {   
      server.stop()
      server.join()
   }
}

/**
 * 
 */
trait JettyStartupAndShutdown  {

   val log = Logger(LoggerFactory.getLogger(this.getClass))
   
   def start() = {
      val now = System.currentTimeMillis()
      JettyTestServer.start
      log.info("Jetty started in " + (System.currentTimeMillis() - now) + " ms.");
   }
   
   def stop() = {
      val now = System.currentTimeMillis()
      JettyTestServer.stop
      log.info("Jetty stopped in " + (System.currentTimeMillis() - now) + " ms.");
   }
}