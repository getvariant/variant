package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.VariantClient;

public class ConnectionTest extends BaseTestWithServer {
	
	private VariantClient client = VariantClient.Factory.getInstance();

	/**
	 * Non existent schema.
	 */
	@org.junit.Test
	public void nonExistentSchemaTest() throws Exception {
		
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
	 * Good schema.
	 */
	@org.junit.Test
	public void goodSchemaTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		
	}	

	/**
	 * TODO: revive this after close.
	 */
	//@org.junit.Test
	public void tooManyConnectionsTest() {
		long now = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
    		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
    		assertNotNull(conn);        	
        }
        System.out.println("***************** " + (System.currentTimeMillis() - now));
	}

}
