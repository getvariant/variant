package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.Connection;
import com.variant.client.VariantClient;

public class ConnectionTest extends BaseTestWithServer {
	
	private VariantClient client = VariantClient.Factory.getInstance();

	/**
	 * Non existent schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void nonExistentSchemaTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:bad_schema");
		assertNotNull(conn);
		
	}	
	
	//@org.junit.Test
	public void goodSchemaTest() throws Exception {
		
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");		
		assertNotNull(conn);
		
	}	

}
