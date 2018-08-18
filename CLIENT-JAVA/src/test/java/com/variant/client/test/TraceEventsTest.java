package com.variant.client.test;

import static org.junit.Assert.*;

import java.util.List;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.test.util.ClientBaseTestWithServer;
import com.variant.client.test.util.event.TraceEventFromDatabase;
import com.variant.client.test.util.event.TraceEventReader;
import com.variant.core.TraceEvent;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.Tuples.Pair;

public class TraceEventsTest extends ClientBaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
		
	/**
	 */
	@org.junit.Test
	public void implicitEvents() throws Exception {
		
		// We need sessions to survive the wait for events.
		long ssnTimeoutSec = EVENT_WRITER_MAX_DELAY / 1000 + 3;
		restartServer(CollectionsUtils.pairsToMap(new Pair<String,String>("variant.session.timeout", String.valueOf(ssnTimeoutSec))));

		Connection conn = client.connectTo("big_conjoint_schema");		

		// Via SID tracker, create.
		String sid = newSid();
		Session ssn1 = conn.getOrCreateSession(sid);
		
		// Crete a session attribute
		ssn1.setAttribute("ssn1 attr key", "ssn1 attr value");
	   	Schema schema = ssn1.getSchema();
	   	State state1 = schema.getState("state1");
	   	//State state2 = schema.getState("state2");

	   	StateRequest req1 = ssn1.targetForState(state1);
	   	assertNotNull(req1);
	   	assertEquals(req1, ssn1.getStateRequest());
		assertEquals(5, req1.getLiveExperiences().size());
		
		StateVisitedEvent event1 = (StateVisitedEvent) req1.getStateVisitedEvent();
		assertEquals(TraceEvent.SVE_NAME, event1.getName());
		assertEquals(1, event1.getAttributes().size());
		assertEquals("state1", event1.getAttribute("$STATE"));
		
		event1.setAttribute("foo", "bar");
		
		// Reget the session.  
		Session ssn2 = conn.getSessionById(sid);
		StateRequest req2 = ssn2.getStateRequest();		
		assertFalse(req2.isCommitted());
		assertEquals("ssn1 attr value", ssn2.getAttribute("ssn1 attr key"));

		StateVisitedEvent event2 = (StateVisitedEvent) req2.getStateVisitedEvent();
		assertEquals(event1.getName(), event2.getName());
		// The SVE is recreated in a local copy, but their attributes are lost, except the $STATE attribute.
		assertEquals(1, event2.getAttributes().size());
		assertEquals("state1", event1.getAttribute("$STATE"));
		
		event2.setAttribute("sve2 atr key", "sve2 attr value");
		req2.commit();
		
		Thread.sleep(EVENT_WRITER_MAX_DELAY);
		List<TraceEventFromDatabase> events = new TraceEventReader().read(e -> e.sessionId.equals(sid));
		assertEquals(1, events.size());
		TraceEventFromDatabase event = events.get(0);
		//System.out.println(event);
		assertEquals(TraceEvent.SVE_NAME, event.name);
		assertEqualAsSets(CollectionsUtils.pairsToMap(new Pair("$STATE", "state1"), new Pair("sve2 atr key", "sve2 attr value")), event.attributes);
		
		// Commit back in ssn1. No extra sve event should be written.
		assertTrue(req2.isCommitted());
		assertFalse(req1.isCommitted());
		req1.commit();
		Thread.sleep(EVENT_WRITER_MAX_DELAY);
		assertEquals(1, new TraceEventReader().read(e -> e.sessionId.equals(sid)).size());

	}

}
