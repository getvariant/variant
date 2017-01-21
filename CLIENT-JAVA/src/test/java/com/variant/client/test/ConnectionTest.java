package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.variant.client.ClientException;
import com.variant.client.ClientUserError;
import com.variant.client.Connection;
import com.variant.client.Connection.Status;
import com.variant.client.ConnectionClosedException;
import com.variant.client.VariantClient;
import com.variant.core.exception.ServerError;

public class ConnectionTest extends ClientBaseTestWithServer {
	
	private VariantClient client = VariantClient.Factory.getInstance();

	/**
	 */
	@org.junit.Test
	public void connectToNonExistentSchemaTest() throws Exception {
		
		new ClientUserExceptionInterceptor() {
			Connection conn = null;
			@Override public void toRun() {
				conn = client.getConnection("bad_schema");
			}
			@Override public void onThrown(ClientException.User e) {
				assertNull(conn);
			}
		}.assertThrown();
	
	}	
	
	/**
	 */
	@org.junit.Test
	public void connectToExistingSchemaTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		assertNotNull(conn.getClient());
		assertNotNull(conn.getSchema());
		assertEquals("big_covar_schema", conn.getSchema().getName());
		assertEquals(5, conn.getSchema().getStates().size());
		assertEquals(6, conn.getSchema().getTests().size());
				
		conn.close();
		assertEquals(Status.CLOSED_BY_CLIENT, conn.getStatus());
		
		// Noop on subsequent close.
		conn.close();
		assertEquals(Status.CLOSED_BY_CLIENT, conn.getStatus());		
	}	

	/**
	 * This test will break if at its start there are any unclosed connections.
	 * @throws Exception 
	 */
	@org.junit.Test
	public void tooManyConnectionsTest() throws Exception {

		Connection[] connections = new Connection[10];
        for (int i = 0; i < 10; i++) {
    		connections[i] = client.getConnection("big_covar_schema");		
    		assertNotNull(connections[i]);        	
        }
		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				Connection conn = client.getConnection("big_covar_schema");		
			}
			
		}.assertThrown(ServerError.TooManyConnections);

        for (int i = 0; i < 10; i++) {
    		connections[i].close();		
    		assertEquals(Status.CLOSED_BY_CLIENT, connections[i].getStatus());
        }

	}

	/**
	 */
	//@org.junit.Test
	public void closedByClientTest() throws Exception {
		
		final Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		assertNotNull(conn.getClient());
		assertNotNull(conn.getSchema());
		assertEquals("big_covar_schema", conn.getSchema().getName());
		assertEquals(5, conn.getSchema().getStates().size());
		assertEquals(6, conn.getSchema().getTests().size());
		
		assertNull(conn.getSession("foo"));
		assertNull(conn.getSessionById("foo"));
		assertNotNull(conn.getOrCreateSession("foo"));  // Creates the session.
		assertNotNull(conn.getSession("foo"));
		assertNotNull(conn.getSessionById("foo"));

		conn.close();
		assertEquals(Status.CLOSED_BY_CLIENT, conn.getStatus());
		
		// Noop on subsequent close.
		conn.close();
		assertEquals(Status.CLOSED_BY_CLIENT, conn.getStatus());

		// Throw user error exception when trying to use this connection.
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);
		
		assertEquals(Status.CLOSED_BY_CLIENT, conn.getStatus());
	}	

	/**
	 * This does not work because the session expires while the server restarts. 
	 */
	@org.junit.Test
	public void closedByServerTest() throws Exception {
		
		final Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(Status.OPEN, conn.getStatus());
		assertNotNull(conn.getClient());
		assertNotNull(conn.getSchema());
		assertEquals("big_covar_schema", conn.getSchema().getName());
		assertEquals(5, conn.getSchema().getStates().size());
		assertEquals(6, conn.getSchema().getTests().size());

		server.restart();

		assertEquals(Status.OPEN, conn.getStatus());

		assertNull(conn.getSession("foo"));        
		assertNull(conn.getSessionById("foo"));

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getOrCreateSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		assertEquals(Status.CLOSED_BY_SERVER, conn.getStatus());
	}	
	
}
