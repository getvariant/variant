package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionAttributes;
import com.variant.client.SessionExpiredException;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.impl.SessionImpl;
import com.variant.client.test.util.ClientBaseTestWithServer;
import com.variant.core.error.ServerError;
import com.variant.core.schema.State;
import com.variant.core.util.CollectionsUtils;

public class SessionTest extends ClientBaseTestWithServer {

	// Sole client
	private VariantClient client = new VariantClient.Builder()
			.withSessionIdTrackerClass(SessionIdTrackerHeadless.class)
			.withTargetingTrackerClass(TargetingTrackerHeadless.class)
			.build();
		
	/**
	 */
	@org.junit.Test
	public void noSessionIdInTrackerTest() throws Exception {
		
		restartServer();

		Connection conn = client.connectTo("variant://localhost:5377/monstrosity");		
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
		assertEquals(System.currentTimeMillis(), ssn1.getTimestamp().toEpochMilli(), 2);
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

		Connection conn = client.connectTo("variant://localhost:5377/monstrosity");		
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
					assertEquals(ServerError.SESSION_EXPIRED, e.error);
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

		Connection conn1 = client.connectTo("variant://localhost:5377/monstrosity");		
		Session ssn1 = conn1.getOrCreateSession(newSid());
		assertNotNull(ssn1);

		Connection conn2 = client.connectTo("variant://localhost:5377/monstrosity");		
		Session ssn2 = conn2.getOrCreateSession(ssn1.getId());
		assertNotNull(ssn2);
	
		assertEquals(0, ssn1.getAttributes().size());
		assertEquals(0, ssn1.getAttributes().size());
		
		// Delete non-existent
		ssn1.getAttributes().remove("foo");
		assertEquals(0, ssn1.getAttributes().size());
		assertEquals(0, ssn1.getAttributes().size());
		
		// Set attribute in ssn1
		final String val1 = "VALUE1";		
		final String val2 = "VALUE2";		
		final String val3 = "VALUE3";		
		SessionAttributes attrs1 = ssn1.getAttributes();
		SessionAttributes attrs2 = ssn2.getAttributes();

		// Should be reflected in attrs1 but not attrs2
		attrs1.put("foo", val1);
		assertEquals(1, attrs1.size());
		assertEquals(0, attrs2.size());
		assertEquals(val1, attrs1.get("foo"));
		assertNull(attrs2.get("foo"));
		
		// Should be reflected in both sessions.
		assertEquals(val1, ssn1.getAttributes().get("foo"));
		assertEquals(val1, ssn2.getAttributes().get("foo"));
		
		// Clear the attribute in the other session.
		attrs1 = ssn1.getAttributes();
		attrs2 = ssn2.getAttributes();
		assertEquals(1, attrs1.size());
		assertEquals(1, attrs2.size());
		attrs2.remove("foo");
		
		// Should be reflected in attrs2 but not in attrs1
		assertEquals(1, attrs1.size());
		assertEquals(val1, attrs1.get("foo"));
		assertEquals(0, attrs2.size());

		// should be reflected in both sessions.
		assertEquals(0, ssn1.getAttributes().size());
		assertEquals(0, ssn2.getAttributes().size());

		// Add several.
		attrs1 = ssn1.getAttributes();
		attrs2 = ssn2.getAttributes();
		attrs1.putAll(CollectionsUtils.hashMap("one", val1, "two", val2, "three", val3));

		// Should be reflected in attrs1 but not attrs2
		assertEquals(3, attrs1.size());		
		assertEquals(0, attrs2.size());		
		assertEquals(val1, attrs1.get("one"));		
		assertEquals(val2, attrs1.get("two"));		
		assertEquals(val3, attrs1.get("three"));		

		attrs1 = ssn1.getAttributes();
		attrs2 = ssn2.getAttributes();

		assertEquals(3, attrs1.size());		
		assertEquals(3, attrs2.size());		

		// Delete 2
		attrs2.remove("three", "two", "non existent");
		assertEquals(1, attrs2.size());		
		assertEquals(val3, attrs1.get("three"));		
		assertEquals(3, attrs1.size());		
		
		// Add 2 more
		final String newVal1 = "VALUE1new";
		final String val4 = "VALUE4";
		final String val5 = "VALUE5";
		attrs2.putAll(CollectionsUtils.hashMap("one", newVal1, "four", val4, "five", val5));
		assertEquals(3, attrs1.size());		
		assertEquals(val1, attrs1.get("one"));		
		assertEquals(val2, attrs1.get("two"));		
		assertEquals(val3, attrs1.get("three"));		
		assertEquals(3, attrs2.size());
		assertEquals(newVal1, attrs2.get("one"));		
		assertEquals(val4, attrs2.get("four"));		
		assertEquals(val5, attrs2.get("five"));		
		
		// Refresh from server.
		attrs1 = ssn1.getAttributes();
		attrs2 = ssn2.getAttributes();
		
		assertEquals(3, attrs1.size());
		assertEquals(newVal1, attrs1.get("one"));		
		assertEquals(val4, attrs1.get("four"));		
		assertEquals(val5, attrs1.get("five"));
		
		assertEquals(3, attrs2.size());
		assertEquals(newVal1, attrs2.get("one"));		
		assertEquals(val4, attrs2.get("four"));		
		assertEquals(val5, attrs2.get("five"));		
		
		assertEquals(3, attrs1.values().size());
		assertEquals(3, attrs1.names().size());
		assertEquals(3, attrs1.entries().size());
	}

}
