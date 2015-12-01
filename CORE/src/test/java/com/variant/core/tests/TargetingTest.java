package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.flashpoint.FlashpointListener;
import com.variant.core.flashpoint.TestTargetingFlashpoint;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.ParserResponse;

public class TargetingTest extends BaseTest {

	static final int TRIALS = 1000000;
	static final float DELTA_AS_FRACTION = .025f;

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
		
		engine.clearFlashpointListeners();
		ParserResponse response = engine.parseSchema(config);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		Schema schema = engine.getSchema();		
		State state = schema.getState("state1");
		com.variant.core.schema.Test test = schema.getTest("test1");
		VariantSession ssn = engine.getSession("foo");

		//
		// No targeting listener - distribution according to weights.
		//
		int[] counts = {0, 0, 0};
		for (int i = 0; i < TRIALS; i++) {
			VariantStateRequest req = engine.newStateRequest(ssn, state, "");
			String expName = req.getTargetedExperience(test).getName();
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
		} 
		verifyCounts(counts, new float[] {1, 2, 97});

		//
		// Add one null listener - still distribution according to weights.
		//
		NullTargetingFlashpointListener nullListener1 = new NullTargetingFlashpointListener();
		nullListener1.postCount = 0;
		engine.addFlashpointListener(nullListener1);
		counts[0] = counts[1] = counts[2] = 0;
		for (int i = 0; i < TRIALS; i++) {
			VariantStateRequest req = engine.newStateRequest(ssn, state, "");
			String expName = req.getTargetedExperience(test).getName();			
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
		} 
		assertEquals(TRIALS, nullListener1.postCount);
		verifyCounts(counts, new float[] {1, 2, 97});

		//
		// Add two null listeners - still distribution according to weights.
		//
		NullTargetingFlashpointListener nullListener2 = new NullTargetingFlashpointListener();
		engine.addFlashpointListener(nullListener2);
		counts[0] = counts[1] = counts[2] = 0;
		nullListener2.postCount = nullListener1.postCount = 0;
		for (int i = 0; i < TRIALS; i++) {
			VariantStateRequest req = engine.newStateRequest(ssn, state, "");
			String expName = req.getTargetedExperience(test).getName();			
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
		} 
		assertEquals(TRIALS, nullListener1.postCount);
		assertEquals(TRIALS, nullListener2.postCount);
		verifyCounts(counts, new float[] {1, 2, 97});

		//
		// Add the A/B listener - changes distribution to 1/1/0.
		//
		ABTargetingFlashpointListener ABListener = new ABTargetingFlashpointListener();
		engine.addFlashpointListener(ABListener);
		counts[0] = counts[1] = counts[2] = 0;
		ABListener.postCount = nullListener2.postCount = nullListener1.postCount = 0;
		for (int i = 0; i < TRIALS; i++) {
			VariantStateRequest req = engine.newStateRequest(ssn, state, "");
			String expName = req.getTargetedExperience(test).getName();			
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
		} 
		assertEquals(TRIALS, nullListener1.postCount);
		assertEquals(TRIALS, nullListener2.postCount);
		assertEquals(TRIALS, ABListener.postCount);
		verifyCounts(counts, new float[] {1, 1, 0});

		//
		// Clear all listeners, then add the A/C listener - changes distribution to 1/0/1.
		//
		engine.clearFlashpointListeners();
		ACTargetingFlashpointListener ACListener = new ACTargetingFlashpointListener();
		engine.addFlashpointListener(ACListener);
		counts[0] = counts[1] = counts[2] = 0;		
		for (int i = 0; i < TRIALS; i++) {
			VariantStateRequest req = engine.newStateRequest(ssn, state, "");
			String expName = req.getTargetedExperience(test).getName();	
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
		} 
		assertEquals(TRIALS, ACListener.postCount);
		verifyCounts(counts, new float[] {1, 0, 1});

		//
		// Add null listener - should not change the distribution.
		//
		engine.addFlashpointListener(nullListener1);
		nullListener1.postCount = ACListener.postCount = 0;
		counts[0] = counts[1] = counts[2] = 0;		
		for (int i = 0; i < TRIALS; i++) {
			VariantStateRequest req = engine.newStateRequest(ssn, state, "");
			String expName = req.getTargetedExperience(test).getName();	
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
		} 
		assertEquals(TRIALS, nullListener1.postCount);
		assertEquals(TRIALS, ACListener.postCount);
		verifyCounts(counts, new float[] {1, 0, 1});

		//
		// Clear all listeners, then add the A/B/Null custom targeter + A/C targeter - changes distribution to 50/25/25.
		//
		engine.clearFlashpointListeners();
		ABNullTargetingFlashpointListener ABNullListener = new ABNullTargetingFlashpointListener();
		engine.addFlashpointListener(ABNullListener);
		engine.addFlashpointListener(ACListener);
		counts[0] = counts[1] = counts[2] = 0;
		ABNullListener.postCount = ACListener.postCount = 0;
		for (int i = 0; i < TRIALS; i++) {
			VariantStateRequest req = engine.newStateRequest(ssn, state, "");
			String expName = req.getTargetedExperience(test).getName();	
			if (expName.equals("A")) counts[0]++;
			else if (expName.equals("B")) counts[1]++;
			else if (expName.equals("C")) counts[2]++;
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
		
		engine.clearFlashpointListeners();
		ParserResponse response = engine.parseSchema(config);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		Schema schema = engine.getSchema();		
		State state = schema.getState("state1");
		VariantSession ssn = engine.getSession("foo");

		boolean exceptionThrown = false;
		try {
			engine.newStateRequest(ssn, state, "");
		}
		catch (VariantRuntimeException e) {
			exceptionThrown = true;
			assertEquals(e.getMessage(), new VariantRuntimeException(MessageTemplate.RUN_WEIGHT_MISSING, "test1", "B").getMessage());
		}
		assertTrue(exceptionThrown);
	}
	
	/**
	 * Always return null.
	 */
	private static class NullTargetingFlashpointListener implements FlashpointListener<TestTargetingFlashpoint> {
		
		private int postCount = 0;
		
		@Override
		public Class<TestTargetingFlashpoint> getFlashpointClass() {
			return TestTargetingFlashpoint.class;
		}
		
		@Override
		public void post(TestTargetingFlashpoint flashpoint) {
			postCount++;
			// do nothing.
		}
	}

	/**
	 * returns A or B in equal probabilities.
	 */
	private static class ABTargetingFlashpointListener implements FlashpointListener<TestTargetingFlashpoint> {

		private int postCount = 0;
		private static Random rand = new Random(System.currentTimeMillis());

		@Override
		public Class<TestTargetingFlashpoint> getFlashpointClass() {
			return TestTargetingFlashpoint.class;
		}
		
		@Override
		public void post(TestTargetingFlashpoint flashpoint) {
			postCount++;
			com.variant.core.schema.Test test = flashpoint.getTest();
			Experience experience = rand.nextBoolean() ? test.getExperience("A") : test.getExperience("B");
			flashpoint.setTargetedExperience(experience);
		}
	}

	/**
	 * returns A or C in equal probabilities.
	 */
	private static class ACTargetingFlashpointListener implements FlashpointListener<TestTargetingFlashpoint> {
		
		private int postCount = 0;
		private static Random rand = new Random(System.currentTimeMillis());
		
		@Override
		public Class<TestTargetingFlashpoint> getFlashpointClass() {
			return TestTargetingFlashpoint.class;
		}
		
		@Override
		public void post(TestTargetingFlashpoint flashpoint) {
			postCount++;
			com.variant.core.schema.Test test = flashpoint.getTest();
			if (test.getName().equals("test1") && flashpoint.getTargetedExperience() == null) {
				Experience experience = rand.nextBoolean() ? test.getExperience("A") : test.getExperience("C");
				flashpoint.setTargetedExperience(experience);
			}
		}
	}

	/**
	 * returns A 25% of the time, B 25% of the time and null 50% of the time..
	 */
	private static class ABNullTargetingFlashpointListener implements FlashpointListener<TestTargetingFlashpoint> {
		
		private int postCount = 0;
		private static Random rand = new Random(System.currentTimeMillis());
		
		@Override
		public Class<TestTargetingFlashpoint> getFlashpointClass() {
			return TestTargetingFlashpoint.class;
		}
		
		@Override
		public void post(TestTargetingFlashpoint flashpoint) {
			postCount++;
			com.variant.core.schema.Test test = flashpoint.getTest();
			if (test.getName().equals("test1") && flashpoint.getTargetedExperience() == null) {
				Experience experience = rand.nextBoolean() ? (rand.nextBoolean() ? test.getExperience("A") : test.getExperience("B")) : null;
				flashpoint.setTargetedExperience(experience);
			}
		}

	}
	
	
}
