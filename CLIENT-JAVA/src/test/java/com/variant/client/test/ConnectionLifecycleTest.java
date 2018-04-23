package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.variant.client.ClientException;
import com.variant.client.Connection;

import static com.variant.core.ConnectionStatus.*;

import com.variant.client.ConnectionClosedException;
import com.variant.client.Connection.LifecycleListener;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.VariantClientImpl;
import com.variant.core.ServerError;
import com.variant.core.util.MutableInteger;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class ConnectionLifecycleTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	private final MutableInteger listenerCount = new MutableInteger(0);
	
    private class GoodListener implements LifecycleListener {	
		@Override
		public void onClosed(Connection connection) {
			listenerCount.increment();
		}
	};

    private class BadListener implements LifecycleListener {	
		@Override
		public void onClosed(Connection connection) {
			throw new RuntimeException("UncaughtException");
		}
	};

	/**
	 * closed by client
	 */
	@org.junit.Test
	public void closedByClientTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");		
		Session ssn = conn.getOrCreateSession(newSid());
		
		



		// Good listener 1
		conn.registerLifecycleListener(
				new LifecycleListener() {
					@Override
					public void onClosed(Connection connection) {
						listenerCount.increment();
					}
				});
		
		conn.close();
		assertEquals(CLOSED_BY_CLIENT, conn.getStatus());
		assertEquals(1, listenerCount);
		
		// Exception on a subsequent close.
		// Throw user error exception when trying to use this connection.
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.close();
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);

		assertEquals(CLOSED_BY_CLIENT, conn.getStatus());

		// Throw user error exception when trying to use this connection.
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				conn.getSession("foo");
			}
			
			@Override public void onThrown(ClientException.User e) {
				assertEquals(ClientUserError.CONNECTION_CLOSED, e.getError());
			}
			
		}.assertThrown(ConnectionClosedException.class);
		
		assertEquals(CLOSED_BY_CLIENT, conn.getStatus());
	}	
}
