package com.variant.client.test;

import static com.variant.core.ConnectionStatus.CLOSED_BY_SERVER;
import static com.variant.core.ConnectionStatus.OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.VariantClientImpl;
import com.variant.core.schema.Schema;
import com.variant.core.util.IoUtils;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class ConnectionUndeployTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	/**
	 * Schema redeployed. 
	 */
	@org.junit.Test
	public void closedByServerUndeployTest() throws Exception {
	
		final Connection conn = client.getConnection("big_conjoint_schema");		
		assertNotNull(conn);
		assertEquals(OPEN, conn.getStatus());
		assertEquals(client, conn.getClient());
		Schema schema = conn.getSchema();
		assertNotNull(schema);
		assertEquals("big_conjoint_schema", conn.getSchema().getName());
		assertEquals(5, conn.getSchema().getStates().size());
		assertEquals(6, conn.getSchema().getTests().size());

		assertNull(conn.getSession("foo"));
		IoUtils.delete(SCHEMATA_DIR + "/big-conjoint-schema.json");
		Thread.sleep(dirWatcherLatencyMillis);

		assertEquals(OPEN, conn.getStatus());
		assertEquals(conn, ((VariantClientImpl)client).byId(conn.getId()));
		assertNotNull(conn.getId());
		assertEquals(schema, conn.getSchema());
		
		// Can't do anything over the connection
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		assertEquals(CLOSED_BY_SERVER, conn.getStatus());
		assertNull(((VariantClientImpl)client).byId(conn.getId()));

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSessionById("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getOrCreateSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSchema();
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		// Confirm the schema is gone.
		assertNull(client.getConnection("big_conjoint_schema"));

	}	

}
