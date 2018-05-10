package com.variant.client.test;

import static com.variant.core.ConnectionStatus.CLOSED_BY_SERVER;
import static com.variant.core.ConnectionStatus.OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.VariantClientImpl;
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
		final Connection conn1 = client.getConnection("big_covar_schema");		
		assertNotNull(conn1);
		assertEquals(OPEN, conn1.getStatus());
		assertEquals(client, conn1.getClient());
		assertNotNull(conn1.getSchema());
		assertEquals("big_covar_schema", conn1.getSchema().getName());
		assertEquals(5, conn1.getSchema().getStates().size());
		assertEquals(6, conn1.getSchema().getTests().size());

	    IoUtils.fileCopy("schemata-remote/big-covar-schema.json", SCHEMATA_DIR + "/big-covar-schema.json");
		Thread.sleep(dirWatcherLatencyMillis);

		// Connection doesn't know its' gone.
		assertEquals(OPEN, conn1.getStatus());
		assertEquals(conn1, ((VariantClientImpl)client).byId(conn1.getId()));

		// Any attempts to use the connection should throw an exception. 		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		assertEquals(CLOSED_BY_SERVER, conn1.getStatus());
		assertNull(((VariantClientImpl)client).byId(conn1.getId()));

		// Now attempts to use the connection should throw an exception. 
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getSessionById("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);
		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getOrCreateSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		assertEquals(CLOSED_BY_SERVER, conn1.getStatus());
		
		
		// Open new connection to the redeployed schema
		final Connection conn2 = client.getConnection("big_covar_schema");
		assertNotNull(conn2);
		assertEquals(OPEN, conn2.getStatus());
		assertEquals(client, conn2.getClient());
		assertNotNull(conn2.getSchema());
		assertEquals("big_covar_schema", conn2.getSchema().getName());
		assertEquals(5, conn2.getSchema().getStates().size());
		assertEquals(6, conn2.getSchema().getTests().size());
		assertFalse(conn1.equals(conn2));
		
	}	

}
