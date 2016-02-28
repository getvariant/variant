package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.session.VariantSessionImpl;

public class SessionTest extends BaseTest {

	/**
	 * 
	 */
	@Test
	public void sessionCreationTest() throws Exception {
				

		VariantSession ssn = api.getSession("key");
		assertNotNull(ssn);
		assertNull(ssn.getStateRequest());
		String json = ((VariantSessionImpl)ssn).toJson();
		VariantSessionImpl deserializedSsn = VariantSessionImpl.fromJson(api, json);
		assertEquals("key", deserializedSsn.getId());
		assertNull(deserializedSsn.getStateRequest());
		assertEquals(0, deserializedSsn.getTraversedStates().size());
		assertEquals(0, deserializedSsn.getTraversedTests().size());
		
		VariantSession ssn2 = api.getSession("key");
		assertEquals(ssn, ssn2);
		assertNull(ssn.getStateRequest());
		assertNull(ssn2.getStateRequest());
		
		VariantSession ssn3 = api.getSession("another-key");
		assertNotEquals (ssn, ssn3);
		assertNull(ssn.getStateRequest());
		assertNull(ssn3.getStateRequest());
		json = ((VariantSessionImpl)ssn3).toJson();
		deserializedSsn = VariantSessionImpl.fromJson(api, json);
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
		VariantSession ssn = api.getSession("foo");
		assertNull(ssn.getStateRequest());
		VariantStateRequest req1 = api.dispatchRequest(ssn, schema.getState("state1"), "");
		assertNotNull(req1);
		assertEquals(req1, ssn.getStateRequest());
		assertNotNull(req1.getStateVisitedEvent());
		String json = ((VariantSessionImpl)ssn).toJson();
		VariantSessionImpl deserializedSsn = VariantSessionImpl.fromJson(api, json);
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
		api.commitStateRequest(req1, "");
		
		// Nothing is instrumented on state2
		VariantStateRequest req2 = api.dispatchRequest(ssn, schema.getState("state2"), "");
		assertNotNull(req2);
		assertNotEquals(req1, req2);
		assertNull(req2.getStateVisitedEvent());
		assertEquals(req2, ssn.getStateRequest());
		json = ((VariantSessionImpl)ssn).toJson();
		deserializedSsn = VariantSessionImpl.fromJson(api, json);
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
		api.commitStateRequest(req2, "");
	}
	
}
