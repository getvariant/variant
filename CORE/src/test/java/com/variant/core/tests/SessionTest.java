package com.variant.core.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserResponse;

public class SessionTest extends BaseTest {

	/**
	 * 
	 */
	@Test
	public void sessionCreationTest() throws VariantBootstrapException {
				

		VariantSession ssn = engine.getSession("key");
		assertNotNull(ssn);
		assertNull(ssn.getStateRequest());

		VariantSession ssn2 = engine.getSession("key");
		assertEquals(ssn, ssn2);
		assertNull(ssn.getStateRequest());
		assertNull(ssn2.getStateRequest());
		
		VariantSession ssn3 = engine.getSession("another-key");
		assertNotEquals (ssn, ssn3);
		assertNull(ssn.getStateRequest());
		assertNull(ssn3.getStateRequest());
	}
	
	@Test
	public void  stateRequestTest() {
		
		Variant variant = Variant.Factory.getInstance();
		
		ParserResponse response = engine.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		Schema schema = variant.getSchema();
		VariantSession ssn = engine.getSession("foo");
		assertNull(ssn.getStateRequest());
		VariantStateRequest req1 = engine.dispatchRequest(ssn, schema.getState("state1"), "");
		assertNotNull(req1);
		assertEquals(req1, ssn.getStateRequest());
		engine.commitStateRequest(req1, "");
		assertEquals(req1, ssn.getStateRequest());
		VariantStateRequest req2 = engine.dispatchRequest(ssn, schema.getState("state2"), "");
		assertNotNull(req1);
		assertNotEquals(req1, req2);
		assertEquals(req2, ssn.getStateRequest());
		
	}
	
}
