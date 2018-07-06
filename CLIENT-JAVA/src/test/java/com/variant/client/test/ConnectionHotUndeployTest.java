package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.UnknownSchemaException;
import com.variant.client.VariantClient;
import com.variant.core.impl.ServerError;
import com.variant.core.util.IoUtils;

/**
 */
public class ConnectionHotUndeployTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	/**
	 * Schema un-deployed. 
	 */
	@org.junit.Test
	public void schemaUndeployTest() throws Exception {
	
		// Connection must go after schema undeployed.
		final Connection conn1 = client.connectTo("big_conjoint_schema");		
		assertNotNull(conn1);
		assertEquals("big_conjoint_schema", conn1.getSchemaName());

		Session ssn1 = conn1.getOrCreateSession("foo");
		
		assertNotNull(ssn1);
		assertEquals(conn1.getSchemaName(), ssn1.getSchema().getName());

		IoUtils.delete(SCHEMATA_DIR + "/big-conjoint-schema.json");
		Thread.sleep(dirWatcherLatencyMillis);
		
		// Can't do anything over the connection
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ServerError.UnknownSchema, e.getError());
			}
			
		}.assertThrown(UnknownSchemaException.class);

		// Can't do anything over the connection
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getOrCreateSession("bar");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ServerError.UnknownSchema, e.getError());
			}
			
		}.assertThrown(UnknownSchemaException.class);

	}	

}
