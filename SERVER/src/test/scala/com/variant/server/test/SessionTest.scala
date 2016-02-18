package com.variant.server.test

import net.liftweb.http.testing.{TestKit,ReportFailure,HttpResponse}
import com.variant.server.util.JettyStartupAndShutdown
import com.variant.server.util.JettyTestServer
import com.variant.core.session.VariantSessionImpl
import org.specs2.Specification
import com.variant.server.util.UnitSpec

// trait WebServiceSpec { _: Specification with JettySetupAndTearDown with TestKit =>
class JettyTest extends UnitSpec with JettyStartupAndShutdown with TestKit {
  
   implicit val reportError = new ReportFailure {
      def fail(msg: String) = {
         JettyTest.this.fail(msg)
      }
   }
  
   lazy val baseUrl = JettyTestServer.baseUrl

   before { start }
  
   after { stop }

   "Get non-existent session" should "create new session" in {
      val id = "foo"
      val resp =  get("/session/" + id) !@ "Jetty is not running"
      assertResult(true) {resp.bodyAsString.equals(new VariantSessionImpl(id).toJson)}
   }
}