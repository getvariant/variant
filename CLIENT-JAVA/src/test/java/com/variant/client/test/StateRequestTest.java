package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.StateNotInstrumentedException;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.core.VariantEvent;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public class StateRequestTest extends ClientBaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
	
	public StateRequestTest() throws Exception {
		startServer();
	}
	
	/**
	 */
	@org.junit.Test
	public void noStabilTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");		

		// Via SID tracker, create.
		String sid = newSid();
		Session ssn = conn.getOrCreateSession(sid);
		assertNotNull(ssn);
		assertEquals(sid, ssn.getId());

	   	Schema schema = ssn.getSchema();
	   	State state1 = schema.getState("state1");
	   	//State state2 = schema.getState("state2");
	   	final Test test1 = schema.getTest("test1");
	   	Test test2 = schema.getTest("test2");
	   	Test test3 = schema.getTest("test3");
	   	Test test4 = schema.getTest("test4");
	   	Test test5 = schema.getTest("test5");
	   	Test test6 = schema.getTest("test6");

	   	final StateRequest req = ssn.targetForState(state1);
	   	assertNotNull(req);
	   	assertEquals(req, ssn.getStateRequest());
		assertEquals(5, req.getLiveExperiences().size());

		new ClientExceptionInterceptor() {
			@Override public void toRun() {
				req.getLiveExperience(test1);
			}
			@Override public void onThrown(ClientException e) {
				assertEquals(ServerError.STATE_NOT_INSTRUMENTED_BY_TEST, e.getError());
			}
		}.assertThrown(StateNotInstrumentedException.class);
		
		Experience e2 = req.getLiveExperience(test2);
		assertNotNull(e2);
		Experience e3 = req.getLiveExperience(test3);
		assertNotNull(e3);
		Experience e4 = req.getLiveExperience(test4);
		assertNotNull(e4);
		Experience e5 = req.getLiveExperience(test5);
		assertNotNull(e5);
		Experience e6 = req.getLiveExperience(test6);
		assertNotNull(e6);
	   	
		assertFalse(req.isCommitted());
		
		// On occasion, we may get a trivial resolution and these will fail ????
		assertNotNull(req.getResolvedParameters().get("path"));
		// assertNotNull(req.getResolvedStateVariant());     See #119. Should never return null.        

		assertEquals(ssn, req.getSession());
		assertEquals(state1, req.getState());
		VariantEvent event = req.getStateVisitedEvent();
		assertNotNull(event);
		assertEquals(StateVisitedEvent.EVENT_NAME, event.getName());
		assertEquals(req.getState().getName(), event.getValue());
		assertEquals(ssn.getCreateDate().getTime(), event.getCreateDate().getTime(), 10);
		assertTrue(event.getParameterMap().isEmpty());
				
		assertTrue(req.commit());
		assertTrue(req.isCommitted());
		assertNull(req.getStateVisitedEvent());		
		
		// No-op.
		assertFalse(req.commit());
		
		// Reget the session -- should not change anything.
		Session ssn2 = conn.getSession(sid);
		assertEquals(ssn, ssn2);
		StateRequest req2 = ssn2.getStateRequest();
		assertEquals(req, req2);  // << The problem probably is that session rewrap doesn't do request rewrap (see StateRequestImpl:104)
		assertTrue(req2.isCommitted());

	}
	
	/**
	 */
	@org.junit.Test
	public void deterministicTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");
		
		// Some session, just to get the schema.
		Schema schema = conn.getOrCreateSession("foo").getSchema();
		
		// Via SID tracker, create.
		String sid = newSid();
		Object[] userData = userDataForSimpleIn(sid, schema, "test4.C", "test5.C");
		Session ssn = conn.getOrCreateSession(userData);
		assertNotNull(ssn);
		assertEquals(sid, ssn.getId());

	   	State state2 = schema.getState("state2");
	   	Test test1 = schema.getTest("test1");
	   	Test test2 = schema.getTest("test2");
	   	Test test3 = schema.getTest("test3");
	   	Test test4 = schema.getTest("test4");
	   	Test test5 = schema.getTest("test5");
	   	Test test6 = schema.getTest("test6");

	   	final StateRequest req = ssn.targetForState(state2);
	   	assertNotNull(req);
		assertEquals(6, req.getLiveExperiences().size());

		assertEquals(experience(schema, "test1.A"), req.getLiveExperience(test1));
		assertEquals(experience(schema, "test2.A"), req.getLiveExperience(test2));
		assertEquals(experience(schema, "test3.A"), req.getLiveExperience(test3));
		assertEquals(experience(schema, "test4.C"), req.getLiveExperience(test4));
		assertEquals(experience(schema, "test5.C"), req.getLiveExperience(test5));
		assertNotNull(req.getLiveExperience(test6));  // Can be anything.

		assertEquals("/path/to/state2/test4.C+test5.C", req.getResolvedParameters().get("path"));
		assertNotNull(req.getResolvedStateVariant());
		
		assertTrue(req.commit());
		assertTrue(req.isCommitted());
		assertNull(req.getStateVisitedEvent());		
		// No-op.
		assertFalse(req.commit());
		
	}

	/**
	 */
	@org.junit.Test
	public void commitTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");

		// Some session, just to get the schema.
		Schema schema = conn.getOrCreateSession("foo").getSchema();

		// Via SID tracker, create.
		String sid = newSid();
		Object[] userData = userDataForSimpleIn(sid, schema, "test4.C", "test5.C");
		Session ssn1 = conn.getOrCreateSession(userData);
		assertNotNull(ssn1);
		assertEquals(sid, ssn1.getId());

	   	final State state2 = schema.getState("state2");
	   	final State state3 = schema.getState("state3");
	   	
	   	final StateRequest req1 = ssn1.targetForState(state2);
		
		assertTrue(req1.commit());
		assertTrue(req1.isCommitted());

		// Reget the session and try targeting again -- should not work.
		final Session ssn2 = conn.getOrCreateSession(userData);
		assertNotNull(ssn2);
		assertEquals(ssn1, ssn2);
		final StateRequest req2 = ssn2.getStateRequest();
		assertEquals(req1, req2);
		assertFalse(req1.commit());
		assertTrue(req1.isCommitted());
		assertFalse(req2.commit());
		assertTrue(req2.isCommitted());
		
		assertNotNull(ssn1.targetForState(state3));
		
		new ClientExceptionInterceptor() {
			@Override public void toRun() {
			   	ssn2.targetForState(state3);
			}
			@Override public void onThrown(ClientException e) {
				assertEquals(ClientUserError.ACTIVE_REQUEST, e.getError());
			}
		}.assertThrown(ClientUserError.ACTIVE_REQUEST);

	}

	/**
	 */
	@org.junit.Test
	public void sessionExpiredTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");		

		String sid = newSid();
		final Session ssn = conn.getOrCreateSession(sid);
		assertFalse(ssn.isExpired());
	
	   	Schema schema = ssn.getSchema();
	   	State state2 = schema.getState("state2");
	   	final StateRequest req = ssn.targetForState(state2);
	   	
		assertEquals(1000, ssn.getTimeoutMillis());
		// Let vacuum thread a chance to run.
		Thread.sleep(2000);
		
		assertTrue(ssn.isExpired());
		new ClientExceptionInterceptor() {
			@Override public void toRun() {
				req.commit();
			}
			@Override public void onThrown(ClientException e) {
				assertEquals(ServerError.SessionExpired, e.getError());
			}
		}.assertThrown(SessionExpiredException.class);
	}
	
	/**
	 * Petclinic schema defines a qual and a targeting hook which will fail,
	 * unless we create "user-agent" session attributes.
	 */
	@org.junit.Test
	public void targetingHookExceptionTest() throws Exception {
		
		Connection conn = client.connectTo("petclinic");		

		String sid = newSid();
		final Session ssn = conn.getOrCreateSession(sid);	
		final Schema schema = ssn.getSchema();
		
		// Targeting and qual hooks will throw exceptions because they
		// expect 'user-agent' attribute
	   	new ClientExceptionInterceptor() {
			@Override public void toRun() {
				ssn.targetForState(schema.getState("newOwner"));
			}
			@Override public void onThrown(ClientException e) {
				assertEquals(ServerError.HOOK_UNHANDLED_EXCEPTION, e.getError());
			}
		}.assertThrown();

		assertNull(ssn.getStateRequest());
		assertTrue(ssn.getTraversedStates().isEmpty());
		assertTrue(ssn.getTraversedTests().isEmpty());
		assertTrue(ssn.getDisqualifiedTests().isEmpty());
		
		// Set the attribute and target. 
		ssn.setAttribute("user-agent", "Any string");
		StateRequest req = ssn.targetForState(schema.getState("newOwner"));
		assertEquals(req, ssn.getStateRequest());
		assertTrue(req.commit());
		assertTrue(req.isCommitted());
	}
	
	@org.junit.Test
	public void targetFromParallelConnectionsTest() throws Exception {
		
		Connection conn1 = client.connectTo("big_conjoint_schema");		

		String sid = newSid();
		Session ssn1 = conn1.getOrCreateSession(sid);	
		Schema schema1 = ssn1.getSchema();
		StateRequest req1 = ssn1.targetForState(schema1.getState("state3"));

		Connection conn2 = client.connectTo("big_conjoint_schema");		
	   	Session ssn2 = conn2.getSessionById(sid);
	   	assertNotNull(ssn2);
		Schema schema2 = ssn2.getSchema();
	   	
	   	assertEquals(schema2.getId(), schema1.getId());
	   	
	   	
	   	StateRequest req2 = ssn2.getStateRequest();
	   	assertNotNull(req2);
	   	assertNotEquals(req2, req1);
	   	assertEquals(ssn2, req2.getSession());

	   	assertFalse(req1.isCommitted());
	   	assertFalse(req2.isCommitted());
	   	assertEqualAsSets(req2.getLiveExperiences(), req1.getLiveExperiences());
	   	assertEqualAsSets(req2.getResolvedParameters(), req1.getResolvedParameters());
	   	assertEquals(req2.getResolvedStateVariant().toString(), req1.getResolvedStateVariant().toString());
	   	assertEquals(req2.getStateVisitedEvent().toString(), req1.getStateVisitedEvent().toString());
	   	
	   	// Commit in conn2
	   	assertTrue(req2.commit());
	   	
	   	// Must be reflected in conn1
	   	assertTrue(req1.isCommitted());
	}

}
