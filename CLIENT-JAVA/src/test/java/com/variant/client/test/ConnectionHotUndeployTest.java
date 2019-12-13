package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.VariantException;
import com.variant.client.test.util.ClientBaseTestWithServer;
import com.variant.share.error.ServerError;
import com.variant.share.util.IoUtils;

/**
 */
public class ConnectionHotUndeployTest extends ClientBaseTestWithServer {
		
	/**
	 * Schema un-deployed. 
	 */
	@org.junit.Test
	public void schemaUndeployTest() throws Exception {
	
		restartServer();

		// Connection must go after schema undeployed.
		final Connection conn1 = client.connectTo("variant://localhost:5377/monstrosity");		
		assertNotNull(conn1);
		assertEquals("monstrosity", conn1.getSchemaName());

		Session foo1 = conn1.getOrCreateSession("foo");
		
		assertNotNull(foo1);
		assertEquals(conn1.getSchemaName(), foo1.getSchema().getMeta().getName());
      assertTrue(conn1.getSession(foo1.getId()).isPresent());

      IoUtils.delete(SCHEMATA_DIR + "/monster.schema");
		Thread.sleep(dirWatcherLatencyMillis);
				
      // Can't access expired session
      assertFalse(conn1.getSession(foo1.getId()).isPresent());

      // Can't create another session
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getOrCreateSession("bar");
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.UNKNOWN_SCHEMA, e.error);
			}
			
		}.assertThrown();

      // Can't create another connection.
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				client.connectTo("variant://localhost:5377/monstrosity");
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.UNKNOWN_SCHEMA, e.error);
			}
			
		}.assertThrown();
		
		// Petclinic should be fine.
		assertNotNull(client.connectTo("variant://localhost:5377/petclinic"));

	}	

}
