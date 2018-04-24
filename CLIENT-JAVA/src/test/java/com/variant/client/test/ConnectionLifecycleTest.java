package com.variant.client.test;

import static com.variant.core.ConnectionStatus.CLOSED_BY_CLIENT;
import static org.junit.Assert.assertEquals;

import java.util.List;

import com.variant.client.Connection;
import com.variant.client.Connection.LifecycleListener;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.test.util.ClientLogTailer;
import com.variant.core.util.LogTailer;
import com.variant.core.util.LogTailer.Entry;
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
			throw new RuntimeException("Uncaught Exception");
		}
	};

	/**
	 * closed by client
	 */
	@org.junit.Test
	public void closedByClientTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");		
		Session ssn = conn.getOrCreateSession(newSid());
		
		conn.registerLifecycleListener(new GoodListener());
		conn.registerLifecycleListener(new BadListener());
		conn.registerLifecycleListener(new GoodListener());
	
		conn.close();
		assertEquals(CLOSED_BY_CLIENT, conn.getStatus());
		assertEquals(2, listenerCount.intValue());
		
		List<Entry> logLines = ClientLogTailer.last(1);
		assertEquals(
				ClientUserError.CONNECTION_LIFECYCLE_LISTENER_EXCEPTION.asMessage("com.variant.client.impl.ConnectionImpl$1"),
				logLines.get(0).message);

	}	
}
