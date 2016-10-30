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
                  "play.http.context" -> "/variant-test"
                  ,"variant.session.timeout.secs" -> 1
                  ,"variant.session.store.vacuum.interval.secs" -> 1
                  ,"variant.event.writer.flush.max.delay.millis" -> 1000
                  ,"variant.event.flusher.class.name" -> "com.variant.server.event.EventFlusherH2"
                  ,"variant.event.flusher.class.init" ->"""{"url":"jdbc:h2:mem:variant;MVCC=true;DB_CLOSE_DELAY=-1;","user":"variant","password":"variant"}"""
            ))
       .build()

   protected val context = app.configuration.getString("play.http.context").get
   protected val store = app.injector.instanceOf[SessionStore]
   
  /**
	 * Each case runs in its own JVM. Each test runs in its
	 * own instance of the test case. We want the jdbc schema
	 * created only once per jvm, but the api be instance scoped.
	 * 
	 * @throws Exception
	 * Needed by the JUnit EventWriter test which is currently off.
	@Before
	public void _beforeTest() throws Exception {
		synchronized (sqlSchemaCreated) { // once per JVM
			if (!sqlSchemaCreated) {
				VariantCore core = rebootApi();
				recreateSchema(core);
				sqlSchemaCreated = true;
			}
		}
	}
	*/
}
