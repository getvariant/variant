package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import javax.inject.Inject
import com.variant.server.session.SessionStore

/**
 * Common to all tests.
 * Builds a custom application.
 */
class VariantSpec extends PlaySpec with OneAppPerSuite {

   // Override app if you need a Application with other than
   // default parameters.
   implicit override lazy val app: Application = new GuiceApplicationBuilder()
      .configure(
            Map(
                  "play.http.context" -> "/variant-test",
                  "variant.session.timeout.secs" -> 1,
                  "variant.session.store.vacuum.interval.secs" -> 1 ))
       .build()
   
   protected val context = app.configuration.getString("play.http.context").get
   protected val store = app.injector.instanceOf[SessionStore]
   
}
