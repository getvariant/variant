package com.variant.client.test;

import com.variant.client.Connection;
import com.variant.client.VariantClient;


public class ConnectionTest extends BaseTestWithServer {
	
	/**
	 * No Schema.
	 *  
	 * @throws Exception
	 */
	@org.junit.Test
	public void basicTest() throws Exception {
		
		
		VariantClient client = VariantClient.Factory.getInstance();
		Connection conn = client.getConnection("http://localhost:9000/test:big_covar_schema");
		
	}	
}