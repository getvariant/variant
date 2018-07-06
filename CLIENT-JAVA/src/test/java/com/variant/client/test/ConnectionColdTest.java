package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.UnknownSchemaException;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.ConnectionImpl;

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
		
		assertNull(client.connectTo("bad_schema"));
	
	}	
	
	/**
	 */
	@org.junit.Test
	public void connectToExistingSchemataTest() throws Exception {
			    
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

	}	

	/**
	 * Server restarted with the same schema. 
	 */
	@org.junit.Test
	public void closedByServerRestartTest() throws Exception {
		
		final Connection conn = client.connectTo("big_conjoint_schema");		
		assertNotNull(conn);

		server.stop();

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSession("foo");
			}
			
			@Override public void onThrown(ClientException e) {
				assertEquals(ClientUserError.SERVER_CONNECTION_TIMEOUT, e.getError());
			}
			
		}.assertThrown();       

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSessionById("foo");
			}
			
			@Override public void onThrown(ClientException e) {
				assertEquals(ClientUserError.SERVER_CONNECTION_TIMEOUT, e.getError());
			}
			
		}.assertThrown(UnknownSchemaException.class);       
		

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getOrCreateSession("foo");
			}
			
			@Override public void onThrown(ClientException e) {
				assertEquals(ClientUserError.SERVER_CONNECTION_TIMEOUT, e.getError());
			}
			
		}.assertThrown(UnknownSchemaException.class);

	}	

}
