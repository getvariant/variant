package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.impl.ClientUserError;
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
	 * Start the server with long enough session expiration
	 */
	public TraceEventsTest() {
		// We need sessions to survive the wait for events.
		long ssnTimeoutSec = EVENT_WRITER_MAX_DELAY / 1000 + 3;
		restartServer(CollectionsUtils.pairsToMap(new Pair<String,String>("variant.session.timeout", String.valueOf(ssnTimeoutSec))));
	}
	
	/**
	 * State visited event.
	 */
	@org.junit.Test
	public void svoTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");		

		// New session.
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
		System.out.println(event);
		assertEquals(TraceEvent.SVE_NAME, event.name);
		assertEqualAsSets(CollectionsUtils.pairsToMap(new Pair("$STATE", "state1"), new Pair("sve2 atr key", "sve2 attr value")), event.attributes);
		assertEquals(5, event.eventExperiences.size());
		assertFalse(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test1")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test2")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test3")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test4")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test5")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test6")));
		
		// Commit back in ssn1. No extra sve event should be written.
		assertTrue(req2.isCommitted());
		assertFalse(req1.isCommitted());
		req1.commit();
		Thread.sleep(EVENT_WRITER_MAX_DELAY);
		assertEquals(1, new TraceEventReader().read(e -> e.sessionId.equals(sid)).size());

	}

	/**
	 * State visited event cannot be explicitly triggered
	 */
	@org.junit.Test
	public void svеTriggerTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");		

		// New session.
		String sid = newSid();
		Session ssn = conn.getOrCreateSession(sid);
	   	Schema schema = ssn.getSchema();
	   	State state1 = schema.getState("state1");
	   	StateRequest req = ssn.targetForState(state1);
	   	
	   	// Cannot explicitly trigger an SVE
		new ClientExceptionInterceptor() {
			@Override public void toRun() {
				ssn.triggerTraceEvent(req.getStateVisitedEvent());
			}
			@Override public void onThrown(VariantException e) {
				assertEquals(ClientUserError.CANNOT_TRIGGER_SVE, e.getError());
			}
		}.assertThrown(VariantException.class);

	}

}
