package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.impl.SchemaImpl;
import com.variant.client.test.util.ClientBaseTestWithServer;
import com.variant.core.error.ServerError;
import com.variant.core.util.IoUtils;

/**
 */
public class ConnectionHotRedeployTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = new VariantClient.Builder()
			.withSessionIdTrackerClass(SessionIdTrackerHeadless.class)
			.withTargetingTrackerClass(TargetingTrackerHeadless.class)
			.build();
	
	/**
	 * Schema re-deployed. 
	 */
	@org.junit.Test
	public void schemaRedeployTest() throws Exception {
		
		restartServer();
		
		final Connection conn1 = client.connectTo("variant://localhost:5377/monstrosity");		
		assertNotNull(conn1);
		assertEquals("monstrosity", conn1.getSchemaName());

		Session ssn1 = conn1.getOrCreateSession("foo");
		
		assertNotNull(ssn1);
		assertEquals(conn1.getSchemaName(), ssn1.getSchema().getMeta().getName());
		
	    IoUtils.fileCopy(SCHEMATA_DIR_SRC + "monster.schema", SCHEMATA_DIR + "/monster.schema");
		Thread.sleep(dirWatcherLatencyMillis);

		// Session has expired by now.
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn1.getAttributes().put("foo", "bar");
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.SESSION_EXPIRED, e.error);
			}
			
		}.assertThrown();

		// Connection should be completely unaware of the schema redeploy.
		Session ssn2 = conn1.getOrCreateSession("foo");
		
		assertNotNull(ssn2);
		assertEquals(ssn1.getSchema().getMeta().getName(), ssn2.getSchema().getMeta().getName());
		assertNotEquals(((SchemaImpl)ssn1.getSchema()).getId(), ((SchemaImpl)ssn2.getSchema()).getId());		
		assertNotNull(ssn2.targetForState(ssn1.getSchema().getState("state3").get()));

		// New connection should work too
		Connection conn2 = client.connectTo("variant://localhost:5377/monstrosity");
		Session ssn3 = conn2.getOrCreateSession("bar");
		ssn3.getAttributes().put("foo", "bar");
		assertEquals("bar", ssn3.getAttributes().get("foo"));
	}
}
