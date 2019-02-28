package com.variant.client.test;

import static com.variant.client.impl.ConfigKeys.SYS_PROP_TIMERS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.Optional;

import com.variant.client.Session;
import com.variant.client.SessionAttributeMap;
import com.variant.client.StateRequest;
import com.variant.client.VariantClient;
import com.variant.client.impl.ConnectionImpl;
import com.variant.client.impl.TraceEventSupport;
import com.variant.client.test.util.ClientBaseTestWithServerAsync;
import com.variant.client.util.Timers;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;
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
				SessionAttributeMap attr = new TimingWrapper<SessionAttributeMap>().withNoRemoteCalls().exec( () -> {
					return ssn.getAttributes();
				});
				
				// Working with attribute map is local
				String val = new TimingWrapper<String>().exec( () -> {
					String res = attr.get("foo");
					return res;
				});

				assertNull(val);

				val = new TimingWrapper<String>().exec( () -> {
					String res = attr.put("foo", "bar");
					return res;
				});

				assertNull(val);

				val = new TimingWrapper<String>().exec( () -> {
					String res = attr.get("foo");
					return res;
				});

				assertEquals("bar", val);

				val = new TimingWrapper<String>().exec( () -> {
					String res = attr.remove("foo");
					return res;
				});

				assertEquals("bar", val);

				StateRequest req = new TimingWrapper<StateRequest>().exec( () -> {
					State state = ssn.getSchema().getState("state" + ((_i % 5) + 1)).get();				
					return ssn.targetForState(state);
				});

				new TimingWrapper<Object>().withNoRemoteCalls().exec( () -> {
					req.getLiveExperiences();
					return null;
				});

				new TimingWrapper<Object>().exec( () -> {
					if (_i % 2 == 0) req.commit(); else req.fail();
					return null;
				});

				new TimingWrapper<Optional<? extends StateRequest>>().withNoRemoteCalls().exec( () -> {
					return ssn.getStateRequest();
				});

				// Trigger trace event
				new TimingWrapper<Object>().exec( () -> {
					ssn.triggerTraceEvent(TraceEventSupport.mkTraceEvent("foo"));
					return null;
				});

				new TimingWrapper<Set<Variation>>().exec( () -> {
					return ssn.getTraversedVariations();
				});

				new TimingWrapper<Set<Variation>>().exec( () -> {
					return ssn.getDisqualifiedVariations();
				});

				new TimingWrapper<Map<State, Integer>>().exec( () -> {
					return ssn.getTraversedStates();
				});

			});
		}
		
		joinAll();
		
	}
	
	/**
	 * Saving keystrokes...
	 */
	private static class TimingWrapper<T> {

		private int[] expectedLocalBounds = {0, 1000000};   // Microseconds
		private int[] expectedRemoteBounds = {0, 30000};   // Ditto
		private int expectedRemoteCount = 1;
		
		TimingWrapper() {}

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

			long remoteOpCount = Timers.remoteTimer.get().getAndClearCount();
			assertEquals(expectedRemoteCount, remoteOpCount);
			long remoteTime = Timers.remoteTimer.get().getAndClear();
			assertTrue("" + remoteTime + " was not >= " + expectedLocalBounds[0], remoteTime >= expectedLocalBounds[0]);
			assertTrue("" + remoteTime + " was not <= " + expectedRemoteBounds[1], remoteTime <= expectedRemoteBounds[1]);
			long localTime = Timers.localTimer.get().getAndClear();
			assertTrue("" + remoteTime + " was not < " + localTime, remoteOpCount == 0 ? true: remoteTime < localTime);
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
