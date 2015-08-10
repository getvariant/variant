package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.TestImpl;

public class TargetingTest extends BaseTest {

	static final int TRIALS = 5000000;
	static final float DELTA_AS_FRACTION = .01f;
	/**
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeTestCase() throws Exception {

		// Bootstrap the Variant container with defaults.
		Variant.Config variantConfig = new Variant.Config();
		variantConfig.getSessionServiceConfig().setKeyResolverClassName("com.variant.core.util.SessionKeyResolverJunit");
		Variant.bootstrap(variantConfig);

	}

	/**
	 * Basic targeting
	 * @throws Exception
	 */
	@Test
	public void basicTest() throws Exception {

		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view1/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = Variant.parseSchema(config);

		assertFalse(response.hasErrors());

		Schema schema = Variant.getSchema();
		TestImpl test1 = (TestImpl) schema.getTest("test1");

		//
		// No custom targeter - distribution according to weights.
		//
		int[] counts = {0, 0, 0};
		for (int i = 0; i < TRIALS; i++) {
			Experience e = test1.target(null);
			if (e.getName().equals("A")) counts[0]++;
			else if (e.getName().equals("B")) counts[1]++;
			else if (e.getName().equals("C")) counts[2]++;
		} 
		verifyCounts(counts, new float[] {1, 2, 97});

		//
		// Add one null custom targeter - still distribution according to weights.
		//
		test1.registerCustomTargeter(new NullTargeter());
		assertEquals(1, test1.getCustomTargeters().size());
		counts[0] = counts[1] = counts[2] = 0;
		for (int i = 0; i < TRIALS; i++) {
			Experience e = test1.target(null);
			if (e.getName().equals("A")) counts[0]++;
			else if (e.getName().equals("B")) counts[1]++;
			else if (e.getName().equals("C")) counts[2]++;
		} 
		verifyCounts(counts, new float[] {1, 2, 97});

		//
		// Add two null custom targeter - still distribution according to weights.
		//
		test1.registerCustomTargeter(new NullTargeter());
		assertEquals(2, test1.getCustomTargeters().size());
		counts[0] = counts[1] = counts[2] = 0;
		for (int i = 0; i < TRIALS; i++) {
			Experience e = test1.target(null);
			if (e.getName().equals("A")) counts[0]++;
			else if (e.getName().equals("B")) counts[1]++;
			else if (e.getName().equals("C")) counts[2]++;
		} 
		verifyCounts(counts, new float[] {1, 2, 97});

		//
		// Add the A/B custom targeter - changes distribution to 1/1/0.
		//
		test1.registerCustomTargeter(new ABTargeter());
		assertEquals(3, test1.getCustomTargeters().size());
		counts[0] = counts[1] = counts[2] = 0;
		for (int i = 0; i < TRIALS; i++) {
			Experience e = test1.target(null);
			if (e.getName().equals("A")) counts[0]++;
			else if (e.getName().equals("B")) counts[1]++;
			else if (e.getName().equals("C")) counts[2]++;
		} 
		verifyCounts(counts, new float[] {1, 1, 0});


		//
		// Clear all targeters, then add the A/C custom targeter - changes distribution to 1/0/1.
		//
		test1.clearCustomTargeters();
		test1.registerCustomTargeter(new ACTargeter());
		assertEquals(1, test1.getCustomTargeters().size());
		counts[0] = counts[1] = counts[2] = 0;
		for (int i = 0; i < TRIALS; i++) {
			Experience e = test1.target(null);
			if (e.getName().equals("A")) counts[0]++;
			else if (e.getName().equals("B")) counts[1]++;
			else if (e.getName().equals("C")) counts[2]++;
		} 
		verifyCounts(counts, new float[] {1, 0, 1});

		//
		// Clear all targeters, then add the A/B/Null custom targeter + A/C targeter - changes distribution to 50/25/25.
		//
		test1.clearCustomTargeters();
		test1.registerCustomTargeter(new ABNullTargeter());
		test1.registerCustomTargeter(new ACTargeter());
		assertEquals(2, test1.getCustomTargeters().size());
		counts[0] = counts[1] = counts[2] = 0;
		for (int i = 0; i < TRIALS; i++) {
			Experience e = test1.target(null);
			if (e.getName().equals("A")) counts[0]++;
			else if (e.getName().equals("B")) counts[1]++;
			else if (e.getName().equals("C")) counts[2]++;
		} 
		System.out.println(counts[0] + " " + counts[1] + " " +counts[2]);
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
			assertEquals(counts[i]/sumCounts, weights[i]/sumWeights, weights[i]/sumWeights * DELTA_AS_FRACTION);
		}
	}

	/**
	 * Always return null.
	 */
	private static class NullTargeter implements TestImpl.Targeter {
		
		@Override
		public Experience target(com.variant.core.schema.Test test, VariantSession session) {
			return null;
		}
	}

	/**
	 * returns A or B in equal probabilities.
	 */
	private static class ABTargeter implements TestImpl.Targeter {
		private static Random rand = new Random(System.currentTimeMillis());
		@Override
		public Experience target(com.variant.core.schema.Test test, VariantSession session) {
			return rand.nextBoolean() ? test.getExperience("A") : test.getExperience("B");
		}
	}

	/**
	 * returns A or C in equal probabilities.
	 */
	private static class ACTargeter implements TestImpl.Targeter {
		private static Random rand = new Random(System.currentTimeMillis());
		@Override
		public Experience target(com.variant.core.schema.Test test, VariantSession session) {
			return rand.nextBoolean() ? test.getExperience("A") : test.getExperience("C");
		}
	}

	/**
	 * returns A 25% of the time, B 25% of the time and null 50% of the time..
	 */
	private static class ABNullTargeter implements TestImpl.Targeter {
		private static Random rand = new Random(System.currentTimeMillis());
		@Override
		public Experience target(com.variant.core.schema.Test test, VariantSession session) {
			return rand.nextBoolean() ? (rand.nextBoolean() ? test.getExperience("A") : test.getExperience("B")) : null;
		}
	}
	
	
}
