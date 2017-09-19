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
	
         val schema = server.schema.get
         val state1 = schema.getState("state1")
         state1 mustNot be (null)
       
         val ssn = SessionImpl.empty(newSid())
       
         ssn.targetForState(state1)
         ssn.getStateRequest mustNot be (null)
       
         val coreSsn2 = CoreSession.fromJson(ssn.toJson, schema);
         coreSsn2.toJson mustBe ssn.coreSession.toJson 
      }
      
      /* This will only apply when schema is hot-deployable.
      "should invalidate session when schema changes" in  {
	
         val ssn = SessionImpl.empty(newSid())
         val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTestNoHooks.json")).get
   	   response.hasMessages() mustBe false
   		server.schema.isDefined mustBe true
         val state1 = server.schema.get.getState("state1")
   		ssn.targetForState(state1);
      }
      */
   }
}
