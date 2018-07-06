package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.core.impl.ServerError;
import com.variant.core.util.IoUtils;

/**
 */
public class ConnectionHotRedeployTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	/**
	 * Schema re-deployed. 
	 */
	@org.junit.Test
	public void schemaRedeployTest() throws Exception {
		
		final Connection conn1 = client.connectTo("big_conjoint_schema");		
		assertNotNull(conn1);
		assertEquals("big_conjoint_schema", conn1.getSchemaName());

		Session ssn1 = conn1.getOrCreateSession("foo");
		
		assertNotNull(ssn1);
		assertEquals(conn1.getSchemaName(), ssn1.getSchema().getName());
		
	    IoUtils.fileCopy(SCHEMATA_DIR_SRC + "big-conjoint-schema.json", SCHEMATA_DIR + "/big-conjoint-schema.json");
		Thread.sleep(dirWatcherLatencyMillis);

		// Session has expired by now.
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn1.setAttribute("foo", "bar");
			}
			
			@Override public void onThrown(ClientException e) {
				assertEquals(ServerError.SessionExpired, e.getError());
			}
			
		}.assertThrown();

		// Connection should be completely unaware of the schema redeploy.
		Session ssn2 = conn1.getOrCreateSession("foo");
		
		assertNotNull(ssn2);
		assertEquals(ssn1.getSchema().getName(), ssn2.getSchema().getName());
		assertNotEquals(ssn1.getSchema().getId(), ssn2.getSchema().getId());		
		assertNotNull(ssn2.targetForState(ssn1.getSchema().getState("state3")));

		// New connection should work too
		Connection conn2 = client.connectTo("big_conjoint_schema");
		Session ssn3 = conn2.getOrCreateSession("bar");
		ssn3.setAttribute("foo", "bar");
		assertEquals("bar", ssn3.getAttribute("foo"));
	}
}
