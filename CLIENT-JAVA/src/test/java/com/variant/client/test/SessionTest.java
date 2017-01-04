package com.variant.client.test;

import static org.junit.Assert.*;

import java.util.Random;

import com.variant.client.Connection;
import com.variant.client.Connection.Status;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.core.util.VariantStringUtils;

public class SessionTest extends BaseTestWithServer {

	private final VariantClient client = VariantClient.Factory.getInstance();
	private final Random random = new Random(System.currentTimeMillis());
	
	/**
	 * No session ID in tracker.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void noSessionIdInTrackerTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		String sessionId = VariantStringUtils.random64BitString(random);
		
		// By session ID
		Session ssn1 = conn.getSessionById(sessionId);
		assertNull(ssn1);

		// Via SID tracker, no create.
		ssn1 = conn.getSession(sessionId);
		assertNull(ssn1);

		// Via SID tracker, create.
		ssn1 = conn.getOrCreateSession(sessionId);
		assertNotNull(ssn1);
		assertEquals(sessionId, ssn1.getId());
		assertEquals(System.currentTimeMillis(), ssn1.creationTimestamp(), 1);
		assertEquals(conn, ssn1.getConnectoin());
		assertTrue(ssn1.getDisqualifiedTests().isEmpty());
		assertTrue(ssn1.getTraversedStates().isEmpty());
		assertTrue(ssn1.getTraversedTests().isEmpty());		

		// Get same session by SID
		Session ssn2 = conn.getSessionById(sessionId);
		assertEquals(ssn1, ssn2);
		
		// Get same session via SID tracker
		ssn2 = conn.getSession(sessionId);
		assertEquals(ssn1, ssn2);	
	}
	
	/**
	 * No session ID in tracker.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void expirationTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		
		Session[] sessions = new Session[100];
		for (int i = 0; i < sessions.length; i++) {
			String id = VariantStringUtils.random64BitString(random);
			sessions[i] = conn.getOrCreateSession(id);
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
	 * set/get session attributes.
	 * 
	 * @throws Exception
	 */
	@org.junit.Test
	public void attributesTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());

		// Via SID tracker, create.
		String sessionId = VariantStringUtils.random64BitString(random);
		Session ssn = conn.getOrCreateSession(sessionId);
		assertNotNull(ssn);
		assertEquals(sessionId, ssn.getId());
		
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
