package com.variant.client.test;

import static com.variant.core.StateRequestStatus.Committed;
import static com.variant.core.StateRequestStatus.Failed;
import static com.variant.core.StateRequestStatus.InProgress;
import static org.junit.Assert.*;

import java.util.List;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.impl.SchemaImpl;
import com.variant.client.test.util.ClientBaseTestWithServer;
import com.variant.client.test.util.event.TraceEventFromDatabase;
import com.variant.client.test.util.event.TraceEventReader;
import com.variant.core.TraceEvent;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;

public class StateRequestTest extends ClientBaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
		
	public StateRequestTest() {
		restartServer();
	}
	
	/**
	 */
	@org.junit.Test
	public void noStabilTest() throws Exception {
		
		Connection conn = client.connectTo("monstrosity");		

		// Via SID tracker, create.
		Session ssn = conn.getOrCreateSession(newSid());
		assertNotNull(ssn);

	   	Schema schema = ssn.getSchema();
	   	State state3 = schema.getState("state3").get();
	   	//State state2 = schema.getState("state2");
	   	Variation test1 = schema.getVariation("test1").get();
	   	Variation test2 = schema.getVariation("test2").get();
	   	Variation test3 = schema.getVariation("test3").get();
	   	Variation test4 = schema.getVariation("test4").get();
	   	Variation test5 = schema.getVariation("test5").get();
	   	Variation test6 = schema.getVariation("test6").get();

	   	final StateRequest req = ssn.targetForState(state3);
	   	assertNotNull(req);
	   	assertEquals(req, ssn.getStateRequest().get());
		assertEquals(5, req.getLiveExperiences().size());

		assertTrue(req.getLiveExperience(test1).isPresent());
		assertTrue(req.getLiveExperience(test2).isPresent());
		assertTrue(req.getLiveExperience(test3).isPresent());
		assertFalse(req.getLiveExperience(test4).isPresent());
		assertTrue(req.getLiveExperience(test5).isPresent());
		assertTrue(req.getLiveExperience(test6).isPresent());
	   			
		assertNotNull(req.getResolvedParameters().get("path"));

		assertEquals(ssn, req.getSession());
		assertEquals(state3, req.getState());
		StateVisitedEvent event = (StateVisitedEvent) req.getStateVisitedEvent();
		assertNotNull(event);
		assertEquals(TraceEvent.SVE_NAME, event.getName());
		assertEquals(1, event.getAttributes().size());
		assertEquals(state3.getName(), event.getAttribute("$STATE"));
			
		assertTrue(req.getStatus() == InProgress);
		req.commit();
		assertEquals(Committed, req.getStatus());
		assertNull(req.getStateVisitedEvent());		
		
		// No-op.
		req.commit();

		assertEquals(Committed, req.getStatus());

		// Reget the session -- should not change anything.
		Session ssn2 = conn.getSession(ssn.getId()).get();
		StateRequest req2 = ssn2.getStateRequest().get();
		assertEquals(Committed, req2.getStatus());

		// Can't fail
		new ClientExceptionInterceptor() {
			@Override public void toRun() {
				req.fail();
			}
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.CANNOT_FAIL, e.getError());
			}
		}.assertThrown(VariantException.class);
	}
	
	/**
	 */
	@org.junit.Test
	public void deterministicTest() throws Exception {
		
		Connection conn = client.connectTo("monstrosity");
		
		// Via SID tracker, create.
		String sid = newSid();
		Object[] userData = new Object[] {sid, "test6.B", "test4.C", "test5.C"};
		Session ssn = conn.getOrCreateSession(userData);
		assertNotNull(ssn);
		assertNotEquals(sid, ssn.getId());
		assertEquals(0, ssn.getTraversedVariations().size());
		Schema schema = ssn.getSchema();
	   	State state2 = schema.getState("state2").get();
	   	Variation test1 = schema.getVariation("test1").get();
	   	Variation test2 = schema.getVariation("test2").get();
	   	Variation test3 = schema.getVariation("test3").get();
	   	Variation test4 = schema.getVariation("test4").get();
	   	Variation test5 = schema.getVariation("test5").get();
	   	Variation test6 = schema.getVariation("test6").get();

	   	final StateRequest req = ssn.targetForState(state2);
	   	assertNotNull(req);
		assertEquals(5, req.getLiveExperiences().size());

		// test1 is disjoint with test5 => has to target to control.
		assertEquals(test1.getExperience("A").get(), req.getLiveExperience(test1).get());
		// test2 is disjoint with test4 => has to target to control. 
		assertEquals(test2.getExperience("A").get(), req.getLiveExperience(test2).get());
		// test3 is uninstrumented
		assertFalse(req.getLiveExperience(test3).isPresent());
		// must honor tracker
		assertEquals(test4.getExperience("C").get(), req.getLiveExperience(test4).get());
		// must honor tracker.
		assertEquals(test5.getExperience("C").get(), req.getLiveExperience(test5).get());
		// test6 is conjoint with both => can target to anything.
		assertEquals(test6.getExperience("B").get(), req.getLiveExperience(test6).get());
		
		assertEquals("/path/to/state2", req.getResolvedParameters().get("path"));
		assertTrue(req.getResolvedStateVariant().isPresent());
		
		assertEquals(InProgress, req.getStatus());
		req.commit();
		assertEquals(Committed, req.getStatus());
		assertNull(req.getStateVisitedEvent());		
		// No-op.
		req.commit();

	}

	/**
	 */
	@org.junit.Test
	public void commitTest() throws Exception {
		
		Connection conn = client.connectTo("monstrosity");

		// Some session, just to get the schema.
		Schema schema = conn.getOrCreateSession("foo").getSchema();

		// Via SID tracker, create.
		String sid = newSid();
		Object[] userData = new Object[] {sid, "test4.C", "test5.C"};
		Session ssn1 = conn.getOrCreateSession(userData);
		assertNotNull(ssn1);
		assertNotEquals(sid, ssn1.getId());

	   	final State state2 = schema.getState("state2").get();
	   	final State state3 = schema.getState("state3").get();
	   	
	   	final StateRequest req1 = ssn1.targetForState(state2);
		
		assertEquals(InProgress, req1.getStatus());
		req1.commit();
		assertEquals(Committed, req1.getStatus());

		// Reget the session and try targeting again -- should not work.
		final Session ssn2 = conn.getOrCreateSession(ssn1.getId());
		assertNotNull(ssn2);
		assertNotEquals(ssn1, ssn2); 
		assertEquals(ssn1.getId(), ssn2.getId());
		final StateRequest req2 = ssn2.getStateRequest().get();
		assertNotEquals(req1, req2);
		
		assertEquals(Committed, req1.getStatus());
		assertEquals(Committed, req2.getStatus());
		req2.commit();
		assertEquals(Committed, req2.getStatus());
		
		assertNotNull(ssn1.targetForState(state3));

		new ClientExceptionInterceptor() {
			@Override public void toRun() {
			   	ssn2.targetForState(state3);
			}
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.ACTIVE_REQUEST, e.getError());
			}
		}.assertThrown();

		ssn1.getStateRequest().get().commit();
		
		// Fail in parallel session should not go through
		new ClientExceptionInterceptor() {
			@Override public void toRun() {
				ssn2.getStateRequest().get().fail();
			}
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.CANNOT_FAIL, e.getError());
			}
		}.assertThrown(VariantException.class);
		
	}

	/**
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void failTest() throws Exception {
		
		Connection conn = client.connectTo("monstrosity");

		Session ssn1 = conn.getOrCreateSession(newSid());
		Schema schema = ssn1.getSchema();

		assertNotNull(ssn1);

		State state2 = schema.getState("state2").get();
	   	
	   	StateRequest req1 = ssn1.targetForState(state2);
	   	req1.getStateVisitedEvent().setAttribute("foo", "bar");
	   	
	   	assertEquals(InProgress, req1.getStatus());
	   	
	   	StateRequest req2 = conn.getSessionById(ssn1.getId()).get().getStateRequest().get();
	   	
	   	req1.fail();
	   	assertEquals(Failed, req1.getStatus());
	   	
		// Try committing in same session.
		new ClientExceptionInterceptor() {
			@Override public void toRun() {
				req1.commit();
			}
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.CANNOT_COMMIT, e.getError());
			}
		}.assertThrown(VariantException.class);

		// Try committing in a parallel session.
		new ClientExceptionInterceptor() {
			@Override public void toRun() {
				req2.commit();
			}
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.CANNOT_COMMIT, e.getError());
			}
		}.assertThrown(VariantException.class);

		Thread.sleep(EVENT_WRITER_MAX_DELAY);
		List<TraceEventFromDatabase> events = new TraceEventReader().read(e -> e.sessionId.equals(ssn1.getId()));
		assertEquals(1, events.size());
		TraceEventFromDatabase event = events.get(0);
		assertEquals("$STATE_VISIT", event.name);
		assertEquals(3, event.attributes.size());
		assertEquals("state2", event.attributes.get("$STATE"));
		assertEquals("Failed", event.attributes.get("$STATUS"));
		assertEquals("bar", event.attributes.get("foo"));		
	}
	
	/**
	 */
	@org.junit.Test
	public void sessionExpiredTest() throws Exception {
		
		Connection conn = client.connectTo("monstrosity");		

		String sid = newSid();
		final Session ssn = conn.getOrCreateSession(sid);
	
	   	Schema schema = ssn.getSchema();
	   	State state2 = schema.getState("state2").get();
	   	final StateRequest req = ssn.targetForState(state2);
	   	
		assertEquals(1000, ssn.getTimeoutMillis());
		// Let vacuum thread a chance to run.
		Thread.sleep(2000);
		
		new ClientExceptionInterceptor() {
			@Override public void toRun() {
				req.commit();
			}
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.SESSION_EXPIRED, e.getError());
			}
		}.assertThrown(SessionExpiredException.class);
	}
	
	/**
	 * Petclinic schema defines a qual and a targeting hook which will fail,
	 * unless we create "user-agent" session attributes.
	 */
	//@org.junit.Test
	public void targetingHookExceptionTest() throws Exception {
		
		Connection conn = client.connectTo("petclinic");		

		String sid = newSid();
		final Session ssn = conn.getOrCreateSession(sid);	
		final Schema schema = ssn.getSchema();
		
		// Targeting and qual hooks will throw exceptions because they
		// expect 'user-agent' attribute
	   	new ClientExceptionInterceptor() {
			@Override public void toRun() {
				ssn.targetForState(schema.getState("vets").get());
			}
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.HOOK_UNHANDLED_EXCEPTION, e.getError());
			}
		}.assertThrown();		
		
		assertFalse(ssn.getStateRequest().isPresent());
		assertTrue(ssn.getTraversedStates().isEmpty());
		// See # 193
		assertEquals(2, ssn.getTraversedVariations().size());
		assertTrue(ssn.getDisqualifiedVariations().isEmpty());
		
		// Set the attribute and target. 
		ssn.getAttributes().put("user-agent", "Any string");
		StateRequest req = ssn.targetForState(schema.getState("newVisit").get());
		assertEquals(req, ssn.getStateRequest().get());
		assertEquals(InProgress, req.getStatus());
		req.commit();
		assertEquals(Committed, req.getStatus());
	}
	
	@org.junit.Test
	public void targetFromParallelConnectionsTest() throws Exception {
		
		Connection conn1 = client.connectTo("monstrosity");		

		Session ssn1 = conn1.getOrCreateSession(newSid());	
		Schema schema1 = ssn1.getSchema();
		StateRequest req1 = ssn1.targetForState(schema1.getState("state3").get());

		Connection conn2 = client.connectTo("monstrosity");		
	   	Session ssn2 = conn2.getSessionById(ssn1.getId()).get();
	   	assertNotNull(ssn2);
		Schema schema2 = ssn2.getSchema();
	   	
	   	assertEquals(((SchemaImpl)schema2).getId(), ((SchemaImpl)schema1).getId());
	   	
	   	
	   	StateRequest req2 = ssn2.getStateRequest().get();
	   	assertNotNull(req2);
	   	assertNotEquals(req2, req1);
	   	assertEquals(ssn2, req2.getSession());

		assertEquals(InProgress, req1.getStatus());
		assertEquals(InProgress, req2.getStatus());
	   	assertEqualAsSets(req2.getLiveExperiences(), req1.getLiveExperiences());
	   	assertEqualAsSets(req2.getResolvedParameters(), req1.getResolvedParameters());
	   	assertEquals(req2.getResolvedStateVariant().toString(), req1.getResolvedStateVariant().toString());
	   	assertEquals(req2.getStateVisitedEvent().toString(), req1.getStateVisitedEvent().toString());
	   	
	   	// Commit in req2
		assertEquals(InProgress, req2.getStatus());
	   	req2.commit();
		assertEquals(Committed, req2.getStatus());
	   	
	   	// req1 doesn't know about it
		assertEquals(InProgress, req1.getStatus());
	   	req1.commit();
		assertEquals(Committed, req1.getStatus());

	}

}
