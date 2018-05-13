package com.variant.client.test;

import static com.variant.core.ConnectionStatus.CLOSED_BY_CLIENT;
import static com.variant.core.ConnectionStatus.CLOSED_BY_SERVER;
import static com.variant.core.ConnectionStatus.OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.ConnectionImpl;
import com.variant.client.impl.LifecycleEventService;
import com.variant.client.impl.VariantClientImpl;
import com.variant.client.lce.ConnectionClosed;
import com.variant.client.lce.SessionExpired;
import com.variant.client.lce.UserHook;
import com.variant.core.ConnectionStatus;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.IoUtils;
import com.variant.core.util.MutableInteger;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class LifecycleHookTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	private LifecycleEventService lceService = ((VariantClientImpl)client).lceService; 
	
	/**
	 * Session Expiration
	 */
	@org.junit.Test
	public void sessionExpiredTest() throws Exception {
		Connection conn = client.getConnection("big_covar_schema");
		
		HashSet<String> hookPosts = new HashSet<String>();
	
		// Connection-level SessionExpired Hook
		conn.addLifecycleHook(
				new UserHook<SessionExpired>() {
					
					@Override public Class<SessionExpired> getLifecycleEventClass() {
						return SessionExpired.class;
					}

					@Override public void post(SessionExpired event) {
						assertEquals(conn, event.getSession().getConnection());
						assertTrue(event.getSession().isExpired());
						hookPosts.add(event.getSession().getId());
					}

				});

		Session ssn = conn.getOrCreateSession(newSid());

		Thread.sleep(ssn.getTimeoutMillis());
		assertTrue(hookPosts.isEmpty());
		assertTrue(ssn.isExpired());
		assertEqualAsSets(CollectionsUtils.set(ssn.getId()), hookPosts);

		
	}

	/**
	 * Connection closed by client
	 */
	@org.junit.Test
	public void connectionClosedByClientTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");
		Session ssn = conn.getOrCreateSession(newSid());
		
		AtomicInteger count = new AtomicInteger(0);

		// ConnectionClosed Hook
		conn.addLifecycleHook(
				new UserHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						count.incrementAndGet();  // Will be posted
						Connection conn = event.getConnection();
						assertEquals(ConnectionStatus.CLOSED_BY_CLIENT, conn.getStatus());
						assertEquals(conn, event.getConnection());						
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
						
						count.incrementAndGet(); // Will be posted
						
						event.getConnection().addLifecycleHook(  // Will throw exception
								
								new UserHook<ConnectionClosed>() {
									
									@Override public Class<ConnectionClosed> getLifecycleEventClass() {
										return ConnectionClosed.class;
									}

									@Override public void post(ConnectionClosed event) {
										count.incrementAndGet();  // should not get here!
									}												

								});
						}
				});

		
		conn.close();
		assertEquals(CLOSED_BY_CLIENT, conn.getStatus());
		assertNull(((ConnectionImpl)conn).getSessionCache().get(ssn.getId()));
		
		lceService.awaitAll();
		
		assertEquals(2, count.intValue());
		assertEquals(2, lceService.asyncExceptions.size());
		assertEquals("Foobar", lceService.asyncExceptions.poll().getMessage());
		assertEquals(ClientUserError.CONNECTION_CLOSED.asMessage(), lceService.asyncExceptions.poll().getMessage());

	}	
	
	/**
	 * closed by server
	 */
	//@org.junit.Test
	public void connectionClosedByServerTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");
		Session ssn = conn.getOrCreateSession(newSid());
		
		MutableInteger count = new MutableInteger(0);
		
		// ConnectionClosed Hook
		conn.addLifecycleHook(
				new UserHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						Connection conn = event.getConnection();
						count.increment();  // Will be posted
						assertEquals(ConnectionStatus.CLOSED_BY_SERVER, conn.getStatus());
						assertEquals(conn, event.getConnection());
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

									@Override public void post(ConnectionClosed event) {
										count.increment();  // should not get here!
									}												

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
	    
		assertEquals(CLOSED_BY_SERVER, conn.getStatus());

		lceService.awaitAll();
		
		assertEquals(2, lceService.asyncExceptions.size());
		assertEquals("Foobar", lceService.asyncExceptions.poll().getMessage());
		assertEquals(ClientUserError.CONNECTION_CLOSED.asMessage(), lceService.asyncExceptions.poll().getMessage());
		
		assertNull(((ConnectionImpl)conn).getSessionCache().get(ssn.getId()));
		assertEquals(2, count.intValue());
	
	}	

}
