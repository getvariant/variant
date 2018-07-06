package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.VariantClient;
import com.variant.client.impl.LifecycleService;
import com.variant.client.impl.VariantClientImpl;
import com.variant.client.lifecycle.LifecycleHook;
import com.variant.client.lifecycle.SessionExpiredLifecycleEvent;
import com.variant.core.util.CollectionsUtils;

/**
 * Test connections of a cold-deployed schemata.
 *
 */
public class LifecycleHookTest extends ClientBaseTestWithServer {
	
	// Sole client
	private VariantClient client = VariantClient.Factory.getInstance();		
	
	private LifecycleService lifecycleService = ((VariantClientImpl)client).lceService; 
	
	private ArrayList<String> hookPosts = new ArrayList<String>();

	private class SessionExpiredHook implements LifecycleHook<SessionExpiredLifecycleEvent> {
		
		final private Connection conn;
		SessionExpiredHook(Connection conn) {
			this.conn = conn;
		}
		@Override public Class<SessionExpiredLifecycleEvent> getLifecycleEventClass() {
			return SessionExpiredLifecycleEvent.class;
		}

		@Override public void post(SessionExpiredLifecycleEvent event) {
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
		/*					
		// 1, Connection-level SessionExpired Hook
		Connection conn1 = client.connectTo("big_conjoint_schema").get();
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
		*/
		// 2. session-level SessionExpired Hook
		hookPosts.clear();
		Connection conn1 = client.connectTo("big_conjoint_schema");
		Connection conn2 = client.connectTo("big_conjoint_schema");
		Session ssn1 = conn1.getOrCreateSession(newSid());
		Session ssn2 = conn1.getOrCreateSession(newSid());
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
				ssn4.targetForState(ssn4.getSchema().getState("state2"));
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

		Thread.sleep(ssn2.getTimeoutMillis());
				
		assertTrue(hookPosts.isEmpty());
		
		new ClientUserExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn6.clearAttribute("foo");
			}
						
		}.assertThrown(SessionExpiredException.class);

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

		lifecycleService.awaitAll();
		assertEqualAsLists(CollectionsUtils.list(ssn6.getId(), ssn6.getId(), ssn7.getId(), ssn7.getId(), ssn8.getId()), hookPosts);

	}

}
