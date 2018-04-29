package com.variant.client.test;

import static com.variant.core.ConnectionStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
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
	
	private int SESSIONS = 200;
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	/**
	 * Schema undeployed. 
	 */
	@org.junit.Test
	public void closedByServerUndeployTest() throws Exception {

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
		Thread.sleep(dirWatcherLatencyMsecs);

		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;
			async (() -> {
				
				SessionImpl ssn = (SessionImpl) sessions[_i];
				StateRequest req = requests[_i];

				// Non-mutating
				assertNotNull(ssn.getId());
				assertNotNull(ssn.getCreateDate());
				assertEquals(conn, ssn.getConnection());
				assertEquals(1000, ssn.getTimeoutMillis());
				
				// Mutable, but doesn't throw connection closed exception 
				assertTrue(ssn.isExpired());

				// Mutable, throws connection closed exception.
				new ClientUserExceptionInterceptor() {
				
					@Override public void toRun() {
						switch (_i % 11) {						
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
						case 10: ssn.getConfig(); break;
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
	}	

}
