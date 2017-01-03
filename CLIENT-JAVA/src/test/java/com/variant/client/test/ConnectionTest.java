package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Connection.Status;
import com.variant.client.VariantClient;

public class ConnectionTest extends BaseTestWithServer {
	
	private VariantClient client = VariantClient.Factory.getInstance();

	/**
	 */
	@org.junit.Test
	public void connectToNonExistentSchemaTest() throws Exception {
		
		new ClientExceptionInterceptor() {
			
			Connection conn = null;

			@Override public void toRun() {
				conn = client.getConnection("http://localhost:9000/test:bad_schema");
			}
			
			@Override public void onThrown(ClientException e) {
				assertNull(conn);
			}
		}.assertThrown();
		
	}	
	
	/**
	 */
	@org.junit.Test
	public void connectToGoodSchemaTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
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
	 * @throws Exception 
	 */
	@org.junit.Test
	public void tooManyConnectionsTest() throws Exception {

        for (int i = 0; i < 10; i++) {
    		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
    		assertNotNull(conn);        	
        }
		
		new ClientExceptionInterceptor() {
			
			Connection conn = null;

			@Override public void toRun() {
				conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
			}
			
			@Override public void onThrown(ClientException e) {
				assertNull(conn);
			}
		}.assertThrown();

	}

}
