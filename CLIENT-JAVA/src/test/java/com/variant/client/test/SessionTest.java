package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.impl.SessionImpl;
import com.variant.client.session.SessionIdTrackerSimple;
import com.variant.client.session.TargetingTrackerSimple;
import com.variant.client.test.util.ClientBaseTestWithServer;
import com.variant.core.impl.ServerError;
import com.variant.core.schema.State;

public class SessionTest extends ClientBaseTestWithServer {

	// Sole client
	private VariantClient client = new VariantClient.Builder()
			.withSessionIdTrackerClass(SessionIdTrackerSimple.class)
			.withTargetingTrackerClass(TargetingTrackerSimple.class)
			.build();
		
	/**
	 */
	@org.junit.Test
	public void noSessionIdInTrackerTest() throws Exception {
		
		restartServer();

		Connection conn = client.connectTo("monstrosity");		
		assertNotNull(conn);
		String sid = newSid();
		
		// By session ID
		assertFalse(conn.getSessionById(sid).isPresent());

		// Via SID tracker, no create.
		assertFalse(conn.getSession(sid).isPresent());

		// Via SID tracker, create.
		Session ssn1 = conn.getOrCreateSession(sid);
		assertNotNull(ssn1);
		assertNotEquals(sid, ssn1.getId());
		assertEquals(System.currentTimeMillis(), ssn1.getCreateDate().getTime(), 2);
		assertEquals(conn, ssn1.getConnection());
		assertTrue(ssn1.getDisqualifiedVariations().isEmpty());
		assertTrue(ssn1.getTraversedStates().isEmpty());
		assertTrue(ssn1.getTraversedVariations().isEmpty());		
		assertEquals("monstrosity", ssn1.getSchema().getMeta().getName());
		
		// Get same session by SID
		Session ssn2 = conn.getSessionById(ssn1.getId()).get();
		assertEquals(
				((SessionImpl)ssn1).getCoreSession().toJson(), 
				((SessionImpl)ssn2).getCoreSession().toJson());
		
		// Get same session via SID tracker
		ssn2 = conn.getSession(ssn1.getId()).get();
		assertEquals(
				((SessionImpl)ssn1).getCoreSession().toJson(), 
				((SessionImpl)ssn2).getCoreSession().toJson());
	}
	
	/**
	 */
	@org.junit.Test
	public void sessionExpiredTest() throws Exception {
		
		restartServer();

		Connection conn = client.connectTo("monstrosity");		
		assertNotNull(conn);
		
		Session[] sessions = new Session[10];
		for (int i = 0; i < sessions.length; i++) {
			String sid = newSid();
			sessions[i] = conn.getOrCreateSession(sid);
			assertNotNull(sessions[i]);
			assertEquals(1000, sessions[i].getTimeoutMillis());
			assertEquals("monstrosity", sessions[i].getSchema().getMeta().getName());
		}
		
		// Let sessions a chance to expire on the server.
		
		Thread.sleep(2000);
		
		final State state2 = sessions[0].getSchema().getState("state2").get();
		for (int i = 0; i < sessions.length; i++) {
			final Session ssn = sessions[i];
			
			new ClientExceptionInterceptor() {
				@Override public void toRun() {
					ssn.targetForState(state2);
				}
				@Override public void onThrown(VariantException e) {
					assertEquals(ServerError.SESSION_EXPIRED, e.getError());
				}
			}.assertThrown(SessionExpiredException.class);
			
		}
		
		// Ensure we change session id on create with an expired ID.
		for (int i = 0; i < sessions.length; i++) {
			Session ssn1 = sessions[i];
			assertFalse(conn.getSessionById(ssn1.getId()).isPresent());  // expired.
			Session ssn2 = conn.getOrCreateSession(ssn1.getId());
			assertNotEquals(ssn2.getId(), ssn1.getId());
		}
	}
   
	/**
	 */
	@org.junit.Test
	public void attributesTest() throws Exception {

		restartServer();

		// Open two parallel connections
		Connection conn1 = client.connectTo("monstrosity");		
		Connection conn2 = client.connectTo("monstrosity");		
		assertNotNull(conn1);
		assertNotNull(conn2);

		// Open a session.
		String sid = newSid();
		assertFalse(conn1.getSessionById(sid).isPresent());
		Session ssn1 = conn1.getOrCreateSession(sid);
		assertNotNull(ssn1);
		assertNotEquals(sid, ssn1.getId());
		Session ssn2 = conn2.getSessionById(ssn1.getId()).get();
		assertNotNull(ssn2);
		assertEquals(ssn1.getId(), ssn2.getId());
		assertEquals(
				((SessionImpl)ssn1).getCoreSession().toJson(), 
				((SessionImpl)ssn2).getCoreSession().toJson());
		
		// Set attribute in ssn1
		assertNull(ssn1.getAttributes().get("foo"));
		assertNull(ssn1.getAttributes().remove("foo"));
		String attr = "VALUE";
		ssn1.getAttributes().put("foo", attr);

		// read back in ssn1 and ssn2
		assertEquals(attr, ssn1.getAttributes().get("foo"));
		assertEquals(attr, ssn2.getAttributes().get("foo"));
		
		// Clear in ssn2
		assertEquals(attr, ssn2.getAttributes().remove("foo"));
		
		// Read back in ssn1 and ssn2
		assertNull(ssn1.getAttributes().get("foo"));
		assertNull(ssn2.getAttributes().get("foo"));

	}

}
