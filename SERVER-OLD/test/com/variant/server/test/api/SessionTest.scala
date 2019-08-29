package com.variant.server.test.api

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConverters._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.error.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import java.util.Optional
import com.variant.server.impl.ConfigurationImpl

/**
 * Session Controller Tests
 */
class SessionTest extends EmbeddedServerSpec {
	
   val emptyTargetingTrackerBody = "{\"tt\":[]}"

   "Variant session" should {

   	"have valid config" in {
	   	
   		var actualSid: String = null
	         
	      assertResp(route(app, httpReq(POST, "/session/monstrosity/" + newSid).withBody(emptyTargetingTrackerBody)))
	      	.isOk
	         .withBodySession { ssn =>
	            actualSid = ssn.getId
	         }
	         
	      val ssn = server.ssnStore.get(actualSid).get
			val config = ssn.getConfiguration
			config mustNot be (null)
	      config.asInstanceOf[ConfigurationImpl].asMap().asScala.map(e => println(e._1 + ", " + e._2))
	      config.asInstanceOf[ConfigurationImpl].asMap().size() mustBe 7
	      config.getDefaultEventFlusherClassName mustBe "com.variant.extapi.std.flush.jdbc.TraceEventFlusherH2"
	      config.getDefaultEventFlusherClassInit mustBe Optional.of("""{"password":"variant","url":"jdbc:h2:mem:variant;MVCC=true;DB_CLOSE_DELAY=-1;","user":"variant"}""")
	      config.getSessionTimeout mustBe 1
	      config.getEventWriterBufferSize mustBe 200
	      config.getEventWriterMaxDelay mustBe 2
	      config.getSchemataDir mustBe "schemata-test"
	      config.getSessionVacuumInterval mustBe 1
	      	      
   	}
   }
}
