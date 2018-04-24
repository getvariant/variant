package com.variant.client.test;

import static com.variant.core.ConnectionStatus.*;
import static org.junit.Assert.*;

import java.util.List;

import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.ConnectionImpl;
import com.variant.client.test.ClientBaseTest.ClientUserExceptionInterceptor;
import com.variant.client.test.util.ClientLogTailer;
import com.variant.core.ConnectionStatus;
import com.variant.core.ServerError;
import com.variant.core.util.LogTailer.Entry;
import com.variant.core.util.IoUtils;
import com.variant.core.util.MutableInteger;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class ConnectionLifecycleTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		


	/**
	 * closed by client
	 */
	@org.junit.Test
	public void closedByClientTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");
		Session ssn = conn.getOrCreateSession(newSid());
		
		MutableInteger count = new MutableInteger(0);

		// Life cycle Listener
		conn.registerLifecycleListener(
				(Connection connection) -> {
					count.increment();  // Will be posted
					assertEquals(ConnectionStatus.CLOSED_BY_CLIENT, connection.getStatus());
					assertEquals(conn, connection);
		});

		// Another, which throws an exception.
		conn.registerLifecycleListener(
				(Connection connection) -> {
					throw new RuntimeException("Runtime Exception");
		});

		conn.registerLifecycleListener(
				(Connection connection) -> {
					count.increment(); // Will be posted
					connection.registerLifecycleListener(  // Will throw exception
							(Connection connection2) -> {
								 // Nothing
					});
		});
		
		conn.close();
		assertEquals(CLOSED_BY_CLIENT, conn.getStatus());
		assertNull(((ConnectionImpl)conn).getSessionCache().get(ssn.getId()));
		assertEquals(2, count.intValue());
		
		List<Entry> logLines = ClientLogTailer.last(1);
		assertEquals(
				ClientUserError.CONNECTION_LIFECYCLE_LISTENER_EXCEPTION.asMessage("com.variant.client.impl.ConnectionImpl$1"),
				logLines.get(0).message);

	}	
	
	/**
	 * closed by server
	 */
	@org.junit.Test
	public void closedByServerTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");
		Session ssn = conn.getOrCreateSession(newSid());
		
		MutableInteger count = new MutableInteger(0);

		// Life cycle Listener
		conn.registerLifecycleListener(
				(Connection connection) -> {
					count.increment();  // Will be posted
					assertEquals(ConnectionStatus.CLOSED_BY_CLIENT, connection.getStatus());
					assertEquals(conn, connection);
		});

		// Another, which throws an exception.
		conn.registerLifecycleListener(
				(Connection connection) -> {
					throw new RuntimeException("Runtime Exception");
		});

		conn.registerLifecycleListener(
				(Connection connection) -> {
					count.increment(); // Will be posted
					connection.registerLifecycleListener(  // Will throw exception
							(Connection connection2) -> {
								 // Nothing
					});
		});
		

	    IoUtils.fileCopy("schemata-remote/big-covar-schema.json", SCHEMATA_DIR + "/big-covar-schema.json");
		Thread.sleep(dirWatcherLatencyMsecs);

		assertEquals(OPEN, conn.getStatus());
		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn.getAttribute("foo");
			}
			
		}.assertThrown(ConnectionClosedException.class);
		
		assertEquals(CLOSED_BY_SERVER, conn.getStatus());
		assertNull(((ConnectionImpl)conn).getSessionCache().get(ssn.getId()));
		assertEquals(2, count.intValue());
		
		List<Entry> logLines = ClientLogTailer.last(1);
		assertEquals(
				ClientUserError.CONNECTION_LIFECYCLE_LISTENER_EXCEPTION.asMessage("com.variant.client.impl.ConnectionImpl$1"),
				logLines.get(0).message);

	}	

}
