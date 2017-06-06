package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import com.variant.core.session.CoreSession
import com.variant.server.impl.SessionImpl

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ServerSessionTest extends BaseSpecWithServer {

  "Server session" should {

    "should serialize and deserialize" in  {
	
       val ssn1 = SessionImpl.empty(newSid)
       val schema = server.schema.get
       val state1 = schema.getState("state1")
       state1 mustNot be (null)
       server.runtime.targetSessionForState(ssn1, state1)
       ssn1.coreSession.getStateRequest mustNot be (null)
       
       val coreSsn2 = CoreSession.fromJson(ssn1.toJson, schema);
       coreSsn2.toJson mustBe ssn1.coreSession.toJson
       
    }
  }
}
