package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Connection.Status;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.core.schema.State;

public class SessionTest extends BaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
	
	/**
	 */
	@org.junit.Test
	public void noSessionIdInTrackerTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		String sid = newSid();
		
		// By session ID
		Session ssn1 = conn.getSessionById(sid);
		assertNull(ssn1);

		// Via SID tracker, no create.
		ssn1 = conn.getSession(sid);
		assertNull(ssn1);

		// Via SID tracker, create.
		ssn1 = conn.getOrCreateSession(sid);
		assertNotNull(ssn1);
		assertEquals(sid, ssn1.getId());
		assertEquals(System.currentTimeMillis(), ssn1.getCreateDate().getTime(), 1);
		assertEquals(conn, ssn1.getConnection());
		assertTrue(ssn1.getDisqualifiedTests().isEmpty());
		assertTrue(ssn1.getTraversedStates().isEmpty());
		assertTrue(ssn1.getTraversedTests().isEmpty());		

		// Get same session by SID
		Session ssn2 = conn.getSessionById(sid);
		assertEquals(ssn1, ssn2);
		
		// Get same session via SID tracker
		ssn2 = conn.getSession(sid);
		assertEquals(ssn1, ssn2);	
	}
	
	/**
	 */
	@org.junit.Test
	public void sessionExpireTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		
		Session[] sessions = new Session[100];
		for (int i = 0; i < sessions.length; i++) {
			String sid = newSid();
			sessions[i] = conn.getOrCreateSession(sid);
			assertNotNull(sessions[i]);
		}
		
		for (int i = 0; i < sessions.length; i++) {
			assertFalse(sessions[i].isExpired());
		}

		assertEquals(1000, sessions[0].getTimeoutMillis());
		// Let vacuum thread a chance to run.
		Thread.sleep(2000);
		
		final State state2 = conn.getSchema().getState("state2");
		for (int i = 0; i < sessions.length; i++) {
			assertTrue(sessions[i].isExpired());
			final Session ssn = sessions[i];
			new ClientUserExceptionInterceptor() {
				@Override public void toRun() {
					ssn.targetForState(state2);
				}
				@Override public void onThrown(ClientException.User e) {
					assertEquals(ClientUserError.SESSION_EXPIRED, e.getError());
				}
			}.assertThrown(SessionExpiredException.class);

		}
			
	}

	/**
	 */
	@org.junit.Test
	public void connectionClosedLocallyTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		
		String sid = newSid();
		final Session ssn = conn.getOrCreateSession(sid);
		final State state2 = conn.getSchema().getState("state2");
		conn.close();
		assertEquals(Connection.Status.CLOSED_BY_CLIENT, conn.getStatus());
		new ClientUserExceptionInterceptor() {
			@Override public void toRun() {
				ssn.targetForState(state2);
			}
		}.assertThrown(ConnectionClosedException.class);
		
	}

	/**
	 * Session expires too soon. See bug https://github.com/getvariant/variant/issues/67
	 */
	//@org.junit.Test
	public void connectionClosedRemotelyTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		
		String sid = newSid();
		final Session ssn = conn.getOrCreateSession(sid);
		final State state2 = conn.getSchema().getState("state2");

		server.restart();

		assertEquals(Status.OPEN, conn.getStatus());
		/*
		new ClientUserExceptionInterceptor() {
			@Override public void toRun() {
				ssn.targetForState(state2);
			}
		}.assertThrown(SessionExpiredException.class);
		assertEquals(Status.CLOSED_BY_SERVER, conn.getStatus());
		*/
		ssn.targetForState(state2);
	}

	/**
	 */
	@org.junit.Test
	public void attributesTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());

		// Via SID tracker, create.
		String sid = newSid();
		Session ssn = conn.getOrCreateSession(sid);
		assertNotNull(ssn);
		assertEquals(sid, ssn.getId());
		
		assertNull(ssn.getAttribute("foo"));
		assertNull(ssn.clearAttribute("foo"));
		String attr = "VALUE";
		ssn.setAttribute("foo", attr);
		assertEquals(attr, ssn.getAttribute("foo"));
		assertEquals(attr, ssn.clearAttribute("foo"));
		assertNull(ssn.getAttribute("foo"));
		assertNull(ssn.clearAttribute("foo"));
	}

}
