package com.variant.client.test;

import static com.variant.core.ConnectionStatus.*;
import static org.junit.Assert.*;

import java.util.LinkedList;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.ConnectionClosedException;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.SessionImpl;
import com.variant.core.schema.State;
import com.variant.core.util.IoUtils;
import com.variant.core.impl.StateVisitedEvent;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class SessionUndeployTest extends ClientBaseTestWithServerAsync {
	
	private int SESSIONS = 100;
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	/**
	 * Schema undeployed. 
	 */
	@org.junit.Test
	public void closedByServerUndeployTest() throws Exception {

		// Open connection
		final Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(OPEN, conn.getStatus());
		assertNotNull(conn.getClient());
		assertNotNull(conn.getSchema());
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

				// Local ops do not refresh => no exceptions
				assertNotNull(ssn.getId());
				assertFalse(ssn.isExpired());
				assertNotNull(ssn.getCreateDate());
				assertEquals(conn, ssn.getConnection());
				assertEquals(conn.getClient().getConfig(), ssn.getConfig());
				
				// These require refresh => throw exception.
				new ClientUserExceptionInterceptor() {
				
					@Override public void toRun() {
						switch (_i % 10) {
						case 0: conn.getSessionById(sessions[_i].getId()); break;
						case 1: ssn.getTraversedStates(); break;
						case 2: ssn.getTraversedTests(); break;
						case 3: ssn.getDisqualifiedTests(); break;
						case 4: ssn.triggerEvent(new StateVisitedEvent(ssn.getCoreSession(), conn.getSchema().getState("state1"))); break;
						case 8: ssn.getAttribute("foo"); break;
						case 9: req.commit(); break;
						}
					}
					
					@Override public void onThrown(ClientException.User e) {
						assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
					}
					
					@Override public void onNotThrown() {
						System.out.println("******************* " + _i);
					}

				}.assertThrown(ConnectionClosedException.class);
		
				assertEquals(CLOSED_BY_SERVER, conn.getStatus());
				
				////// check immutable ops again.
				////// Check session expiration listeners
				////// should we expire sessions or connection close?
			});
		}
		
		joinAll();
		
		assertNull(client.getConnection("big_covar_schema"));
	}	

}
