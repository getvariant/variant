package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.Connection;
import com.variant.client.ServerConnectException;
import com.variant.client.Session;
import com.variant.client.UnknownSchemaException;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.ConnectionImpl;
import com.variant.client.test.util.ClientBaseTestWithServer;
import com.variant.core.impl.ServerError;

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

		restartServer();
		
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				client.connectTo("bad_schema");
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(
						ServerError.UNKNOWN_SCHEMA.asMessage("bad_schema"), 
						e.getMessage());
			}
			
		}.assertThrown(UnknownSchemaException.class);       
	
	}	
	
	/**
	 */
	@org.junit.Test
	public void connectToExistingSchemataTest() throws Exception {
	
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

		// Retrieve good session over wrong connection
		Session ssn = conn2.getOrCreateSession("foo");
		assertNotEquals("foo", ssn.getId());
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				conn3.getSession(ssn.getId());
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(
						ServerError.WRONG_CONNECTION.asMessage("petclinic"), 
						e.getMessage());
			}
			
		}.assertThrown();       
	}	

	/**
	 * Server shutdown. 
	 */
	@org.junit.Test
	public void serverDownTest() throws Exception {
	
		restartServer();
		
		final Connection conn = client.connectTo("monstrosity");		
		assertNotNull(conn);
		final Session ssn = conn.getOrCreateSession("foo");
		assertNotNull(ssn);
		
		stopServer();

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSession("foo");
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(
						ClientUserError.SERVER_CONNECTION_TIMEOUT.asMessage("localhost"), 
						e.getMessage());
			}
			
		}.assertThrown(ServerConnectException.class);       

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSessionById("foo");
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(
						ClientUserError.SERVER_CONNECTION_TIMEOUT.asMessage("localhost"), 
						e.getMessage());
			}
			
		}.assertThrown(ServerConnectException.class);       
		

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getOrCreateSession("bar");
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(
						ClientUserError.SERVER_CONNECTION_TIMEOUT.asMessage("localhost"), 
						e.getMessage());
			}
			
		}.assertThrown(ServerConnectException.class);

		// Should be back in business after server restarts.
		restartServer();
		assertFalse(conn.getSession("foo").isPresent());
		Session ssn2 = conn.getOrCreateSession("foo");
		assertNotNull(ssn2);
		assertNotNull(conn.getSession(ssn2.getId()));
		assertNotNull(conn.getSessionById(ssn2.getId()));
	}	

}
