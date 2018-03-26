package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Connection.Status;
import com.variant.client.ConnectionClosedException;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.core.ServerError;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class ConnectionColdTest extends ClientBaseTestWithServer {
	
	// Sole client
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
				assertEquals(ServerError.UnknownSchema, e.getError());
			}
		}.assertThrown();
	
	}	
	
	/**
	 */
	@org.junit.Test
	public void connectToExistingSchemataTest() throws Exception {
			    
		// Connection to a schema
		Connection conn1 = client.getConnection("big_covar_schema");		
		assertNotNull(conn1);
		assertEquals(Status.OPEN, conn1.getStatus());
		assertNotNull(conn1.getClient());
		assertNotNull(conn1.getSchema());
		assertEquals("big_covar_schema", conn1.getSchema().getName());
		assertEquals(5, conn1.getSchema().getStates().size());
		assertEquals(6, conn1.getSchema().getTests().size());

		// Second connection to the same schema
		Connection conn2 = client.getConnection("big_covar_schema");		
		assertNotNull(conn2);
		assertEquals(Status.OPEN, conn2.getStatus());
		assertEquals(conn1.getClient(), conn2.getClient());
		assertNotNull(conn2.getSchema());
		assertEquals("big_covar_schema", conn2.getSchema().getName());
		assertEquals(5, conn2.getSchema().getStates().size());
		assertEquals(6, conn2.getSchema().getTests().size());

		// Third connection to another schema
		Connection conn3 = client.getConnection("petclinic");		
		assertNotNull(conn3);
		assertEquals(Status.OPEN, conn3.getStatus());
		assertEquals(conn1.getClient(), conn3.getClient());
		assertNotNull(conn3.getSchema());
		assertEquals("petclinic", conn3.getSchema().getName());
		assertEquals(2, conn3.getSchema().getStates().size());
		assertEquals(1, conn3.getSchema().getTests().size());

		// Close first connection
		conn1.close();
		assertEquals(Status.CLOSED_BY_CLIENT, conn1.getStatus());
		
		// Noop on subsequent close.
		conn1.close();
		assertEquals(Status.CLOSED_BY_CLIENT, conn1.getStatus());		
		
		// Other 2 connections should not be affected.
		assertEquals(Status.OPEN, conn2.getStatus());
		assertEquals("big_covar_schema", conn2.getSchema().getName());
		assertEquals(5, conn2.getSchema().getStates().size());
		assertEquals(6, conn2.getSchema().getTests().size());
		assertEquals(Status.OPEN, conn3.getStatus());
		assertEquals("petclinic", conn3.getSchema().getName());
		assertEquals(2, conn3.getSchema().getStates().size());
		assertEquals(1, conn3.getSchema().getTests().size());

		// Close second connection
		conn2.close();
		assertEquals(Status.CLOSED_BY_CLIENT, conn1.getStatus());
		assertEquals(Status.CLOSED_BY_CLIENT, conn2.getStatus());		

		// Third connection should not be affected.
		assertEquals(Status.OPEN, conn3.getStatus());
		assertEquals("petclinic", conn3.getSchema().getName());
		assertEquals(2, conn3.getSchema().getStates().size());
		assertEquals(1, conn3.getSchema().getTests().size());

		// Close last connection
		conn3.close();
		assertEquals(Status.CLOSED_BY_CLIENT, conn1.getStatus());
		assertEquals(Status.CLOSED_BY_CLIENT, conn2.getStatus());
		assertEquals(Status.CLOSED_BY_CLIENT, conn3.getStatus());

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
				client.getConnection("big_covar_schema");		
			}
			
		}.assertThrown(ServerError.TooManyConnections);

        for (int i = 0; i < 10; i++) {
    		connections[i].close();		
    		assertEquals(Status.CLOSED_BY_CLIENT, connections[i].getStatus());
        }

	}

	/**
	 * Connection closed by client.
	 */
	@org.junit.Test
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
	 * Server restarted with the same schema. 
	 */
	@org.junit.Test
	public void closedByServerRestartTest() throws Exception {
		
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
