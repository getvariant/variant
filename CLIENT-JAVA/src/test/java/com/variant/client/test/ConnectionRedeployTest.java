package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Connection.Status;
import com.variant.client.ConnectionClosedException;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.core.util.IoUtils;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class ConnectionRedeployTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	/**
	 * Schema undeployed. 
	 */
	@org.junit.Test
	public void closedByServerRedeployTest() throws Exception {
		
		// Connection must go after schema undeployed.
		final Connection conn1= client.getConnection("big_covar_schema");		
		assertNotNull(conn1);
		assertEquals(Status.OPEN, conn1.getStatus());
		assertEquals(client, conn1.getClient());
		assertNotNull(conn1.getSchema());
		assertEquals("big_covar_schema", conn1.getSchema().getName());
		assertEquals(5, conn1.getSchema().getStates().size());
		assertEquals(6, conn1.getSchema().getTests().size());

	    IoUtils.fileCopy("schemata-remote/big-covar-schema.json", SCHEMATA_DIR + "/big-covar-schema.json");
		Thread.sleep(dirWatcherLatencyMsecs);

		// Connection doesn't know yet the server is gone.
		assertEquals(Status.OPEN, conn1.getStatus());

		// Attempts to get or a non-existent session should fail. 
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		assertEquals(Status.CLOSED_BY_SERVER, conn1.getStatus());

		assertNull(conn1.getSessionById("foo"));
		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getOrCreateSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getId();
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		assertEquals(Status.CLOSED_BY_SERVER, conn1.getStatus());
		
		
		// Open new connection to the redeployed schema
		final Connection conn2 = client.getConnection("big_covar_schema");
		assertNotNull(conn2);
		assertEquals(Status.OPEN, conn2.getStatus());
		assertEquals(client, conn2.getClient());
		assertNotNull(conn2.getSchema());
		assertEquals("big_covar_schema", conn2.getSchema().getName());
		assertEquals(5, conn2.getSchema().getStates().size());
		assertEquals(6, conn2.getSchema().getTests().size());
		assertFalse(conn1.equals(conn2));
	}	

}
