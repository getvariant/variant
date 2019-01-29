package com.variant.client.test;

import static org.junit.Assert.*;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.impl.ConnectionImpl;

import static com.variant.client.impl.ConfigKeys.SYS_PROP_TIMERS;
import com.variant.client.test.util.ClientBaseTestWithServerAsync;
import com.variant.client.util.Timers;
import com.variant.core.schema.State;

public class TimersTest extends ClientBaseTestWithServerAsync {

	private int SESSIONS = 4;
	
	// Sole client
	private VariantClient client = new VariantClient.Builder()
			.withSessionIdTrackerClass(SessionIdTrackerHeadless.class)
			.withTargetingTrackerClass(TargetingTrackerHeadless.class)
			.build();

	@org.junit.Test
	public void timersTestNullString() throws Exception {
		
		System.setProperty(SYS_PROP_TIMERS, "");

		restartServer();
		
		assertTrue("".equals(System.getProperty(SYS_PROP_TIMERS)) );
		
		ConnectionImpl conn1 = (ConnectionImpl) client.connectTo("variant://localhost:5377/monstrosity");
		assertEquals(1, Timers.remoteCallCounter.get().value());
		assert(Timers.remoteTimer.get().value() > 0 && Timers.remoteTimer.get().value() < 100);
		assert(Timers.localTimer.get().value() > 0 && Timers.localTimer.get().value() < 400);
		
		Connection conn2 = client.connectTo("variant://localhost:5377/petclinic");	
		assertEquals(1, Timers.remoteCallCounter.get().value());
		assert(Timers.remoteTimer.get().value() > 0 && Timers.remoteTimer.get().value() < 100);
		assert(Timers.localTimer.get().value() > 0 && Timers.localTimer.get().value() < 400);
		
		// Create sessions in conn1
		Session[] sessions1 = new Session[SESSIONS];
		StateRequest[] requests1 = new StateRequest[SESSIONS];
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;  // Zhava. 
			async (() -> {
				assertTrue(Timers.localTimer.get().isStopped());
				assertTrue(Timers.remoteTimer.get().isStopped());
				Session ssn = conn1.getOrCreateSession(newSid());
				assertEquals(1, Timers.remoteCallCounter.get().value());
				assert(Timers.remoteTimer.get().value() > 0 && Timers.remoteTimer.get().value() < 100);
				System.out.println("**** " + Timers.remoteTimer.get().value());
				assert(Timers.localTimer.get().value() > 0 && Timers.localTimer.get().value() < 400);
				
				State state = ssn.getSchema().getState("state" + ((_i % 5) + 1)).get();				
				assertTrue(Timers.localTimer.get().isStopped());
				assertTrue(Timers.remoteTimer.get().isStopped());
				StateRequest req = ssn.targetForState(state);
				
			});
		}
		
		joinAll();
/*		
		for (int i = 0; i < SESSIONS; i++) {
			assertNotNull(sessions1[i]);
			assertNotNull(requests1[i]);
		}
		
		// Retrieve existing sessions
		Session[] sessions2 = new Session[SESSIONS];
		StateRequest[] requests2 = new StateRequest[SESSIONS];
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;  // Zhava. 
			async (() -> {
				String sid = sessions1[_i].getId();
				Session ssn = conn1.getSessionById(sid).get();
				assertEquals(sid, ssn.getId());
				assertNotEquals(ssn, sessions1[_i]);
				// Should be okay to use state from parallel schema.
				StateRequest req = ssn.getStateRequest().get();
				assertNotNull(req);
				assertEquals(req.getSession(), ssn);
				sessions2[_i] = ssn;
				requests2[_i] = req;
			});
		}

		// Create sessions in conn2
		Session[] sessions3 = new Session[SESSIONS];
		StateRequest[] requests3 = new StateRequest[SESSIONS];
		for (int i = 0; i < SESSIONS; i++) {
			final int _i = i;  // Zhava. 
			async (() -> {
				Session ssn = conn2.getOrCreateSession(newSid());
				State state = ssn.getSchema().getState("newVisit").get();
				// The qualifying and targeting hooks will throw an NPE
				// if user-agent attribute is not set.
				ssn.getAttributes().put("user-agent", "does not matter");
				StateRequest req = ssn.targetForState(state);
				assertNotNull(req);
				assertEquals(req.getSession(), ssn);
				sessions3[_i] = ssn;
				requests3[_i] = req;
			});
		}
		*/
	}
	
}
