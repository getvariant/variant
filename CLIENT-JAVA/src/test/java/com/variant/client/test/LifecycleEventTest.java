package com.variant.client.test;

import static com.variant.core.ConnectionStatus.CLOSED_BY_CLIENT;
import static com.variant.core.ConnectionStatus.CLOSED_BY_SERVER;
import static com.variant.core.ConnectionStatus.OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Queue;

import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.ConnectionImpl;
import com.variant.client.lce.ConnectionClosed;
import com.variant.client.lce.ConnectionLifecycleEvent;
import com.variant.client.lce.UserHook;
import com.variant.core.ConnectionStatus;
import com.variant.core.util.IoUtils;
import com.variant.core.util.MutableInteger;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class LifecycleEventTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	/**
	 * Session Expiration
	 */
	@org.junit.Test
	public void sessionExpiredTest() throws Exception {
		Connection conn = client.getConnection("big_covar_schema");
		Session ssn = conn.getOrCreateSession(newSid());

		Thread.sleep(ssn.getTimeoutMillis());
	}

	/**
	 * Connection closed by client
	 */
	@org.junit.Test
	public void connectionClosedByClientTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");
		Session ssn = conn.getOrCreateSession(newSid());
		
		MutableInteger count = new MutableInteger(0);

		// Life cycle Listener
		conn.addLifecycleHook(
				new UserHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						count.increment();  // Will be posted
						Connection conn = event.getConnection();
						assertEquals(ConnectionStatus.CLOSED_BY_CLIENT, conn.getStatus());
						assertEquals(conn, conn);						
					}

				});

		// Another, which throws an exception.
		conn.addLifecycleHook(
				new UserHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						throw new RuntimeException("Foobar");					}
				});

		conn.addLifecycleHook(
				new UserHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						
						count.increment(); // Will be posted
						
						event.getConnection().addLifecycleHook(  // Will throw exception
								
								new UserHook<ConnectionClosed>() {
									
									@Override public Class<ConnectionClosed> getLifecycleEventClass() {
										return ConnectionClosed.class;
									}

									@Override public void post(ConnectionClosed event) {}												

								});
						}
				});

		
		conn.close();
		assertEquals(CLOSED_BY_CLIENT, conn.getStatus());
		assertNull(((ConnectionImpl)conn).getSessionCache().get(ssn.getId()));
		assertEquals(2, count.intValue());
		
		Queue<Throwable> asyncExceptions = ((ConnectionImpl)conn).getAsyncExceptions();
		assertEquals(2, asyncExceptions.size());
		assertEquals("Foobar", asyncExceptions.poll().getMessage());
		assertEquals(ClientUserError.CONNECTION_CLOSED.asMessage(), asyncExceptions.poll().getMessage());

	}	
	
	/**
	 * closed by server
	 */
	@org.junit.Test
	public void connectionClosedByServerTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");
		Session ssn = conn.getOrCreateSession(newSid());
		
		MutableInteger count = new MutableInteger(0);
		
		// Life cycle Listener
		conn.addLifecycleHook(
				new UserHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						Connection conn = event.getConnection();
						count.increment();  // Will be posted
						assertEquals(ConnectionStatus.CLOSED_BY_SERVER, conn.getStatus());
						assertEquals(conn, conn);
					}

				});

		// Another, which throws an exception.
		conn.addLifecycleHook(
				new UserHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						throw new RuntimeException("Foobar");					}
				});

		conn.addLifecycleHook(
				new UserHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						
						count.increment(); // Will be posted
						
						event.getConnection().addLifecycleHook(  // Will throw exception
								
								new UserHook<ConnectionClosed>() {
									
									@Override public Class<ConnectionClosed> getLifecycleEventClass() {
										return ConnectionClosed.class;
									}

									@Override public void post(ConnectionClosed event) {}												

								});
						}
				});

	    IoUtils.fileCopy("schemata-remote/big-covar-schema.json", SCHEMATA_DIR + "/big-covar-schema.json");
		Thread.sleep(dirWatcherLatencyMillis);

		assertEquals(OPEN, conn.getStatus());
		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn.getAttribute("foo");
			}
			
		}.assertThrown(ConnectionClosedException.class);
	
		Queue<Throwable> asyncExceptions = ((ConnectionImpl)conn).getAsyncExceptions();
		assertEquals(2, asyncExceptions.size());
		assertEquals("Foobar", asyncExceptions.poll().getMessage());
		assertEquals(ClientUserError.CONNECTION_CLOSED.asMessage(), asyncExceptions.poll().getMessage());
		
		assertEquals(CLOSED_BY_SERVER, conn.getStatus());
		assertNull(((ConnectionImpl)conn).getSessionCache().get(ssn.getId()));
		assertEquals(2, count.intValue());
	
	}	

}
