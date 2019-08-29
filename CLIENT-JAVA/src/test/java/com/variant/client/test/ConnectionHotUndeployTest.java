package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.test.util.ClientBaseTestWithServer;
import com.variant.core.error.ServerError;
import com.variant.core.util.IoUtils;

/**
 */
public class ConnectionHotUndeployTest extends ClientBaseTestWithServer {
	
	// Sole client
	// Sole client
	private VariantClient client = new VariantClient.Builder()
			.withSessionIdTrackerClass(SessionIdTrackerHeadless.class)
			.withTargetingTrackerClass(TargetingTrackerHeadless.class)
			.build();
	
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
		
		// Session should be timed out by now => connection is gone.
		
      // Can't access expired session
      new ClientExceptionInterceptor() {
         
         @Override public void toRun() {
            conn1.getSession("foo").isPresent();
         }
         
         @Override public void onThrown(VariantException e) {
            assertEquals(ServerError.UNKNOWN_SCHEMA, e.getError());
         }
         
      }.assertThrown();

      // Can't create another session
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				conn1.getOrCreateSession("bar");
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.UNKNOWN_SCHEMA, e.getError());
			}
			
		}.assertThrown();

      // Can't create another connection.
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				client.connectTo("variant://localhost:5377/monstrosity");
			}
			
			@Override public void onThrown(VariantException e) {
				assertEquals(ServerError.UNKNOWN_SCHEMA, e.getError());
			}
			
		}.assertThrown();
		
		// Petclinic should be fine.
		assertNotNull(client.connectTo("variant://localhost:5377/petclinic"));

	}	

}
