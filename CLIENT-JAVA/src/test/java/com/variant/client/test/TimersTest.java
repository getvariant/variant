package com.variant.client.test;

import static com.variant.client.impl.ConfigKeys.SYS_PROP_TIMERS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.impl.ConnectionImpl;
import com.variant.client.test.util.ClientBaseTestWithServerAsync;
import com.variant.client.util.Timers;
import com.variant.core.schema.State;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.Tuples.Pair;

public class TimersTest extends ClientBaseTestWithServerAsync {

	private int SESSIONS = 20;
	
	// Sole client
	private VariantClient client = new VariantClient.Builder()
			.withSessionIdTrackerClass(SessionIdTrackerHeadless.class)
			.withTargetingTrackerClass(TargetingTrackerHeadless.class)
			.build();

	@org.junit.Test
	public void timersTestNullString() throws Exception {
		
		System.setProperty(SYS_PROP_TIMERS, "");

		restartServer(CollectionsUtils.pairsToMap(new Pair<String,String>("variant.with.timing", "true")));
		
		assertTrue("".equals(System.getProperty(SYS_PROP_TIMERS)) );
		
		ConnectionImpl conn = new TimingWrapper<ConnectionImpl>().exec( () -> {
			return (ConnectionImpl) client.connectTo("variant://localhost:5377/monstrosity");
		});

		for (int i = 0; i < SESSIONS; i++) {
			int _i = i;  // Zhava. 
			async (() -> {

				// Create sessions
				Session ssn = new TimingWrapper<Session>().exec( () -> {
					return conn.getOrCreateSession(newSid());
				});
				
				// Get session attribute map.
				Map<String,String> attr = new TimingWrapper<Map<String,String>>().exec( () -> {
					return ssn.getAttributes();
				});
				
				// Working with attribute map is local
				String val = new TimingWrapper<String>().withNoRemoteCalls().exec( () -> {
					System.out.println("********* " + Timers.remoteTimer.get().getAndClearCount());
					String res = attr.get("foo");
					System.out.println("********* " + Timers.remoteTimer.get().getAndClearCount());
					return res;
				});

				assertNull(val);

				StateRequest req = new TimingWrapper<StateRequest>().exec( () -> {
					State state = ssn.getSchema().getState("state" + ((_i % 5) + 1)).get();				
					return ssn.targetForState(state);
				});
				
			});
		}
		
		joinAll();
		
	}
	
	/**
	 * Saving keystrokes...
	 */
	private static class TimingWrapper<T> {

		private int[] expectedLocalBounds = {0, 300};
		private int[] expectedRemoteBounds = {0, 100};
		private int expectedRemoteCount = 1;
				
		TimingWrapper(int[] localBounds, int[] remoteBounds, int remoteCount) {
			this.expectedLocalBounds = localBounds;
			this.expectedRemoteBounds = remoteBounds;
			this.expectedRemoteCount = remoteCount;
		}

		TimingWrapper() {
			this(new int[] {0, 300}, new int[] {0, 100}, 1);
		}

		TimingWrapper<T> withNoRemoteCalls() {
			expectedRemoteBounds = new int[] {0, 0};
			expectedRemoteCount = 0;
			return this;
		}
		
		T exec(Operation<T> op) {
							
			assertTrue(Timers.localTimer.get().isStopped());
			assertTrue(Timers.remoteTimer.get().isStopped());
			
			Timers.localTimer.get().reset();
			Timers.remoteTimer.get().reset();

			T result = op.apply();

			assertEquals(expectedRemoteCount, Timers.remoteTimer.get().getAndClearCount());
			long remoteTime = Timers.remoteTimer.get().getAndClear();
			assertTrue(remoteTime >= expectedLocalBounds[0]);
			assertTrue(remoteTime <= expectedRemoteBounds[1]);
			long localTime = Timers.localTimer.get().getAndClear();
			assertTrue("" + localTime + " was not >= " + expectedLocalBounds[0], localTime >= expectedLocalBounds[0]);
			assertTrue("" + localTime + " was not <= " + expectedLocalBounds[1], localTime <= expectedLocalBounds[1]);

			return result;
		}
		
		@FunctionalInterface
		static interface Operation<V> {
			V apply();
		}
	}

}
