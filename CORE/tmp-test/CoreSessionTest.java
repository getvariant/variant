package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.exception.Error;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.session.CoreSession;
import com.variant.server.ParserResponse;
import com.variant.server.ParserMessage.Severity;
import com.variant.server.test.ParserDisjointOkayTest;

public class CoreSessionTest extends BaseTestCore {

	/**
	 * 
	 */
	@Test
	public void sessionCreationTest() throws Exception {
			
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		VariantCoreSession ssn = core.getSession("key", true).getBody();
		assertEquals("key", ssn.getId());
		assertNull(ssn.getStateRequest());
		assertEquals(0, ssn.getTraversedStates().size());
		assertEquals(0, ssn.getTraversedTests().size());
		String json = ((CoreSession)ssn).toJson();
		CoreSession deserializedSsn = new CoreSession(json, core);
		assertEquals("key", deserializedSsn.getId());
		assertNull(deserializedSsn.getStateRequest());
		assertEquals(0, deserializedSsn.getTraversedStates().size());
		assertEquals(0, deserializedSsn.getTraversedTests().size());
		
		// Test for idempotency. ssn2 is a different object from ssn,
		// but should have all props the same, except the creation timestamp.
		VariantCoreSession ssn2 = core.getSession("key", true).getBody();
		assertEquals("key", ssn2.getId());
		assertNull(ssn2.getStateRequest());
		assertEquals(0, ssn2.getTraversedStates().size());
		assertEquals(0, ssn2.getTraversedTests().size());
				
		VariantCoreSession ssn3 = core.getSession("another-key", true).getBody();
		assertNotEquals (ssn, ssn3);
		assertNull(ssn.getStateRequest());
		assertNull(ssn3.getStateRequest());
		json = ((CoreSession)ssn3).toJson();
		deserializedSsn = new CoreSession(json, core);
		assertEquals("another-key", deserializedSsn.getId());
		assertNull(deserializedSsn.getStateRequest());
		assertEquals(0, deserializedSsn.getTraversedStates().size());
		assertEquals(0, deserializedSsn.getTraversedTests().size());

	}

	@Test
	public void  stateRequestTest() throws Exception {
				
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		Schema schema = core.getSchema();
		VariantCoreSession ssn = core.getSession("foo", true).getBody();
		assertNull(ssn.getStateRequest());
		VariantCoreStateRequest req1 = ssn.targetForState(schema.getState("state1"));
		assertNotNull(req1);
		assertEquals(req1, ssn.getStateRequest());
		assertNotNull(req1.getStateVisitedEvent());
		String json = ((CoreSession)ssn).toJson();
		CoreSession deserializedSsn = new CoreSession(json, core);
		assertEquals("foo", deserializedSsn.getId());
		VariantCoreStateRequest deserializedReq = deserializedSsn.getStateRequest();
		assertEquals(deserializedReq.getSession(), deserializedSsn);
		assertEquals(req1.getState(), deserializedReq.getState());
		assertEqualAsSets(req1.getResolvedParameterNames(), deserializedReq.getResolvedParameterNames());
		assertEquals(req1.getStatus(), deserializedReq.getStatus());
		assertEqualAsSets(req1.getLiveExperiences(), deserializedReq.getLiveExperiences());
		assertNull(deserializedReq.getStateVisitedEvent());
		assertEqualAsSets(ssn.getTraversedStates(), deserializedSsn.getTraversedStates());
		assertEqualAsSets(ssn.getTraversedTests(), deserializedSsn.getTraversedTests());
		req1.commit();
		System.out.println(((CoreSession)ssn).toJson());
		// Nothing is instrumented on state2
		VariantCoreStateRequest req2 = ssn.targetForState(schema.getState("state2"));
		assertNotNull(req2);
		assertNotEquals(req1, req2);
		assertNull(req2.getStateVisitedEvent());
		assertEquals(req2, ssn.getStateRequest());
		json = ((CoreSession)ssn).toJson();
		deserializedSsn = new CoreSession(json, core);
		assertEquals("foo", deserializedSsn.getId());
		deserializedReq = deserializedSsn.getStateRequest();
		assertEquals(deserializedReq.getSession(), deserializedSsn);
		assertEquals(req2.getState(), deserializedReq.getState());
		assertEqualAsSets(req2.getResolvedParameterNames(), deserializedReq.getResolvedParameterNames());
		assertEquals(req2.getStatus(), deserializedReq.getStatus());
		assertEqualAsSets(req2.getLiveExperiences(), deserializedReq.getLiveExperiences());
		assertNull(deserializedReq.getStateVisitedEvent());
		assertEqualAsSets(ssn.getTraversedStates(), deserializedSsn.getTraversedStates());
		assertEqualAsSets(ssn.getTraversedTests(), deserializedSsn.getTraversedTests());
		req2.commit();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void  crossSchemaTest() throws Exception {
				
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		Schema schema1 = core.getSchema();
		final VariantCoreSession ssn = core.getSession("foo2", true).getBody();
		State state1 = schema1.getState("state1");
		final VariantCoreStateRequest req = ssn.targetForState(state1);
		req.commit();  // Saves the session.

		Thread.sleep(5);
		
		// Core Session store does not guarantee idempotency any more.  
		// Only client side session wrappter is idempotent.
		//final VariantCoreSession ssn2 = core.getSession("foo2", true).getBody();
		//assertEquals("getSession() is not idempotent", ssn1, ssn2);
	    
	    // new schema.
		response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
	    final Schema schema2 = core.getSchema();
	    assertNotEquals(schema1.getId(), schema2.getId());
	    
	    // can't get session with the same sessionId because schema changed.
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				core.getSession("foo2", false); 
			}
		}.assertThrown(Error.RUN_SCHEMA_MODIFIED, schema2.getId(), schema1.getId());
		
	    // ditto, even with recreate
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				core.getSession("foo2", true); 
			}
		}.assertThrown(Error.RUN_SCHEMA_MODIFIED, schema2.getId(), schema1.getId());

		// ditto, target a session created by older schema to a state in a newer schema;
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				ssn.targetForState(schema2.getState("state1"));
			}
		}.assertThrown(Error.RUN_SCHEMA_MODIFIED, schema2.getId(), schema1.getId());

		// ditto, can't commit
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				req.commit();  // Saves the session.
			}
		}.assertThrown(Error.RUN_SCHEMA_MODIFIED, schema2.getId(), schema1.getId());
	    
		// new API
		core = rebootApi();
		assertNull(core.getSchema());
		
		response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		Schema schema3 = core.getSchema();
		assertNotEquals(schema2.getId(), schema3.getId());
		
		VariantCoreSession ssn2 = core.getSession("foo2", true).getBody(); // should be a new session because api's changed
		assertEquals("foo2", ssn2.getId());
		assertNull(ssn2.getStateRequest());
		assertEquals(0,ssn2.getTraversedStates().size());
		assertEquals(0, ssn2.getTraversedTests().size());
	    assertTrue(ssn.creationTimestamp() < ssn2.creationTimestamp());
		
	}

	/**
	 * Attributes are not supported in core session.
	 * @throws Exception
	 */
	@Test
	public void attributesTest() throws Exception {
		
		ParserResponse response = core.parseSchema(ParserDisjointOkayTest.SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		final VariantCoreSession ssn = core.getSession("foo", true).getBody();

		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				ssn.setAttribute("foo", new Object());
			}
		}.assertThrown(Error.RUN_METHOD_UNSUPPORTED);

		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				ssn.getAttribute("foo");
			}
		}.assertThrown(Error.RUN_METHOD_UNSUPPORTED);

	}

}
