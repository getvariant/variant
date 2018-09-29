package com.variant.client.test;

import static com.variant.core.StateRequestStatus.Committed;
import static com.variant.core.StateRequestStatus.InProgress;
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@org.junit.Test
	public void sveTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");		

		// New session.
		Session ssn1 = conn.getOrCreateSession(newSid());

		// Crete a session attribute
		ssn1.getAttributes().put("ssn1 attr key", "ssn1 attr value");
	   	Schema schema = ssn1.getSchema();
	   	State state1 = schema.getState("state1").get();
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
		Session ssn2 = conn.getSessionById(ssn1.getId());
		assertEquals(ssn1.getId(), ssn2.getId());
		StateRequest req2 = ssn2.getStateRequest().get();		
		assertEquals(InProgress, req2.getStatus());
		assertEquals("ssn1 attr value", ssn2.getAttributes().get("ssn1 attr key"));

		StateVisitedEvent event2 = (StateVisitedEvent) req2.getStateVisitedEvent();
		assertEquals(event1.getName(), event2.getName());
		// The SVE is recreated in a local copy, but their attributes are lost, except the $STATE attribute.
		assertEquals(1, event2.getAttributes().size());
		assertEquals("state1", event1.getAttribute("$STATE"));
		
		event2.setAttribute("sve2 atr key", "sve2 attr value");
		req2.commit();
		
		Thread.sleep(EVENT_WRITER_MAX_DELAY);
		List<TraceEventFromDatabase> events = new TraceEventReader().read(e -> e.sessionId.equals(ssn1.getId()));
		assertEquals(1, events.size());
		TraceEventFromDatabase event = events.get(0);
		//System.out.println(event);
		assertEquals(TraceEvent.SVE_NAME, event.name);
		assertEqualAsSets(
				CollectionsUtils.pairsToMap(new Pair("$STATE", "state1"), new Pair("$STATUS", "Committed"), new Pair("sve2 atr key", "sve2 attr value")), 
				event.attributes);
		assertEquals(5, event.eventExperiences.size());
		assertFalse(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test1")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test2")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test3")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test4")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test5")));
		assertTrue(event.eventExperiences.stream().anyMatch(ee -> ee.testName.equals("test6")));
		
		// Commit back in ssn1. No extra sve event should be written.
		assertEquals(Committed, req2.getStatus());
		assertEquals(InProgress, req1.getStatus());
		req1.commit();
		assertEquals(Committed, req1.getStatus());
		Thread.sleep(EVENT_WRITER_MAX_DELAY);
		assertEquals(1, new TraceEventReader().read(e -> e.sessionId.equals(ssn1.getId())).size());

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
	   	State state1 = schema.getState("state1").get();
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

	/**
	 * Custom events
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@org.junit.Test
	public void cutomEventTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");		

		// New session.
		Session ssn = conn.getOrCreateSession(newSid());
	   	Schema schema = ssn.getSchema();
	   	
	   	// Trigger on untargeted session: produces an orphan (no live experiences) event.
	   	ssn.triggerTraceEvent(TraceEvent.mkTraceEvent("custom1"));
	   	ssn.triggerTraceEvent(TraceEvent.mkTraceEvent("custom2", CollectionsUtils.pairsToMap(new Pair("foo", "bar"))));
	   	
	   	State state3 = schema.getState("state3").get();
	   	StateRequest req = ssn.targetForState(state3);
	   	req.getStateVisitedEvent().setAttribute("yin", "yang");
	   	req.commit();
	   	
		Thread.sleep(EVENT_WRITER_MAX_DELAY);
		List<TraceEventFromDatabase> events = new TraceEventReader().read(e -> e.sessionId.equals(ssn.getId()));
		assertEquals(3, events.size());
		//events.forEach(e -> System.out.println("***\n" + e));

		TraceEventFromDatabase custom1 = events.get(0);
		assertEquals("custom1", custom1.name);
		assertEquals(0, custom1.attributes.size());
		assertEquals(0, custom1.eventExperiences.size());
		
		TraceEventFromDatabase custom2 = events.get(1);
		assertEquals("custom2", custom2.name);
		assertEquals(1, custom2.attributes.size());
		assertEquals("bar", custom2.attributes.get("foo"));
		assertEquals(0, custom2.eventExperiences.size());

		TraceEventFromDatabase custom3 = events.get(2);
		assertEquals(TraceEvent.SVE_NAME, custom3.name);
		assertEquals(3, custom3.attributes.size());
		assertEquals("state3", custom3.attributes.get("$STATE"));
		assertEquals("Committed", custom3.attributes.get("$STATUS"));
		assertEquals("yang", custom3.attributes.get("yin"));
		assertEquals(5, custom3.eventExperiences.size());
	}

}
