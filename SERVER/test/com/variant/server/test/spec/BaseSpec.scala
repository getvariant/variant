package com.variant.server.test.spec

import play.api.libs.json.JsValue
import com.variant.core.ServerError
import org.scalatestplus.play.PlaySpec
import com.variant.server.api.Session
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.impl.SessionImpl
import com.variant.core.schema.Schema
import com.variant.core.util.StringUtils
import com.variant.core.util.Constants._
import java.util.Random
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Application
import com.variant.server.boot.VariantServer
import com.variant.server.conn.ConnectionStore
import com.variant.server.conn.SessionStore

/**
 * 
 */
trait BaseSpec extends PlaySpec {
      
   /**
    * Upstream will define the application.
    * note, that its value is not stable, so dependencies
    * should be defs.
    */
   protected def application: Application
   
   protected def context = application.configuration.getString("play.http.context").get
   protected def server = application.injector.instanceOf[VariantServer]
   protected def connStore = application.injector.instanceOf[ConnectionStore]
   protected def ssnStore = application.injector.instanceOf[SessionStore]

   /**
    * Parse an 400 error body
    */
   protected def parseError(body: JsValue): (Boolean, ServerError, Seq[String]) = {
      body mustNot be (null)
      val isInternal = (body \ "isInternal").as[Boolean]
      val code = (body \ "code").as[Int] 
      val args = (body \ "args").as[Seq[String]]
      (isInternal, ServerError.byCode(code), args)
   }
   
   /**
    * Create and add a targeting stabile to a session.
    */
   protected def setTargetingStabile(ssn: Session, experiences: String*) {
		val stabile = new SessionScopedTargetingStabile()
		experiences.foreach {e => stabile.add(experience(e, ssn.getSchema))}
		ssn.asInstanceOf[SessionImpl].coreSession.setTargetingStabile(stabile);
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
      StringUtils.random64BitString(new Random(System.currentTimeMillis()))
   
   /**
    * Normalize a JSON string by removing any white space.
    */
   protected def normalJson(jsonIn: String):String = Json.stringify(Json.parse(jsonIn))

   /**
    * All connect route calls should use this. This is the only request
    * that does not have connection id in X-Connection-ID header.
    */
   protected def connectionRequest(schemaName: String) = {
      FakeRequest(POST, context + "/connection/" + schemaName).withHeaders("Content-Type" -> HTTP_HEADER_CONTENT_TYPE)
   }
   
   /**
    * All route calls, emulating a connected API call.
    */
   protected def connectedRequest(method: String, uri: String, connId: String) = {
      FakeRequest(method, uri)
         .withHeaders("Content-Type" -> HTTP_HEADER_CONTENT_TYPE, HTTP_HEADER_CONNID -> connId)
   }

}