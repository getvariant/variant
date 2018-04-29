package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Connection.LifecycleListener;

import static com.variant.core.ConnectionStatus.*;

import com.variant.client.ConnectionClosedException;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.VariantClientImpl;
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
		
		assertNull(client.getConnection("bad_schema"));
	
	}	
	
	/**
	 */
	@org.junit.Test
	public void connectToExistingSchemataTest() throws Exception {
			    
		// Connection to a schema
		Connection conn1 = client.getConnection("big_covar_schema");		
		assertNotNull(conn1);
		assertEquals(OPEN, conn1.getStatus());
		assertNotNull(conn1.getClient());
		assertNotNull(conn1.getSchema());
		assertEquals("big_covar_schema", conn1.getSchema().getName());
		assertEquals(5, conn1.getSchema().getStates().size());
		assertEquals(6, conn1.getSchema().getTests().size());

		// Second connection to the same schema
		Connection conn2 = client.getConnection("big_covar_schema");		
		assertNotNull(conn2);
		assertEquals(OPEN, conn2.getStatus());
		assertEquals(conn1.getClient(), conn2.getClient());
		assertNotNull(conn2.getSchema());
		assertEquals("big_covar_schema", conn2.getSchema().getName());
		assertEquals(5, conn2.getSchema().getStates().size());
		assertEquals(6, conn2.getSchema().getTests().size());

		// Third connection to another schema
		Connection conn3 = client.getConnection("petclinic");		
		assertNotNull(conn3);
		assertEquals(OPEN, conn3.getStatus());
		assertEquals(conn1.getClient(), conn3.getClient());
		assertNotNull(conn3.getSchema());
		assertEquals("petclinic", conn3.getSchema().getName());
		assertEquals(2, conn3.getSchema().getStates().size());
		assertEquals(1, conn3.getSchema().getTests().size());

		// Close first connection
		conn1.close();
		assertEquals(CLOSED_BY_CLIENT, conn1.getStatus());
		assertNull(((VariantClientImpl)client).byId(conn1.getId()));
				
		// Other 2 connections should not be affected.
		assertEquals(OPEN, conn2.getStatus());
		assertEquals("big_covar_schema", conn2.getSchema().getName());
		assertEquals(5, conn2.getSchema().getStates().size());
		assertEquals(6, conn2.getSchema().getTests().size());
		assertEquals(OPEN, conn3.getStatus());
		assertEquals("petclinic", conn3.getSchema().getName());
		assertEquals(2, conn3.getSchema().getStates().size());
		assertEquals(1, conn3.getSchema().getTests().size());

		// Close second connection
		conn2.close();
		assertNull(((VariantClientImpl)client).byId(conn2.getId()));
		assertEquals(CLOSED_BY_CLIENT, conn1.getStatus());
		assertEquals(CLOSED_BY_CLIENT, conn2.getStatus());		

		// Third connection should not be affected.
		assertEquals(OPEN, conn3.getStatus());
		assertEquals("petclinic", conn3.getSchema().getName());
		assertEquals(2, conn3.getSchema().getStates().size());
		assertEquals(1, conn3.getSchema().getTests().size());

		// Close last connection
		conn3.close();
		assertNull(((VariantClientImpl)client).byId(conn3.getId()));
		assertEquals(CLOSED_BY_CLIENT, conn1.getStatus());
		assertEquals(CLOSED_BY_CLIENT, conn2.getStatus());
		assertEquals(CLOSED_BY_CLIENT, conn3.getStatus());

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
    		assertEquals(CLOSED_BY_CLIENT, connections[i].getStatus());
        }

	}

	/**
	 * Connection closed by client.
	 */
	@org.junit.Test
	public void closedByClientTest() throws Exception {
		
		final Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(OPEN, conn.getStatus());
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
		assertEquals(CLOSED_BY_CLIENT, conn.getStatus());
		
		// Exception on subsequent operations
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.close();
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.registerLifecycleListener(
						new LifecycleListener() {							
							@Override
							public void onClosed(Connection connection) {}
						});
			}
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);
		

	}	

	/**
	 * Server restarted with the same schema. 
	 */
	@org.junit.Test
	public void closedByServerRestartTest() throws Exception {
		
		final Connection conn = client.getConnection("big_covar_schema");		
		assertNotNull(conn);
		assertEquals(OPEN, conn.getStatus());
		assertNotNull(conn.getClient());
		assertNotNull(conn.getSchema());
		assertEquals("big_covar_schema", conn.getSchema().getName());
		assertEquals(5, conn.getSchema().getStates().size());
		assertEquals(6, conn.getSchema().getTests().size());

		server.restart();

		// The connection doesn't yet know it's gone.
		assertEquals(OPEN, conn.getStatus());

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);       

		assertEquals(CLOSED_BY_SERVER, conn.getStatus());

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSessionById("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);       
		
		assertEquals(CLOSED_BY_SERVER, conn.getStatus());

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getOrCreateSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		assertEquals(CLOSED_BY_SERVER, conn.getStatus());
	}	

}
