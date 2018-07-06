package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.UnknownSchemaException;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.core.impl.ServerError;
import com.variant.core.schema.State;

public class SessionTest extends ClientBaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
	
	/**
	 * 
	 * @throws Exception
	 */
	public SessionTest() throws Exception {
	   startServer();
	}
	
	/**
	 */
	@org.junit.Test
	public void noSessionIdInTrackerTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");		
		assertNotNull(conn);
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
		assertEquals("big_conjoint_schema", ssn1.getSchema().getName());
		
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
	public void sessionExpiredTest() throws Exception {
		
		Connection conn = client.connectTo("big_conjoint_schema");		
		assertNotNull(conn);
		
		Session[] sessions = new Session[10];
		for (int i = 0; i < sessions.length; i++) {
			String sid = newSid();
			sessions[i] = conn.getOrCreateSession(sid);
			assertNotNull(sessions[i]);
			assertEquals(1000, sessions[i].getTimeoutMillis());
			assertEquals("big_conjoint_schema", sessions[i].getSchema().getName());
		}
		
		for (int i = 0; i < sessions.length; i++) {
			assertFalse(sessions[i].isExpired());
		}

		// Let sessions a chance to expire on the server.
		
		Thread.sleep(2000);
		
		final State state2 = sessions[0].getSchema().getState("state2");
		for (int i = 0; i < sessions.length; i++) {
			assertTrue(sessions[i].isExpired());
			final Session ssn = sessions[i];
			new ClientUserExceptionInterceptor() {
				@Override public void toRun() {
					ssn.targetForState(state2);
				}
				@Override public void onThrown(ClientException e) {
					assertEquals(ServerError.SessionExpired, e.getError());
					assertTrue(ssn.isExpired());
				}
			}.assertThrown(SessionExpiredException.class);
		}
	}
   
	/**
	 */
	@org.junit.Test
	public void attributesTest() throws Exception {

		// Open two parallel connections
		Connection conn1 = client.connectTo("big_conjoint_schema");		
		Connection conn2 = client.connectTo("big_conjoint_schema");		
		assertNotNull(conn1);
		assertNotNull(conn2);

		// Open a session.
		String sid = newSid();
		assertNull(conn1.getSessionById(sid));
		Session ssn1 = conn1.getOrCreateSession(sid);
		assertNotNull(ssn1);
		assertEquals(sid, ssn1.getId());
		Session ssn2 = conn2.getSessionById(sid);
		assertNotNull(ssn2);
		assertEquals(sid, ssn2.getId());
		
		// Set attribute in ssn1
		assertNull(ssn1.getAttribute("foo"));
		assertNull(ssn1.clearAttribute("foo"));
		String attr = "VALUE";
		ssn1.setAttribute("foo", attr);

		// read back in ssn1 and ssn2
		assertEquals(attr, ssn1.getAttribute("foo"));
		assertEquals(attr, ssn2.getAttribute("foo"));
		
		// Clear in ssn2
		assertEquals(attr, ssn2.clearAttribute("foo"));
		
		// Read back in ssn1 and ssn2
		assertNull(ssn1.getAttribute("foo"));
		assertNull(ssn2.getAttribute("foo"));

	}

}
