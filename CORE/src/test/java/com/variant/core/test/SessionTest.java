package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;

public class SessionTest extends BaseTestCore {

	/**
	 * No Session Test
	 */
	@Test
	public void noSchemaTest() throws Exception {
		
		// Can't create session if no schema.
		assertNull(api.getSchema());
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { api.getSession("foo"); }
		}.assertThrown(MessageTemplate.RUN_SCHEMA_UNDEFINED);

		// Unsuccessful parse will not create a schema, so we still should not be able to get a session.
		ParserResponse response = api.parseSchema("UNPARSABLE JUNK");
		assertEquals(Severity.FATAL, response.highestMessageSeverity());
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { api.getSession("foo"); }
		}.assertThrown(MessageTemplate.RUN_SCHEMA_UNDEFINED);

		
		// Create schema. We should be able to get and save.
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		VariantCoreSession ssn = api.getSession("bar");
		assertNotNull(ssn);

		api.getSessionService().saveSession(ssn);

		// Unsuccessful parse will not replace the existing schema, so still should be able to save.
		response = api.parseSchema("UNPARSABLE JUNK");
		assertEquals(Severity.FATAL, response.highestMessageSeverity());
		
		api.getSessionService().saveSession(ssn);

		// Successful parse invalidates existing schemas.
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		final VariantCoreSession ssnFinal = ssn;  // No closures in Java.
		
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { api.getSessionService().saveSession(ssnFinal); }
		}.assertThrown(MessageTemplate.RUN_SCHEMA_REPLACED, api.getSchema().getId(), ssnFinal.getSchemaId());

	}
	
	/**
	 * 
	 */
	@Test
	public void sessionCreationTest() throws Exception {
			
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		VariantCoreSession ssn = api.getSession("key");
		assertEquals("key", ssn.getId());
		assertNull(ssn.getStateRequest());
		assertEquals(0, ssn.getTraversedStates().size());
		assertEquals(0, ssn.getTraversedTests().size());
		String json = ((CoreSessionImpl)ssn).toJson();
		CoreSessionImpl deserializedSsn = CoreSessionImpl.fromJson(api, json);
		assertEquals("key", deserializedSsn.getId());
		assertNull(deserializedSsn.getStateRequest());
		assertEquals(0, deserializedSsn.getTraversedStates().size());
		assertEquals(0, deserializedSsn.getTraversedTests().size());
		
		// Test for idempotency. ssn2 is a different object from ssn,
		// but should have all props the same, except the creation timestamp.
		VariantCoreSession ssn2 = api.getSession("key");
		assertEquals("key", ssn2.getId());
		assertNull(ssn2.getStateRequest());
		assertEquals(0, ssn2.getTraversedStates().size());
		assertEquals(0, ssn2.getTraversedTests().size());
				
		VariantCoreSession ssn3 = api.getSession("another-key");
		assertNotEquals (ssn, ssn3);
		assertNull(ssn.getStateRequest());
		assertNull(ssn3.getStateRequest());
		json = ((CoreSessionImpl)ssn3).toJson();
		deserializedSsn = CoreSessionImpl.fromJson(api, json);
		assertEquals("another-key", deserializedSsn.getId());
		assertNull(deserializedSsn.getStateRequest());
		assertEquals(0, deserializedSsn.getTraversedStates().size());
		assertEquals(0, deserializedSsn.getTraversedTests().size());

	}

	@Test
	public void  stateRequestTest() throws Exception {
				
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		Schema schema = api.getSchema();
		VariantCoreSession ssn = api.getSession("foo");
		assertNull(ssn.getStateRequest());
		VariantStateRequest req1 = ssn.targetForState(schema.getState("state1"), "");
		assertNotNull(req1);
		assertEquals(req1, ssn.getStateRequest());
		assertNotNull(req1.getStateVisitedEvent());
		String json = ((CoreSessionImpl)ssn).toJson();
		CoreSessionImpl deserializedSsn = CoreSessionImpl.fromJson(api, json);
		assertEquals("foo", deserializedSsn.getId());
		VariantStateRequest deserializedReq = deserializedSsn.getStateRequest();
		assertEquals(deserializedReq.getSession(), deserializedSsn);
		assertEquals(req1.getState(), deserializedReq.getState());
		assertEqualAsSets(req1.getResolvedParameterMap(), deserializedReq.getResolvedParameterMap());
		assertEquals(req1.getStatus(), deserializedReq.getStatus());
		assertEqualAsSets(req1.getTargetedExperiences(), deserializedReq.getTargetedExperiences());
		assertNull(deserializedReq.getStateVisitedEvent());
		assertEqualAsSets(ssn.getTraversedStates(), deserializedSsn.getTraversedStates());
		assertEqualAsSets(ssn.getTraversedTests(), deserializedSsn.getTraversedTests());
		req1.commit("");
		System.out.println(((CoreSessionImpl)ssn).toJson());
		// Nothing is instrumented on state2
		VariantStateRequest req2 = ssn.targetForState(schema.getState("state2"), "");
		assertNotNull(req2);
		assertNotEquals(req1, req2);
		assertNull(req2.getStateVisitedEvent());
		assertEquals(req2, ssn.getStateRequest());
		json = ((CoreSessionImpl)ssn).toJson();
		deserializedSsn = CoreSessionImpl.fromJson(api, json);
		assertEquals("foo", deserializedSsn.getId());
		deserializedReq = deserializedSsn.getStateRequest();
		assertEquals(deserializedReq.getSession(), deserializedSsn);
		assertEquals(req2.getState(), deserializedReq.getState());
		assertEqualAsSets(req2.getResolvedParameterMap(), deserializedReq.getResolvedParameterMap());
		assertEquals(req2.getStatus(), deserializedReq.getStatus());
		assertEqualAsSets(req2.getTargetedExperiences(), deserializedReq.getTargetedExperiences());
		assertNull(deserializedReq.getStateVisitedEvent());
		assertEqualAsSets(ssn.getTraversedStates(), deserializedSsn.getTraversedStates());
		assertEqualAsSets(ssn.getTraversedTests(), deserializedSsn.getTraversedTests());
		req2.commit("");
	}

	@Test
	public void  crossSchemaTest() throws Exception {
				
		ParserResponse response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		Schema schema = api.getSchema();
		VariantCoreSession ssn1 = api.getSession("foo2");
		State state1 = schema.getState("state1");
		VariantStateRequest req = ssn1.targetForState(state1, "");
		req.commit("");  // Saves the session.

		Thread.sleep(10);
		
		VariantCoreSession ssn2 = api.getSession("foo2");
	    assertEquals(ssn1.creationTimestamp(), ssn2.creationTimestamp());

	    // new schema.
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
	    
		ssn2 = api.getSession("foo2");  // should be a new session because schema's changed
		assertEquals("foo2", ssn2.getId());
		assertNull(ssn2.getStateRequest());
		assertEquals(0,ssn2.getTraversedStates().size());
		assertEquals(0, ssn2.getTraversedTests().size());
	    assertTrue(ssn1.creationTimestamp() < ssn2.creationTimestamp());
		
	    Schema schema2 = api.getSchema();
	    assertNotEquals(schema.getId(), schema2.getId());
	    state1 = schema2.getState("state1");
		req = ssn2.targetForState(state1, "");
		req.commit("");  // Saves the session.
	    
		// new API
		rebootApi();
		assertNull(api.getSchema());
		
		response = api.parseSchema(SchemaParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		Schema schema3 = api.getSchema();
		assertNotEquals(schema2.getId(), schema3.getId());
		
		VariantCoreSession ssn3 = api.getSession("foo2"); // should be a new session because api's changed
		assertEquals("foo2", ssn3.getId());
		assertNull(ssn3.getStateRequest());
		assertEquals(0,ssn3.getTraversedStates().size());
		assertEquals(0, ssn3.getTraversedTests().size());
	    assertTrue(ssn2.creationTimestamp() < ssn3.creationTimestamp());
		
	}

}
