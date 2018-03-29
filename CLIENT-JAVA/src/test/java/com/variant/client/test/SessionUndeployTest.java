package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.LinkedList;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Connection.Status;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.core.ServerError;
import com.variant.core.util.IoUtils;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class SessionUndeployTest extends ClientBaseTestWithServer {
	
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
		assertEquals(Status.OPEN, conn.getStatus());
		assertNotNull(conn.getClient());
		assertNotNull(conn.getSchema());
		assertEquals("big_covar_schema", conn.getSchema().getName());
		assertEquals(5, conn.getSchema().getStates().size());
		assertEquals(6, conn.getSchema().getTests().size());
		
		// Register session expiration listener.
		final LinkedList<Session> expiredSessions = new LinkedList<Session>();
		
		conn.registerExpirationListener(
				new Connection.ExpirationListener() {
					@Override
					public void expired(Session session) {
						expiredSessions.add(session);
					}
				});
		
		// Create session
		final String sid1 = newSid();
		final String sid2 = newSid();
		Session ssn1 = conn.getOrCreateSession(sid1);
		assertEquals(sid1, ssn1.getId());
		Session ssn2 = conn.getOrCreateSession(sid2);
		assertEquals(sid2, ssn2.getId());

		StateRequest req1 = ssn1.targetForState(conn.getSchema().getState("state1"));
		assertNotNull(req1);
		
		IoUtils.delete(SCHEMATA_DIR + "/big-covar-schema.json");
		Thread.sleep(dirWatcherLatencyMsecs);

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSessionById(sid1);
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		assertEquals(Status.CLOSED_BY_SERVER, conn.getStatus());
		
		
	}	

}
