package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.ConnectionImpl;
import com.variant.client.impl.SessionImpl;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.State;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.IoUtils;
import com.variant.core.util.Tuples;

/**
 * Ridiculously complex test that tries to test everything.
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

		// Connection to a schema
		ConnectionImpl conn1 = (ConnectionImpl) client.connectTo("big_conjoint_schema");		
		assertNotNull(conn1);
		assertNotNull(conn1.getClient());
		assertEquals(conn1.getSessionTimeoutMillis(), 1000);
		assertNotNull(conn1.getSessionCache());
		assertEquals("big_conjoint_schema", conn1.getSchemaName());
		
		// Second connection to the same schema
		Connection conn2 = client.connectTo("big_conjoint_schema");		
		assertNotNull(conn2);
		assertEquals(conn1.getClient(), conn2.getClient());
		assertEquals("big_conjoint_schema", conn2.getSchemaName());

		// Third connection to petclinic schema
		Connection conn3 = client.connectTo("petclinic");		
		assertNotNull(conn3);
		assertEquals(conn1.getClient(), conn3.getClient());
		assertEquals("petclinic", conn3.getSchemaName());

		// Create sessions in conn1
		Session[] sessions1 = new Session[SESSIONS];
		StateRequest[] requests1 = new StateRequest[SESSIONS];
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;  // Zhava. 
			async (() -> {
				String sid = newSid();
				Session ssn = conn1.getOrCreateSession(sid);
				assertEquals(sid, ssn.getId());
				State state = ssn.getSchema().getState("state" + ((_i % 5) + 1));
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
				State state = ssn.getSchema().getState("newOwner");
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

		IoUtils.delete(SCHEMATA_DIR + "/big-conjoint-schema.json");
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
				
				switch (_i % 10) {
				case 0: conn1.getSessionById(sessions1[_i].getId()); break;
				case 1: ssn.getTraversedStates(); break;
				case 2: ssn.getTraversedTests(); break;
				case 3: ssn.getDisqualifiedTests(); break;
				case 4: ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), ssn.getSchema().getState("state1"))); break;
				case 5: ssn.getAttribute("foo"); break;
				case 6: ssn.setAttribute("foo", "bar"); break;
				case 7: ssn.clearAttribute("foo"); break;
				case 8: ssn.targetForState(ssn.getSchema().getState("state" + ((_i % 5) + 1))); break;
				case 9: req.commit(); break;
				}
		
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
				
				switch (_i % 10) {
				case 0: conn2.getSessionById(sessions1[_i].getId()); break;
				case 1: ssn.getTraversedStates(); break;
				case 2: ssn.getTraversedTests(); break;
				case 3: ssn.getDisqualifiedTests(); break;
				case 4: ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), ssn.getSchema().getState("state1"))); break;
				case 5: ssn.getAttribute("foo"); break;
				case 6: ssn.setAttribute("foo", "bar"); break;
				case 7: ssn.clearAttribute("foo"); break;
				case 8: ssn.targetForState(ssn.getSchema().getState("state" + ((_i % 5) + 1))); break;
				case 9: req.commit(); break;
				}
		
			});
		}

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
				
					switch (_i % 9) {
					case 0: req.commit(); break;
					case 1: ssn.getTraversedStates(); break;
					case 2: ssn.getTraversedTests(); break;
					case 3: ssn.getDisqualifiedTests(); break;
					case 4: ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), ssn.getSchema().getState("state1"))); break;
					case 5: ssn.getAttribute("foo"); break;
					case 6: ssn.setAttribute("foo", "bar"); break;
					case 7: ssn.clearAttribute("foo"); break;
					case 8: ssn.targetForState(ssn.getSchema().getState("state" + ((_i % 5) + 1))); break;
					}
		
			});
		}

		joinAll();
		
		assertNull(client.connectTo("big_conjoint_schema"));

	}	

	/**
	 * Schema undeployed with a session timeout interval set to greater than
	 * dirWatcherLatency, so that sessions do not expire while we wait for
	 * server to detect the delete of the schema file. 
	 */
	@org.junit.Test
	public void serverUndeploySessionDrainingTest() throws Exception {

		// Restore the big_conjoint_schema
	    IoUtils.fileCopy(SCHEMATA_DIR_SRC + "big-conjoint-schema.json", SCHEMATA_DIR + "/big-conjoint-schema.json");

		// restart the server with the longger session timeout
		int ssnTimeout = dirWatcherLatencyMillis/1000 + 2000;
		server.restart(CollectionsUtils.pairsToMap(new Tuples.Pair<String,String>("variant.session.timeout", String.valueOf(ssnTimeout))));
		
		// Connection to a schema
		ConnectionImpl conn1 = (ConnectionImpl) client.connectTo("big_conjoint_schema");		
		assertNotNull(conn1);
		assertNotNull(conn1.getClient());
		assertEquals(conn1.getSessionTimeoutMillis(), 1000);
		assertNotNull(conn1.getSessionCache());
		assertEquals("big_conjoint_schema", conn1.getSchemaName());
		
		// Second connection to the same schema
		Connection conn2 = client.connectTo("big_conjoint_schema");		
		assertNotNull(conn2);
		assertEquals(conn1.getClient(), conn2.getClient());
		assertEquals("big_conjoint_schema", conn2.getSchemaName());

		// Third connection to petclinic schema
		Connection conn3 = client.connectTo("petclinic");		
		assertNotNull(conn3);
		assertEquals(conn1.getClient(), conn3.getClient());
		assertEquals("petclinic", conn3.getSchemaName());

		// Create sessions in conn1
		Session[] sessions1 = new Session[SESSIONS];
		StateRequest[] requests1 = new StateRequest[SESSIONS];
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;  // Zhava. 
			async (() -> {
				String sid = newSid();
				Session ssn = conn1.getOrCreateSession(sid);
				assertEquals(sid, ssn.getId());
				State state = ssn.getSchema().getState("state" + ((_i % 5) + 1));
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
				State state = ssn.getSchema().getState("newOwner");
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

		IoUtils.delete(SCHEMATA_DIR + "/big-conjoint-schema.json");
		Thread.sleep(dirWatcherLatencyMillis);

		// Long session timeout => all sessions should be alive.
				
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions1[_i];
				StateRequest req = requests1[_i];

				assertFalse(ssn.isExpired());
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn1, ssn.getConnection());
				assertEquals(ssnTimeout * 1000, ssn.getTimeoutMillis());
				
				assertNotNull(ssn.getTraversedStates());
				assertNotNull(ssn.getTraversedTests());
				assertNotNull(ssn.getDisqualifiedTests());
				ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), ssn.getSchema().getState("state1")));
				String key = "key" + _i;
				String value = "value" + _i;
				assertNull(ssn.getAttribute(key));
				assertNull(ssn.setAttribute(key, value));
				assertEquals(value, ssn.getAttribute(key));
				
				// Current state request is active
				new ClientExceptionInterceptor() {
					
					@Override public void toRun() {
						ssn.targetForState(ssn.getSchema().getState("state" + ((_i % 5) + 1)));			
					}
					
					@Override public void onThrown(ClientException e) {
						assertEquals(ClientUserError.ACTIVE_REQUEST, e.getError());
					}

				}.assertThrown();
				
				// Getting session by id in the same connection should yield the same object.
				Session ssnByIdFromThisConnection = conn1.getSessionById(ssn.getId());
				assertEquals(ssn, ssnByIdFromThisConnection);
				assertEquals(ssn.getStateRequest(), ssnByIdFromThisConnection.getStateRequest());
				assertEquals(value, ssnByIdFromThisConnection.getAttribute(key));
				
				// Getting session by ID in different connection should yield different object.
				Session ssnByIdFromOtherConnection = conn2.getSessionById(ssn.getId());
				assertNotEquals(ssn, ssnByIdFromOtherConnection);
				assertNotNull(ssnByIdFromOtherConnection.getStateRequest());
				assertNotEquals(ssn.getStateRequest(), ssnByIdFromOtherConnection.getStateRequest());
				assertEquals(value, ssnByIdFromOtherConnection.getAttribute(key));

				assertTrue(req.commit());
				
				// Committing in the via other session is OK.
				ssnByIdFromOtherConnection.getStateRequest().commit();
			});
		}

		joinAll();
		
		// Attempt to create a session should fail.
		new ClientExceptionInterceptor() {

			@Override public void toRun() {
				conn2.getOrCreateSession(newSid());				
			}
			
			@Override public void onThrown(ClientException e) {
				assertEquals(ServerError.UnknownSchema, e.getError());
			}

		}.assertThrown();
		
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions2[_i];
				StateRequest req = requests2[_i];

				assertFalse(ssn.isExpired());
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn2, ssn.getConnection());
				assertEquals(ssnTimeout * 1000, ssn.getTimeoutMillis());
				
				assertEquals(ssn, conn2.getSessionById(ssn.getId()));
				assertNotNull(ssn.getTraversedStates());
				assertNotNull(ssn.getTraversedTests());
				assertNotNull(ssn.getDisqualifiedTests());
				ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), ssn.getSchema().getState("state1")));
				String key = "key" + _i;
				String value = "value" + _i;
				assertEquals(value, ssn.getAttribute(key));
				assertEquals(value, sessions1[_i].clearAttribute(key));
				assertNotNull(ssn.targetForState(ssn.getSchema().getState("state" + ((_i % 5) + 1))));
				assertTrue(req.commit());
		
			});
		}

		// The non-draining connection should work too.
		
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				// Session has expired on the server
				assertNull(conn3.getSessionById(sessions1[_i].getId()));

				SessionImpl ssn = (SessionImpl) sessions3[_i];
				StateRequest req = requests3[_i];

				assertFalse(ssn.isExpired());
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn3, ssn.getConnection());
				assertEquals(ssnTimeout * 1000, ssn.getTimeoutMillis());
				
				assertEquals(ssn, conn3.getSessionById(ssn.getId()));
				assertNotNull(ssn.getTraversedStates());
				assertNotNull(ssn.getTraversedTests());
				assertNotNull(ssn.getDisqualifiedTests());
				State state = ssn.getSchema().getState("newOwner");
				// The user-agent attribute should still be set
				assertEquals("does not matter", ssn.clearAttribute("user-agent"));
				assertTrue(req.commit());

			});
		}

		joinAll();
		
		assertNull(client.connectTo("big_conjoint_schema"));

	}	
}
