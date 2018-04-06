package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static com.variant.core.ConnectionStatus.*;

import java.util.LinkedList;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
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
	//@org.junit.Test
	public void noSessionIdInTrackerTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(OPEN, conn.getStatus());
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
	//@org.junit.Test
	public void sessionExpiredTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(OPEN, conn.getStatus());
		
		Session[] sessions = new Session[10];
		for (int i = 0; i < sessions.length; i++) {
			String sid = newSid();
			sessions[i] = conn.getOrCreateSession(sid);
			assertNotNull(sessions[i]);
			assertEquals(1000, sessions[i].getTimeoutMillis());
		}
		
		for (int i = 0; i < sessions.length; i++) {
			assertFalse(sessions[i].isExpired());
		}

		// Let sessions a chance to expire on the server.
		
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
   //@org.junit.Test
   public void sessionExpirationListenerTest() throws Exception {
      
      Connection conn = client.getConnection("big_covar_schema");    
      assertNotNull(conn);
      assertEquals(OPEN, conn.getStatus());
      final Session ssn = conn.getOrCreateSession(newSid());
      assertNotNull(ssn);
      final LinkedList<Integer> listenersRan = new LinkedList<Integer>();
      
      // Add two listeners, both of which should fire in the order added;
      conn.registerExpirationListener(
            new Connection.ExpirationListener() {
               @Override public void expired(Session session) {
            	  assertEquals(session, ssn);
            	  assertTrue(session.isExpired());
                  listenersRan.add(1);
               }
            }
      );

      conn.registerExpirationListener(
            new Connection.ExpirationListener() {
               @Override public void expired(Session session) {
             	  assertEquals(session, ssn);
             	  assertTrue(session.isExpired());
                  listenersRan.add(2);
               }
            }
      );

      assertFalse(ssn.isExpired());
      
      assertEquals(1000, ssn.getTimeoutMillis());
      // Let vacuum thread a chance to run.
      Thread.sleep(2000);

      assertTrue(ssn.isExpired());

      assertEquals(2, listenersRan.size());
      assertTrue(listenersRan.get(0) == 1);
      assertTrue(listenersRan.get(1) == 2);
   }
   
	/**
	 */
	//@org.junit.Test
	public void connectionClosedLocallyTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(OPEN, conn.getStatus());
		
		String sid = newSid();
		final Session ssn = conn.getOrCreateSession(sid);
		final State state2 = conn.getSchema().getState("state2");
		conn.close();
		assertEquals(CLOSED_BY_CLIENT, conn.getStatus());
		new ClientUserExceptionInterceptor() {
			@Override public void toRun() {
				ssn.targetForState(state2);
			}
		}.assertThrown(ConnectionClosedException.class);
		
	}

	/**
	 * Session expires too soon. See bug https://github.com/getvariant/variant/issues/67
	 * + bug 82, probably a dupe.
	 */
    @org.junit.Test
	public void connectionClosedRemotelyTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(OPEN, conn.getStatus());
		
		String sid = newSid();
		final Session ssn = conn.getOrCreateSession(sid);
		final State state2 = conn.getSchema().getState("state2");

		server.restart();

		assertEquals(OPEN, conn.getStatus());
		
		new ClientUserExceptionInterceptor() {
			@Override public void toRun() {
				ssn.targetForState(state2);
			}
		}.assertThrown(SessionExpiredException.class);
		assertEquals(CLOSED_BY_SERVER, conn.getStatus());
		
		//ssn.targetForState(state2);
	}

	/**
	 */
	//@org.junit.Test
	public void attributesTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(OPEN, conn.getStatus());

		// Via SID tracker, create.
		String sid = newSid();
		Session ssn = conn.getOrCreateSession(sid);
		assertNotNull(ssn);
		assertEquals(sid, ssn.getId());
		
		// Local checks.
		assertNull(ssn.getAttribute("foo"));
		assertNull(ssn.clearAttribute("foo"));
		String attr = "VALUE";
		ssn.setAttribute("foo", attr);
		assertEquals(attr, ssn.getAttribute("foo"));
		assertEquals(attr, ssn.clearAttribute("foo"));
		assertNull(ssn.getAttribute("foo"));
		
		// roundtrip to server.
	   	State state1 = conn.getSchema().getState("state1");
	   	StateRequest req = ssn.targetForState(state1);
	   	assertNotNull(req);
		assertEquals(attr, ssn.getAttribute("foo"));
		assertEquals(attr, ssn.clearAttribute("foo"));
		assertNull(ssn.getAttribute("foo"));
		assertNull(ssn.clearAttribute("foo"));

	}

}
