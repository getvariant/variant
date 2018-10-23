package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.impl.ConnectionImpl;
import com.variant.client.impl.SessionImpl;
import com.variant.client.test.util.ClientBaseTestWithServerAsync;
import com.variant.core.TraceEvent;
import com.variant.core.impl.ServerError;
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

		restartServer();
		
		// Connection to a schema
		ConnectionImpl conn1 = (ConnectionImpl) client.connectTo("monstrosity");		
		assertNotNull(conn1);
		assertNotNull(conn1.getClient());
		assertEquals(conn1.getSessionTimeoutMillis(), 1000);
		assertEquals("monstrosity", conn1.getSchemaName());
		
		// Second connection to the same schema
		Connection conn2 = client.connectTo("monstrosity");		
		assertNotNull(conn2);
		assertEquals(conn1.getClient(), conn2.getClient());
		assertEquals("monstrosity", conn2.getSchemaName());

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
				Session ssn = conn1.getOrCreateSession(newSid());
				State state = ssn.getSchema().getState("state" + ((_i % 5) + 1)).get();
				StateRequest req = ssn.targetForState(state);
				assertNotNull(req);
				assertEquals(req, ssn.getStateRequest().get());
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
				StateRequest req = ssn.getStateRequest().get();
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
				Session ssn = conn3.getOrCreateSession(newSid());
				State state = ssn.getSchema().getState("newVisit").get();
				// The qualifying and targeting hooks will throw an NPE
				// if user-agent attribute is not set.
				ssn.getAttributes().put("user-agent", "does not matter");
				StateRequest req = ssn.targetForState(state);
				assertNotNull(req);
				assertEquals(req.getSession(), ssn);
				sessions3[_i] = ssn;
				requests3[_i] = req;
			});
		}

		joinAll();

		IoUtils.delete(SCHEMATA_DIR + "/monster.schema");
		Thread.sleep(dirWatcherLatencyMillis);

		// Short session timeout => all sessions should be gone
		// and throw SessionExpired exception on any mutating op which goes over the network.
		
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions1[_i];
				StateRequest req = requests1[_i];

				// Non-mutating methods that don't go over the network.
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(conn1, ssn.getConnection());
				assertEquals(1000, ssn.getTimeoutMillis());
				assertNull(conn1.getSessionById(sessions1[_i].getId()));
								
				// All else should throw session expired exception.
				new ClientExceptionInterceptor() {
					
					@Override public void toRun() {
						switch (_i % 9) {
						case 0: ssn.getTraversedStates(); break;
						case 1: ssn.getTraversedVariations(); break;
						case 2: ssn.getDisqualifiedVariations(); break;
						case 3: ssn.triggerTraceEvent(TraceEvent.mkTraceEvent("foo")); break;
						case 4: ssn.getAttributes().get("foo"); break;
						case 5: ssn.getAttributes().put("foo", "bar"); break;
						case 6: ssn.getAttributes().remove("foo"); break;
						case 7: ssn.targetForState(ssn.getSchema().getState("state" + ((_i % 5) + 1)).get()); break;
						case 8: req.commit(); break;
						}
					}
					
					@Override public void onThrown(VariantException e) {
						assertEquals(ServerError.SESSION_EXPIRED, e.getError());
					}
					
				}.assertThrown();
		
			});
		}

		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions2[_i];
				StateRequest req = requests2[_i];

				// Non-mutating methods that don't go over the network.
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(conn2, ssn.getConnection());
				assertEquals(1000, ssn.getTimeoutMillis());
			    assertNull(conn2.getSessionById(sessions1[_i].getId()));
			    
				// Mutating methods.
				new ClientExceptionInterceptor() {
					@Override public void toRun() {
						switch (_i % 9) {
						case 0: ssn.getTraversedStates(); break;
						case 1: ssn.getTraversedVariations(); break;
						case 2: ssn.getDisqualifiedVariations(); break;
						case 3: ssn.triggerTraceEvent(TraceEvent.mkTraceEvent("foo")); break;
						case 4: ssn.getAttributes().get("foo"); break;
						case 5: ssn.getAttributes().put("foo", "bar"); break;
						case 6: ssn.getAttributes().remove("foo"); break;
						case 7: req.commit(); break;
						case 8: ssn.targetForState(ssn.getSchema().getState("state" + ((_i % 5) + 1)).get()); break;
						}
					}
					
					@Override public void onThrown(VariantException e) {
						assertEquals(ServerError.SESSION_EXPIRED, e.getError());
					}
					
				}.assertThrown();
		
			});
		}

		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				// Session has expired on the server
				assertNull(conn3.getSessionById(sessions3[_i].getId()));
			
				SessionImpl ssn = (SessionImpl) sessions3[_i];
				StateRequest req = requests3[_i];

				// Non-mutating methods that don't go over the network.
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(conn3, ssn.getConnection());
				assertEquals(1000, ssn.getTimeoutMillis());
				
				// All else throw session expired exception.
				new ClientExceptionInterceptor() {
					
					@Override public void toRun() {
						switch (_i % 9) {
						case 0: req.commit(); break;
						case 1: ssn.getTraversedStates(); break;
						case 2: ssn.getTraversedVariations(); break;
						case 3: ssn.getDisqualifiedVariations(); break;
						case 4: ssn.triggerTraceEvent(TraceEvent.mkTraceEvent("foo")); break;
						case 5: ssn.getAttributes().get("foo"); break;
						case 6: ssn.getAttributes().put("foo", "bar"); break;
						case 7: ssn.getAttributes().remove("foo"); break;
						case 8: ssn.targetForState(ssn.getSchema().getState("newVisit").get()); break;

						}
					 }
					
					@Override public void onThrown(VariantException e) {
						assertEquals(ServerError.SESSION_EXPIRED, e.getError());
					}
					
				}.assertThrown();
		
			});
		}

		joinAll();
		
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				client.connectTo("monstrosity");
			}

			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.UNKNOWN_SCHEMA, e.getError());
			}
			
		}.assertThrown();

	}	

	/**
	 * Schema undeployed with a session timeout interval set to greater than
	 * dirWatcherLatency, so that sessions do not expire while we wait for
	 * server to detect the delete of the schema file. 
	 */
	@org.junit.Test
	public void serverUndeploySessionDrainingTest() throws Exception {

		restartServer();

		// Restore the monstrosity
	    IoUtils.fileCopy(SCHEMATA_DIR_SRC + "monster.schema", SCHEMATA_DIR + "/monster.schema");

		// restart the server with the longger session timeout
		int ssnTimeout = dirWatcherLatencyMillis/1000 + 5;

		restartServer(CollectionsUtils.pairsToMap(new Tuples.Pair<String,String>("variant.session.timeout", String.valueOf(ssnTimeout))));
		
		// Connection to a schema
		ConnectionImpl conn1 = (ConnectionImpl) client.connectTo("monstrosity");		
		assertNotNull(conn1);
		assertNotNull(conn1.getClient());
		assertEquals(ssnTimeout * 1000, conn1.getSessionTimeoutMillis());
		assertEquals("monstrosity", conn1.getSchemaName());
		
		// Second connection to the same schema
		Connection conn2 = client.connectTo("monstrosity");		
		assertNotNull(conn2);
		assertEquals(conn1.getClient(), conn2.getClient());
		assertEquals("monstrosity", conn2.getSchemaName());

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
				Session ssn = conn1.getOrCreateSession(newSid());
				State state = ssn.getSchema().getState("state" + ((_i % 5) + 1)).get();
				StateRequest req = ssn.targetForState(state);
				assertNotNull(req);
				assertEquals(req, ssn.getStateRequest().get());
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
				StateRequest req = ssn.getStateRequest().get();
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
				Session ssn = conn3.getOrCreateSession(newSid());
				State state = ssn.getSchema().getState("newVisit").get();
				// The qualifying and targeting hooks will throw an NPE
				// if user-agent attribute is not set.
				ssn.getAttributes().put("user-agent", "does not matter");
				StateRequest req = ssn.targetForState(state);
				assertNotNull(req);
				assertEquals(req.getSession(), ssn);
				sessions3[_i] = ssn;
				requests3[_i] = req;
			});
		}

		joinAll();

		IoUtils.delete(SCHEMATA_DIR + "/monster.schema");
		Thread.sleep(dirWatcherLatencyMillis);

		// Long session timeout => all sessions should be alive.
				
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions1[_i];
				StateRequest req = requests1[_i];

				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn1, ssn.getConnection());
				assertEquals(ssnTimeout * 1000, ssn.getTimeoutMillis());
				
				assertNotNull(ssn.getTraversedStates());
				assertNotNull(ssn.getTraversedVariations());
				assertNotNull(ssn.getDisqualifiedVariations());
				ssn.triggerTraceEvent(TraceEvent.mkTraceEvent("foo"));
				String key = "key" + _i;
				String value = "value" + _i;
				assertNull(ssn.getAttributes().get(key));
				assertNull(ssn.getAttributes().put(key, value));
				assertEquals(value, ssn.getAttributes().get(key));
				
				// Current state request is active
				new ClientExceptionInterceptor() {
					
					@Override public void toRun() {
						ssn.targetForState(ssn.getSchema().getState("state" + ((_i % 5) + 1)).get());			
					}
					
					@Override public void onThrown(VariantException e) {
						assertEquals(ServerError.ACTIVE_REQUEST, e.getError());
					}

				}.assertThrown();
				
				Session ssnByIdFromThisConnection = conn1.getSessionById(ssn.getId());
				assertEquals(value, ssnByIdFromThisConnection.getAttributes().get(key));
				
				// Getting session by ID in different connection should yield different object.
				Session ssnByIdFromOtherConnection = conn2.getSessionById(ssn.getId());
				assertNotEquals(ssn, ssnByIdFromOtherConnection);
				assertTrue(ssnByIdFromOtherConnection.getStateRequest().isPresent());
				assertNotEquals(ssn.getStateRequest().get(), ssnByIdFromOtherConnection.getStateRequest().get());
				assertEquals(value, ssnByIdFromOtherConnection.getAttributes().get(key));

				req.commit();
				
				// Committing in the via other session is OK.
				ssnByIdFromOtherConnection.getStateRequest().get().commit();
			});
		}

		joinAll();
		
		// Attempt to create a session should fail.
		new ClientExceptionInterceptor() {

			@Override public void toRun() {
				conn2.getOrCreateSession(newSid());				
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.UNKNOWN_SCHEMA, e.getError());
			}

		}.assertThrown();
		
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions2[_i];
				StateRequest req = requests2[_i];

				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn2, ssn.getConnection());
				assertEquals(ssnTimeout * 1000, ssn.getTimeoutMillis());
				
				assertNotNull(ssn.getTraversedStates());
				assertNotNull(ssn.getTraversedVariations());
				assertNotNull(ssn.getDisqualifiedVariations());
				ssn.triggerTraceEvent(TraceEvent.mkTraceEvent("foo"));
				String key = "key" + _i;
				String value = "value" + _i;
				assertEquals(value, ssn.getAttributes().get(key));
				assertEquals(value, sessions1[_i].getAttributes().remove(key));
				assertNotNull(ssn.targetForState(ssn.getSchema().getState("state" + ((_i % 5) + 1)).get()));
				req.commit();
		
			});
		}

		// The non-draining connection should work too.
		
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				// Session has expired on the server
				assertNotNull(conn3.getSessionById(sessions3[_i].getId()));

				SessionImpl ssn = (SessionImpl) sessions3[_i];
				StateRequest req = requests3[_i];

				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(client.getConfig(), ssn.getConfig());
				assertEquals(conn3, ssn.getConnection());
				assertEquals(ssnTimeout * 1000, ssn.getTimeoutMillis());
				
				assertNotNull(ssn.getTraversedStates());
				assertNotNull(ssn.getTraversedVariations());
				assertNotNull(ssn.getDisqualifiedVariations());
				// The user-agent attribute should still be set
				assertEquals("does not matter", ssn.getAttributes().remove("user-agent"));
				req.commit();

			});
		}

		joinAll();

		assertNotNull(client.connectTo("monstrosity"));

	}	
}
