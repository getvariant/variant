package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.core.VariantEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

public class TraceEventsTest extends ClientBaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
	
	public TraceEventsTest() throws Exception {
		restartServer();
	}
	
	/**
	 */
	@org.junit.Test
	public void implicitEvents() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");		

		// Via SID tracker, create.
		String sid = newSid();
		Session ssn1 = conn.getOrCreateSession(sid);

	   	Schema schema = ssn1.getSchema();
	   	State state1 = schema.getState("state1");
	   	//State state2 = schema.getState("state2");

	   	StateRequest req1 = ssn1.targetForState(state1);
	   	assertNotNull(req1);
	   	assertEquals(req1, ssn1.getStateRequest());
		assertEquals(5, req1.getLiveExperiences().size());
		
		VariantEvent event1 = req1.getStateVisitedEvent();
		assertEquals(0, event1.getParameterMap().size());

		event1.getParameterMap().put("foo", "bar");
		

		// Reget the session. Should have the event params.		
		assertTrue(req1.commit());

		Session ssn2 = conn.getSessionById(sid);
		assertEquals(ssn1, ssn2);
		StateRequest req2 = ssn2.getStateRequest();
		assertEquals(req1, req2); 
		assertTrue(req2.isCommitted());
		VariantEvent event2 = req2.getStateVisitedEvent();
		//assertEquals(event1, event2);  // Fails. Bug #158

		
	}

}
