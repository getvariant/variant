package com.variant.client.test;

import static com.variant.core.ConnectionStatus.CLOSED_BY_CLIENT;
import static com.variant.core.ConnectionStatus.CLOSED_BY_SERVER;
import static com.variant.core.ConnectionStatus.OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.VariantClient;
import com.variant.client.impl.ClientUserError;
import com.variant.client.impl.ConnectionImpl;
import com.variant.client.impl.VariantClientImpl;
import com.variant.client.lifecycle.ConnectionClosed;
import com.variant.client.lifecycle.LifecycleHook;
import com.variant.client.lifecycle.LifecycleService;
import com.variant.client.lifecycle.SessionExpired;
import com.variant.core.ConnectionStatus;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.IoUtils;
import com.variant.core.util.MutableInteger;
import com.variant.core.util.StringUtils;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class LifecycleHookTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	private LifecycleService lifecycleService = ((VariantClientImpl)client).lceService; 
	
	private ArrayList<String> hookPosts = new ArrayList<String>();

	private class SessionExpiredHook implements LifecycleHook<SessionExpired> {
		
		final private Connection conn;
		SessionExpiredHook(Connection conn) {
			this.conn = conn;
		}
		@Override public Class<SessionExpired> getLifecycleEventClass() {
			return SessionExpired.class;
		}

		@Override public void post(SessionExpired event) {
			System.out.println("********************** posted for sid " + event.getSession().getId());
			assertEquals(conn, event.getSession().getConnection());
			assertTrue(event.getSession().isExpired());
			hookPosts.add(event.getSession().getId());
		}

	};

	/**
	 * Session Expiration
	 */
	@org.junit.Test
	public void sessionExpiredTest() throws Exception {
							
		// 1, Connection-level SessionExpired Hook
		Connection conn1 = client.getConnection("big_covar_schema");
		conn1.addLifecycleHook(new SessionExpiredHook(conn1));

		Session ssn1 = conn1.getOrCreateSession(newSid());
		Session ssn2 = conn1.getOrCreateSession(newSid());

		Thread.sleep(ssn1.getTimeoutMillis());
		assertTrue(hookPosts.isEmpty());
		assertTrue(ssn1.isExpired());
		// ssn2 doesn't know yet it's expired.
		assertEqualAsSets(CollectionsUtils.set(ssn1.getId()), hookPosts);
		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn2.getStateRequest();
			}
			
		}.assertThrown(SessionExpiredException.class);
		
		lifecycleService.awaitAll();
		
		assertEqualAsSets(CollectionsUtils.set(ssn1.getId(), ssn2.getId()), hookPosts);
		
		// 2. session-level SessionExpired Hook
		hookPosts.clear();
		Connection conn2 = client.getConnection("big_covar_schema");
		Session ssn3 = conn2.getOrCreateSession(newSid());
		Session ssn4 = conn2.getOrCreateSession(newSid());
		Session ssn5 = conn2.getOrCreateSession(newSid());
		
		ssn3.addLifecycleHook(new SessionExpiredHook(conn2));
		ssn5.addLifecycleHook(new SessionExpiredHook(conn2));

		Thread.sleep(ssn3.getTimeoutMillis());
		assertTrue(hookPosts.isEmpty());

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn3.addLifecycleHook(new SessionExpiredHook(conn2));
			}
			
		}.assertThrown(SessionExpiredException.class);

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn4.targetForState(conn2.getSchema().getState("state2"));
			}
			
		}.assertThrown(SessionExpiredException.class);
		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn5.getAttribute("foo");
			}
			
		}.assertThrown(SessionExpiredException.class);

		lifecycleService.awaitAll();

		// ssn 4 doesn't have a hook registered.
		assertEqualAsSets(CollectionsUtils.set(ssn3.getId(), ssn5.getId()), hookPosts);

		// 3. Connection- and session-level SessionExpired Hook
		// (session level is posted last)

		hookPosts.clear();
		Session ssn6 = conn1.getOrCreateSession(newSid());
		Session ssn7 = conn1.getOrCreateSession(newSid());
		Session ssn8 = conn2.getOrCreateSession(newSid());
		ssn6.addLifecycleHook(new SessionExpiredHook(conn1));
		ssn7.addLifecycleHook(new SessionExpiredHook(conn1));
		ssn8.addLifecycleHook(new SessionExpiredHook(conn2));
		System.out.println("********************** sids " + ssn6.getId() + " " + ssn7.getId() + " " + ssn8.getId());

		Thread.sleep(ssn2.getTimeoutMillis());
				
		assertTrue(hookPosts.isEmpty());
		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn6.clearAttribute("foo");
			}
			
			@Override
			public void onThrown(ClientException.User e) {
				e.printStackTrace();
			}
			
		}.assertThrown(SessionExpiredException.class);
		/*
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn7.getDisqualifiedTests();
			}
			
		}.assertThrown(SessionExpiredException.class);

		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn8.getTraversedTests();
			}
			
		}.assertThrown(SessionExpiredException.class);
*/
		lifecycleService.awaitAll();
		Thread.sleep(2000);
		System.out.println("******** hookPosts: " + CollectionsUtils.toString(hookPosts));
		assertEqualAsLists(CollectionsUtils.list(ssn2.getId()), hookPosts);

	}

	/**
	 * Connection closed by client
	 */
	//@org.junit.Test
	public void connectionClosedByClientTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");
		Session ssn = conn.getOrCreateSession(newSid());
		
		AtomicInteger count = new AtomicInteger(0);

		// ConnectionClosed Hook
		conn.addLifecycleHook(
				new LifecycleHook<ConnectionClosed>() {
					
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
				new LifecycleHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						throw new RuntimeException("Foobar");					}
				});

		conn.addLifecycleHook(
				new LifecycleHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						
						count.incrementAndGet(); // Will be posted
						
						event.getConnection().addLifecycleHook(  // Will throw exception
								
								new LifecycleHook<ConnectionClosed>() {
									
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
		
		lifecycleService.awaitAll();
		
		assertEquals(2, count.intValue());
		assertEquals(2, lifecycleService.asyncExceptions.size());
		assertEquals("Foobar", lifecycleService.asyncExceptions.poll().getMessage());
		assertEquals(ClientUserError.CONNECTION_CLOSED.asMessage(), lifecycleService.asyncExceptions.poll().getMessage());

	}	
	
	/**
	 * closed by server
	 */
	////@org.junit.Test
	public void connectionClosedByServerTest() throws Exception {
		
		Connection conn = client.getConnection("big_covar_schema");
		Session ssn = conn.getOrCreateSession(newSid());
		
		MutableInteger count = new MutableInteger(0);
		
		// ConnectionClosed Hook
		conn.addLifecycleHook(
				new LifecycleHook<ConnectionClosed>() {
					
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
				new LifecycleHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						throw new RuntimeException("Foobar");					}
				});

		conn.addLifecycleHook(
				new LifecycleHook<ConnectionClosed>() {
					
					@Override public Class<ConnectionClosed> getLifecycleEventClass() {
						return ConnectionClosed.class;
					}

					@Override public void post(ConnectionClosed event) {
						
						count.increment(); // Will be posted
						
						event.getConnection().addLifecycleHook(  // Will throw exception
								
								new LifecycleHook<ConnectionClosed>() {
									
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

		lifecycleService.awaitAll();
		
		assertEquals(2, lifecycleService.asyncExceptions.size());
		assertEquals("Foobar", lifecycleService.asyncExceptions.poll().getMessage());
		assertEquals(ClientUserError.CONNECTION_CLOSED.asMessage(), lifecycleService.asyncExceptions.poll().getMessage());
		
		assertNull(((ConnectionImpl)conn).getSessionCache().get(ssn.getId()));
		assertEquals(2, count.intValue());
	
	}	

}
