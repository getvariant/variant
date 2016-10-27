package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Random;

import org.junit.Test;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.TestTargetingHook;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.ParserResponse;
import com.variant.core.xdm.Schema;
import com.variant.core.xdm.State;
import com.variant.core.xdm.Test.Experience;
import com.variant.core.xdm.impl.MessageTemplate;

public class TargetingTest extends BaseTestCore {

	static final int TRIALS = 500000;
	static final float DELTA_AS_FRACTION = .05f;

	/**
	 * Use the null event flusher and null session store, 
	 * because the test will be generating lots sessions and events.
	 */
	static {
		// Do nothing to save sessions to speed things up. We won't need them.
		injectorConfigAsResourceName = "/com/variant/core/conf/injector-session-store-null.json";
	}

	VariantCore core = rebootApi();
	
	/**
	 * Basic targeting
	 * @throws Exception
	 */
	@Test
	public void basicTest() throws Exception {
				
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
			    "        'parameters':{                                        \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
			    "     {  'name':'state2',                                      \n" +
			    "        'parameters':{                                        \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        }                                                     \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':1 ,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':2                                      \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':97                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                       'path':'/path/to/state1/test1.B'       \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                       'path':'/path/to/state1/test1.C'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = core.parseSchema(config);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		Schema schema = core.getSchema();		
		State state = schema.getState("state1");
		com.variant.core.xdm.Test test = schema.getTest("test1");

		//
		// No targeting listener - distribution according to weights.
		//
		int[] counts = {0, 0, 0};
		for (int i = 0; i < TRIALS; i++) {
			VariantCoreSession ssn = core.getSession("foo" + i, true).getBody();
			VariantCoreStateRequest req = ssn.targetForState(state);
			String expName = req.getLiveExperience(test).getName();
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
			req.commit();
		} 
		verifyCounts(counts, new float[] {1, 2, 97});
		
		//
		// Add one null listener - still distribution according to weights.
		//
		NullTargetingHookListener nullListener1 = new NullTargetingHookListener();
		nullListener1.postCount = 0;
		core.addHookListener(nullListener1);
		counts[0] = counts[1] = counts[2] = 0;
		for (int i = 0; i < TRIALS; i++) {
			VariantCoreSession ssn = core.getSession("foo" + i, true).getBody();
			VariantCoreStateRequest req = ssn.targetForState(state);
			String expName = req.getLiveExperience(test).getName();			
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
			req.commit();
		} 
		assertEquals(TRIALS, nullListener1.postCount);
		verifyCounts(counts, new float[] {1, 2, 97});

		//
		// Add two null listeners - still distribution according to weights.
		//
		NullTargetingHookListener nullListener2 = new NullTargetingHookListener();
		core.addHookListener(nullListener2);
		counts[0] = counts[1] = counts[2] = 0;
		nullListener2.postCount = nullListener1.postCount = 0;
		for (int i = 0; i < TRIALS; i++) {
			VariantCoreSession ssn = core.getSession("foo" + i, true).getBody();
			VariantCoreStateRequest req = ssn.targetForState(state);
			String expName = req.getLiveExperience(test).getName();			
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
			req.commit();
		} 
		assertEquals(TRIALS, nullListener1.postCount);
		assertEquals(TRIALS, nullListener2.postCount);
		verifyCounts(counts, new float[] {1, 2, 97});

		//
		// Add the A/B listener - changes distribution to 1/1/0.
		//
		ABTargetingHookListener ABListener = new ABTargetingHookListener();
		core.addHookListener(ABListener);
		counts[0] = counts[1] = counts[2] = 0;
		ABListener.postCount = nullListener2.postCount = nullListener1.postCount = 0;
		for (int i = 0; i < TRIALS; i++) {
			VariantCoreSession ssn = core.getSession("foo" + i, true).getBody();
			VariantCoreStateRequest req = ssn.targetForState(state);
			String expName = req.getLiveExperience(test).getName();			
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
			req.commit();
		} 
		assertEquals(TRIALS, nullListener1.postCount);
		assertEquals(TRIALS, nullListener2.postCount);
		assertEquals(TRIALS, ABListener.postCount);
		verifyCounts(counts, new float[] {1, 1, 0});

		//
		// Clear all listeners, then add the A/C listener - changes distribution to 1/0/1.
		//
		core.clearHookListeners();
		ACTargetingHookListener ACListener = new ACTargetingHookListener();
		core.addHookListener(ACListener);
		counts[0] = counts[1] = counts[2] = 0;		
		for (int i = 0; i < TRIALS; i++) {
			VariantCoreSession ssn = core.getSession("foo" + i, true).getBody();
			VariantCoreStateRequest req = ssn.targetForState(state);
			String expName = req.getLiveExperience(test).getName();	
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
			req.commit();
		} 
		assertEquals(TRIALS, ACListener.postCount);
		verifyCounts(counts, new float[] {1, 0, 1});

		//
		// Add null listener - should not change the distribution.
		//
		core.addHookListener(nullListener1);
		nullListener1.postCount = ACListener.postCount = 0;
		counts[0] = counts[1] = counts[2] = 0;		
		for (int i = 0; i < TRIALS; i++) {
			VariantCoreSession ssn = core.getSession("foo" + i, true).getBody();
			VariantCoreStateRequest req = ssn.targetForState(state);
			String expName = req.getLiveExperience(test).getName();	
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
			req.commit();
		} 
		assertEquals(TRIALS, nullListener1.postCount);
		assertEquals(TRIALS, ACListener.postCount);
		verifyCounts(counts, new float[] {1, 0, 1});

		//
		// Clear all listeners, then add the A/B/Null custom targeter + A/C targeter - changes distribution to 50/25/25.
		//
		core.clearHookListeners();
		ABNullTargetingHookListener ABNullListener = new ABNullTargetingHookListener();
		core.addHookListener(ABNullListener);
		core.addHookListener(ACListener);
		counts[0] = counts[1] = counts[2] = 0;
		ABNullListener.postCount = ACListener.postCount = 0;
		for (int i = 0; i < TRIALS; i++) {
			VariantCoreSession ssn = core.getSession("foo" + i, true).getBody();
			VariantCoreStateRequest req = ssn.targetForState(state);
			String expName = req.getLiveExperience(test).getName();	
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
			req.commit();
		} 
		assertEquals(TRIALS, ABNullListener.postCount);
		assertEquals(TRIALS, ACListener.postCount);
		verifyCounts(counts, new float[] {50, 25, 25});
	}
	
	/**
	 * 
	 * @param counts
	 * @param weights
	 */
	private static void verifyCounts(int[] counts, float[] weights) {
		float sumCounts = 0, sumWeights = 0;
		for (int i = 0; i < counts.length; i++) {
			sumCounts += counts[i];
			sumWeights += weights[i];
		}
		for (int i = 0; i < counts.length; i++) {
			//System.out.println("Delta: " + (weights[i]/sumWeights * DELTA_AS_FRACTION));
			assertEquals(weights[i]/sumWeights * DELTA_AS_FRACTION, counts[i]/sumCounts, weights[i]/sumWeights);
		}
	}

	/**
	 * Basic targeting
	 * @throws Exception
	 */
	@Test
	public void noWeightsExceptionTest() throws Exception {

		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
			    "        'parameters':{                                        \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
			    "     {  'name':'state2',                                      \n" +
			    "        'parameters':{                                        \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        }                                                     \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':1 ,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B'                                      \n" +
/*			    "              'weight':2                                      \n" + */
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':97                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                       'path':'/path/to/state1/test1.B'       \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                       'path':'/path/to/state1/test1.C'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		core.clearHookListeners();
		ParserResponse response = core.parseSchema(config);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		Schema schema = core.getSchema();		
		final State state = schema.getState("state1");
		final VariantCoreSession ssn = core.getSession("foo", true).getBody();

		new ExceptionInterceptor<VariantRuntimeException>() {
			@Override public void toRun() { ssn.targetForState(state); }
			@Override public void onThrown(VariantRuntimeException e) { 
				assertEquals(e.getMessage(), new VariantRuntimeUserErrorException(MessageTemplate.RUN_WEIGHT_MISSING, "test1", "B").getMessage()); 
			}
			@Override public Class<VariantRuntimeException> getExceptionClass() { return VariantRuntimeException.class; }
		}.assertThrown();

	}
	
	/**
	 * Always return null.
	 */
	private static class NullTargetingHookListener implements HookListener<TestTargetingHook> {
		
		private int postCount = 0;
		
		@Override
		public Class<TestTargetingHook> getHookClass() {
			return TestTargetingHook.class;
		}
		
		@Override
		public void post(TestTargetingHook hook) {
			postCount++;
			// do nothing.
		}
	}

	/**
	 * returns A or B in equal probabilities.
	 */
	private static class ABTargetingHookListener implements HookListener<TestTargetingHook> {

		private int postCount = 0;
		private static Random rand = new Random(System.currentTimeMillis());

		@Override
		public Class<TestTargetingHook> getHookClass() {
			return TestTargetingHook.class;
		}
		
		@Override
		public void post(TestTargetingHook hook) {
			postCount++;
			com.variant.core.xdm.Test test = hook.getTest();
			Experience experience = rand.nextBoolean() ? test.getExperience("A") : test.getExperience("B");
			hook.setTargetedExperience(experience);
		}
	}

	/**
	 * returns A or C in equal probabilities.
	 */
	private static class ACTargetingHookListener implements HookListener<TestTargetingHook> {
		
		private int postCount = 0;
		private static Random rand = new Random(System.currentTimeMillis());
		
		@Override
		public Class<TestTargetingHook> getHookClass() {
			return TestTargetingHook.class;
		}
		
		@Override
		public void post(TestTargetingHook hook) {
			postCount++;
			com.variant.core.xdm.Test test = hook.getTest();
			if (test.getName().equals("test1") && hook.getTargetedExperience() == null) {
				Experience experience = rand.nextBoolean() ? test.getExperience("A") : test.getExperience("C");
				hook.setTargetedExperience(experience);
			}
		}
	}

	/**
	 * returns A 25% of the time, B 25% of the time and null 50% of the time..
	 */
	private static class ABNullTargetingHookListener implements HookListener<TestTargetingHook> {
		
		private int postCount = 0;
		private static Random rand = new Random(System.currentTimeMillis());
		
		@Override
		public Class<TestTargetingHook> getHookClass() {
			return TestTargetingHook.class;
		}
		
		@Override
		public void post(TestTargetingHook hook) {
			postCount++;
			com.variant.core.xdm.Test test = hook.getTest();
			if (test.getName().equals("test1") && hook.getTargetedExperience() == null) {
				Experience experience = rand.nextBoolean() ? (rand.nextBoolean() ? test.getExperience("A") : test.getExperience("B")) : null;
				hook.setTargetedExperience(experience);
			}
		}

	}
	
	
}
