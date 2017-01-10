package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.variant.client.Connection;
import com.variant.client.Connection.Status;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

public class SessionTest extends BaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
	
	/**
	 */
	//@org.junit.Test
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
		assertEquals(System.currentTimeMillis(), ssn1.creationTimestamp(), 1);
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
	//@org.junit.Test
	public void expirationTest() throws Exception {
		
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
		
		for (int i = 0; i < sessions.length; i++) {
			assertTrue(sessions[i].isExpired());
		}

	}
	
	/**
	 */
	//@org.junit.Test
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

	/**
	 */
	@org.junit.Test
	public void targetingTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());

		// Via SID tracker, create.
		String sid = newSid();
		Session ssn = conn.getOrCreateSession(sid);
		assertNotNull(ssn);
		assertEquals(sid, ssn.getId());

	   	Schema schema = conn.getSchema();
	   	State state1 = schema.getState("state1");
	   	Test test1 = schema.getTest("test1");
	   	//Test test2 = schema.getTest("test2");
	   	//Test test3 = schema.getTest("test3");
	   	//Test test4 = schema.getTest("test4");
	   	//Test test5 = schema.getTest("test5");
	   	//Test test6 = schema.getTest("test6");

	   	ssn.targetForState(state1);
	   	
		
	}
}
