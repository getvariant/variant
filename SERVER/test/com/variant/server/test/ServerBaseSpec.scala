package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import javax.inject.Inject
import com.variant.server.session.SessionStore
import org.scalatest.BeforeAndAfterAll
import com.variant.server.jdbc.JdbcService
import com.variant.server.event.EventWriter
import com.variant.server.event.EventWriter
import com.variant.server.boot.VariantServer
import com.variant.core.session.CoreSessionImpl
import com.variant.server.session.ServerSession
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.core.schema.Schema
import java.util.Random
import com.variant.core.event.impl.util.VariantStringUtils

/**
 * Common to all tests.
 * Builds a custom application.
 */
object ServerBaseSpec {
   private var sqlSchemaCreated = false
}

class ServerBaseSpec extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {

   import ServerBaseSpec._
   
   // Set system property, if needed
   //sys.props += (("variant.schema.dir", "/schemas"))

   // Override app if you need a Application with other than
   // default parameters. 
   // TODO: find a way to externalize it, 
   implicit override lazy val app: Application = new GuiceApplicationBuilder()
      .configure(
            Map(
                  "play.http.context" -> "/variant-test"
                  ,"variant.schemas.dir" -> "test-schemas"
                  ,"variant.session.timeout" -> 1
                  ,"variant.session.store.vacuum.interval" -> 1
                  ,"variant.event.writer.buffer.size" -> 200
                  ,"variant.event.writer.max.delay" -> 2
                  ,"variant.event.flusher.class.name" -> "com.variant.server.event.EventFlusherH2"
                  ,"variant.event.flusher.class.init" ->"""{"url":"jdbc:h2:mem:variant;MVCC=true;DB_CLOSE_DELAY=-1;","user":"variant","password":"variant"}"""
            ))
       .build()

   protected val context = app.configuration.getString("play.http.context").get
   protected val store = app.injector.instanceOf[SessionStore]
   protected val server = app.injector.instanceOf[VariantServer]
 
   "Server must come up with a valid schema" in {
      server.schema.isDefined mustBe true 
   }
   
    /**
	 * Each case runs in its own JVM. Each test runs in its
	 * own instance of the test case. We want the jdbc schema
	 * created only once per jvm, but the api be instance scoped.
	 * 
	 * @throws Exception
	 * Needed by the JUnit EventWriter test which is currently off.
    */
   override def beforeAll() {
		synchronized { // once per JVM
			if (!sqlSchemaCreated) {
				recreateSchema()
				sqlSchemaCreated = true
			}
		}
	}
   
   /**
    * Create and add a targeting stabile to a session.
    */
   protected def setTargetingStabile(ssn: ServerSession, experiences: String*) {
		val stabile = new SessionScopedTargetingStabile()
		experiences.foreach {e => stabile.add(experience(e, ssn.getSchema()))}
		ssn.coreSessionImpl.setTargetingStabile(stabile);
	}

   /**
    * Find experience object by its comma separated name.
    */
   protected def experience(name: String, schema: Schema) = {
		val tokens = name.split("\\.")
		assert(tokens.length == 2)
		schema.getTest(tokens(0)).getExperience(tokens(1))
	}

   /**
    * Generate a new random session ID.
    */
   protected def newSid() = 
      VariantStringUtils.random64BitString(new Random(System.currentTimeMillis()))
   
   /**
	 * @throws Exception 
	 * 
	 */
	private def recreateSchema() {
		
		val jdbc = new JdbcService(server.eventWriter);
		
		try {			
			jdbc.getVendor() match {
   			case JdbcService.Vendor.POSTGRES => jdbc.recreateSchema()
	   		case JdbcService.Vendor.H2 => jdbc.createSchema()  // Fresh in-memory DB.
		   }
		}
		catch {
		   case _: ClassCastException => 
		   case e: Throwable => throw e		
		}
	}

}
