package com.variant.client.test;

import static com.variant.core.ConnectionStatus.CLOSED_BY_SERVER;
import static com.variant.core.ConnectionStatus.OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.SessionExpiredException;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.SessionImpl;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.util.IoUtils;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class SessionUndeployTest extends ClientBaseTestWithServerAsync {
	
	private int SESSIONS = 20;
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	/**
	 * Schema undeployed with a session timeout interval set to less than
	 * dirWatcherLatency, so that all sessions expire while we wait for
	 * server to detect the delete of the schema file. 
	 * Default server config.
	 */
	@org.junit.Test
	public void serverUndeploySessionTimeoutTest() throws Exception {

		// First connection to big_covar_schema
		final Connection conn1 = client.getConnection("big_covar_schema");		
		assertEquals(OPEN, conn1.getStatus());
		assertEquals(client, conn1.getClient());
		final Schema schema1 = conn1.getSchema();
		assertEquals("big_covar_schema", schema1.getName());

		// Second connection to big_covar_schema
		final Connection conn2 = client.getConnection("big_covar_schema");		
		assertEquals(OPEN, conn2.getStatus());
		assertEquals(client, conn2.getClient());
		final Schema schema2 = conn2.getSchema();
		assertEquals("big_covar_schema", schema2.getName());
		assertEquals(schema1.getId(), schema2.getId());
		
		// Third connection to petclinic
		final Connection conn3 = client.getConnection("petclinic");		
		assertEquals(OPEN, conn3.getStatus());
		assertEquals(client, conn3.getClient());
		final Schema schema3 = conn3.getSchema();
		assertEquals("petclinic", schema3.getName());

		// Create sessions in conn1
		Session[] sessions1 = new Session[SESSIONS];
		StateRequest[] requests1 = new StateRequest[SESSIONS];
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;  // Zhava. 
			async (() -> {
				String sid = newSid();
				Session ssn = conn1.getOrCreateSession(sid);
				assertEquals(sid, ssn.getId());
				State state = schema1.getState("state" + ((_i % 5) + 1));
				StateRequest req = ssn.targetForState(state);
				assertNotNull(req);
				assertEquals(req, ssn.getStateRequest());
				assertEquals(req.getSession(), ssn);
				sessions1[_i] = ssn;
				requests1[_i] = req;
			});
		}
		
		joinAll();
		
		for (int i = 0; i < SESSIONS; i++) {
			assertNotNull(sessions1[i]);
			assertNotNull(requests1[i]);
		}
		
		// Retrieve existing sessions over conn2
		Session[] sessions2 = new Session[SESSIONS];
		StateRequest[] requests2 = new StateRequest[SESSIONS];
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;  // Zhava. 
			async (() -> {
				String sid = sessions1[_i].getId();
				Session ssn = conn2.getSessionById(sid);
				assertEquals(sid, ssn.getId());
				assertNotEquals(ssn, sessions1[_i]);
				// Should be okay to use state from parallel schema.
				StateRequest req = ssn.getStateRequest();
				assertNotNull(req);
				assertEquals(req.getSession(), ssn);
				sessions2[_i] = ssn;
				requests2[_i] = req;
			});
		}

		// Create sessions in conn3
		Session[] sessions3 = new Session[SESSIONS];
		StateRequest[] requests3 = new StateRequest[SESSIONS];
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;  // Zhava. 
			async (() -> {
				String sid = newSid();
				Session ssn = conn3.getOrCreateSession(sid);
				assertEquals(sid, ssn.getId());
				State state = schema3.getState("newOwner");
				// The qualifying and targeting hooks will throw an NPE
				// if user-agent attribute is not set.
				ssn.setAttribute("user-agent", "does not matter");
				StateRequest req = ssn.targetForState(state);
				assertNotNull(req);
				assertEquals(req.getSession(), ssn);
				sessions3[_i] = ssn;
				requests3[_i] = req;
			});
		}

		joinAll();

		IoUtils.delete(SCHEMATA_DIR + "/big-covar-schema.json");
		Thread.sleep(dirWatcherLatencyMillis);

		// Short session timeout => all sessions should be gone and 3 connections cleaned out.
		
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions1[_i];
				StateRequest req = requests1[_i];

				// Non-mutating methods that don't go over the network.
				assertTrue(ssn.isExpired());
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				//assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn1, ssn.getConnection());
				assertEquals(1000, ssn.getTimeoutMillis());
				
				// Mutating methods throw connection closed exception
				// because by now all sessions in the connection have expired and the
				// connection has been vacuumed.
				new ClientUserExceptionInterceptor() {
				
					@Override public void toRun() {
						switch (_i % 10) {
						case 0: conn1.getSessionById(sessions1[_i].getId()); break;
						case 1: ssn.getTraversedStates(); break;
						case 2: ssn.getTraversedTests(); break;
						case 3: ssn.getDisqualifiedTests(); break;
						case 4: ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), schema1.getState("state1"))); break;
						case 5: ssn.getAttribute("foo"); break;
						case 6: ssn.setAttribute("foo", "bar"); break;
						case 7: ssn.clearAttribute("foo"); break;
						case 8: ssn.targetForState(schema1.getState("state4")); break;
						case 9: req.commit(); break;
						}
					}
					
					@Override public void onThrown(ClientException.User e) {
						assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
						assertEquals(CLOSED_BY_SERVER, conn1.getStatus());
					}
					
				}.assertThrown(ConnectionClosedException.class);
		
			});
		}

		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions2[_i];
				StateRequest req = requests2[_i];

				// Non-mutating methods that don't go over the network.
				assertTrue(ssn.isExpired());
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				//assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn2, ssn.getConnection());
				assertEquals(1000, ssn.getTimeoutMillis());
				
				// Mutating methods throw connection closed exception
				// because by now all sessions in the connection have expired and the
				// connection has been cleaned out.
				new ClientUserExceptionInterceptor() {
				
					@Override public void toRun() {
						switch (_i % 10) {
						case 0: conn2.getSessionById(sessions1[_i].getId()); break;
						case 1: ssn.getTraversedStates(); break;
						case 2: ssn.getTraversedTests(); break;
						case 3: ssn.getDisqualifiedTests(); break;
						case 4: ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), schema1.getState("state1"))); break;
						case 5: ssn.getAttribute("foo"); break;
						case 6: ssn.setAttribute("foo", "bar"); break;
						case 7: ssn.clearAttribute("foo"); break;
						case 8: ssn.targetForState(schema1.getState("state4")); break;
						case 9: req.commit(); break;
						}
					}
					
					@Override public void onThrown(ClientException.User e) {
						assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
						assertEquals(CLOSED_BY_SERVER, conn1.getStatus());
					}
					
				}.assertThrown(ConnectionClosedException.class);
		
			});
		}

		// The non-draining connection should remain open, even though all the sessions
		// are gone and vacuumed.
		assertEquals(OPEN, conn3.getStatus());

		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				// Session has expired on the server
				assertNull(conn3.getSessionById(sessions1[_i].getId()));
			
				SessionImpl ssn = (SessionImpl) sessions3[_i];
				StateRequest req = requests3[_i];

				// Non-mutating methods that don't go over the network.
				assertTrue(ssn.isExpired());
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				//assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn3, ssn.getConnection());
				assertEquals(1000, ssn.getTimeoutMillis());
				
				// Mutating methods throw session expired exception
				new ClientUserExceptionInterceptor() {
				
					@Override public void toRun() {
						switch (_i % 9) {
						case 0: req.commit(); break;
						case 1: ssn.getTraversedStates(); break;
						case 2: ssn.getTraversedTests(); break;
						case 3: ssn.getDisqualifiedTests(); break;
						case 4: ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), schema1.getState("state1"))); break;
						case 5: ssn.getAttribute("foo"); break;
						case 6: ssn.setAttribute("foo", "bar"); break;
						case 7: ssn.clearAttribute("foo"); break;
						case 8: ssn.targetForState(schema1.getState("state4")); break;
						}
					}
					
					@Override public void onThrown(ClientException.User e) {
						assertEquals(ClientUserError.SESSION_EXPIRED, e.getError());
					}
					
				}.assertThrown(SessionExpiredException.class);
		
			});
		}

		joinAll();
		
		assertNull(client.getConnection("big_covar_schema"));

	}	

	/**
	 * Schema undeployed with a session timeout interval set to greater than
	 * dirWatcherLatency, so that we can test schema draining. 
	 *
	//@org.junit.Test
	public void serverUndeploySessionDrainingTest() throws Exception {

		// Open connection
		final Connection conn = client.getConnection("big_covar_schema");		
		final Schema schema = conn.getSchema();
		assertEquals(OPEN, conn.getStatus());
		assertNotNull(conn.getClient());
		assertNotNull(schema);
		assertEquals("big_covar_schema", conn.getSchema().getName());
		assertEquals(5, conn.getSchema().getStates().size());
		assertEquals(6, conn.getSchema().getTests().size());
				
		// Create sessions
		Session[] sessions = new Session[SESSIONS];
		StateRequest[] requests = new StateRequest[SESSIONS];
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;  // Zhava. 
			async (() -> {
				String sid = newSid();
				Session ssn = conn.getOrCreateSession(sid);
				assertEquals(sid, ssn.getId());
				State state = conn.getSchema().getState("state" + ((_i % 5) + 1));
				StateRequest req = ssn.targetForState(state);
				assertNotNull(req);
				assertEquals(req.getSession(), ssn);
				sessions[_i] = ssn;
				requests[_i] = req;
			});
		}
		
		joinAll();

		IoUtils.delete(SCHEMATA_DIR + "/big-covar-schema.json");
		Thread.sleep(dirWatcherLatencyMillis);

		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions[_i];
				StateRequest req = requests[_i];

				// Non-mutating
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertTrue(ssn.isExpired());
				//assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn, ssn.getConnection());
				assertEquals(1000, ssn.getTimeoutMillis());
				
				// Try every mutating method - should throw connection closed exception.
				new ClientUserExceptionInterceptor() {
				
					@Override public void toRun() {
						switch (_i % 10) {
						case 0: conn.getSessionById(sessions[_i].getId()); break;
						case 1: ssn.getTraversedStates(); break;
						case 2: ssn.getTraversedTests(); break;
						case 3: ssn.getDisqualifiedTests(); break;
						case 4: ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), schema.getState("state1"))); break;
						case 5: ssn.getAttribute("foo"); break;
						case 6: ssn.setAttribute("foo", "bar"); break;
						case 7: ssn.clearAttribute("foo"); break;
						case 8: ssn.targetForState(schema.getState("state4")); break;
						case 9: req.commit(); break;
						}
					}
					
					@Override public void onThrown(ClientException.User e) {
						assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
						assertEquals(CLOSED_BY_SERVER, conn.getStatus());
					}
					
				}.assertThrown(ConnectionClosedException.class);
		
			});
		}
		
		joinAll();
		
		assertNull(client.getConnection("big_covar_schema"));

	}	*/
}
