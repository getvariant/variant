package com.variant.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.variant.client.test.util.ClientBaseTestWithServer;
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
		
		final private Session ssn;
		SessionExpiredHook(Session ssn) {
			this.ssn = ssn;
		}
		@Override public Class<SessionExpiredLifecycleEvent> getLifecycleEventClass() {
			return SessionExpiredLifecycleEvent.class;
		}

		@Override public void post(SessionExpiredLifecycleEvent event) {
			assertEquals(ssn.getId(), event.getSession().getId());
			assertTrue(event.getSession().isExpired());
			hookPosts.add(event.getSession().getId());
		}

	};

	/**
	 * Session expired remotely, i.e. the SessionExpiredLifecycleEvent raised by Server, not by Vacuumer.
	 */
	@org.junit.Test
	public void sessionExpiredRemotelyTest() throws Exception {
		
		restartServer();
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
		//System.out.println(CollectionsUtils.toString(CollectionsUtils.list(ssn1.getId(),ssn2.getId(),ssn3.getId(),ssn4.getId(),ssn5.getId())));
		ssn1.addLifecycleHook(new SessionExpiredHook(ssn1));
		ssn2.addLifecycleHook(new SessionExpiredHook(ssn2));

		// Enough to expire on the server, but not yet to trip the local vacuum.
		Thread.sleep(ssn3.getTimeoutMillis());

		// Sessions don't know yet tye're expired.
		assertFalse(ssn1.isExpired());
		assertFalse(ssn2.isExpired());
		assertFalse(ssn3.isExpired());
		assertFalse(ssn4.isExpired());
		assertFalse(ssn5.isExpired());
		
		assertTrue(hookPosts.isEmpty());
		
		// Second hook for ssn2 - should work.
		ssn2.addLifecycleHook(new SessionExpiredHook(ssn2));

		// Expires ssn2 and posts 2 hooks.
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn2.targetForState(ssn2.getSchema().getState("state2"));
			}
			
		}.assertThrown(SessionExpiredException.class);
		
		// Expires ssn5, but no hooks
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn5.getAttribute("foo");
			}
			
		}.assertThrown(SessionExpiredException.class);

		lifecycleService.awaitAll();

		assertFalse(ssn1.isExpired());
		assertTrue(ssn2.isExpired());
		assertFalse(ssn3.isExpired());
		assertFalse(ssn4.isExpired());
		assertTrue(ssn5.isExpired());
		
		//System.out.println("*********** " + CollectionsUtils.toString(hookPosts));
		// ssn1 has the hook, but isn't yet expired, ssn2 has 2 hooks which should have been posted, 
		// and ssn5 doesn't have a hook registered.
		assertEqualAsSets(CollectionsUtils.list(ssn2.getId(), ssn2.getId()), hookPosts);

		// Can't add hooks to expired sessions.
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn2.addLifecycleHook(new SessionExpiredHook(ssn2));
			}
			
		}.assertThrown(SessionExpiredException.class);

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn5.addLifecycleHook(new SessionExpiredHook(ssn2));
			}
			
		}.assertThrown(SessionExpiredException.class);

		hookPosts.clear();						
		assertTrue(hookPosts.isEmpty());
		
		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn1.clearAttribute("foo");
			}
						
		}.assertThrown(SessionExpiredException.class);

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn2.getDisqualifiedTests();
			}
			
		}.assertThrown(SessionExpiredException.class);

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn3.getTraversedTests();
			}
			
		}.assertThrown(SessionExpiredException.class);

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn4.setAttribute("foo", "bar");
			}
			
		}.assertThrown(SessionExpiredException.class);

		new ClientExceptionInterceptor() {
			
			@Override public void toRun() {
				ssn5.getAttribute("foo");
			}
			
		}.assertThrown(SessionExpiredException.class);

		lifecycleService.awaitAll();
		// ssn2 and ssn5 have already fired.
		//System.out.println("*********** " + CollectionsUtils.toString(hookPosts));
		assertEqualAsLists(CollectionsUtils.list(ssn1.getId()), hookPosts);

	}

}
