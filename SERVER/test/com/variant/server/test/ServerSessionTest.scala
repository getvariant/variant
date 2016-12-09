package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ServerSessionTest extends BaseSpecWithSchema {

  "Routes" should {

    "send 404 on a bad request" in  {
	/**
	 * No Session Test
	 *
	@Test
	public void noSchemaTest() throws Exception {
		
		// Can't create session if no schema.
		assertNull(core.getSchema());
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { core.getSession("foo", true); }
		}.assertThrown(Error.RUN_SCHEMA_UNDEFINED);

		// Unsuccessful parse will not create a schema, so we still should not be able to get a session.
		ParserResponse response = core.parseSchema("UNPARSABLE JUNK");
		assertEquals(Severity.FATAL, response.highestMessageSeverity());
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { core.getSession("foo", true); }
		}.assertThrown(Error.RUN_SCHEMA_UNDEFINED);

		
		// Create schema. We should be able to get and save.
		response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		VariantCoreSession ssn = core.getSession("bar", true).getBody();
		assertNotNull(ssn);

		//core.saveSession((CoreSessionImpl)ssn);

		// Unsuccessful parse will not replace the existing schema, so still should be able to save.
		response = core.parseSchema("UNPARSABLE JUNK");
		assertEquals(Severity.FATAL, response.highestMessageSeverity());
		
		//core.saveSession((CoreSessionImpl)ssn);

		// Successful parse invalidates existing schemas.
		response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		final VariantCoreSession ssnFinal = ssn;  // No closures in Java
		
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				core.getSession("bar", true); 
			}
		}.assertThrown(Error.RUN_SCHEMA_MODIFIED, core.getSchema().getId(), ssnFinal.getSchemaId());

		
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				VariantCoreStateRequest req = ssnFinal.targetForState(core.getSchema().getState("state1"));
				req.commit();
			}
		}.assertThrown(Error.RUN_SCHEMA_MODIFIED, core.getSchema().getId(), ssnFinal.getSchemaId());
	}
	* 
	*/
	
    }
  }
}
