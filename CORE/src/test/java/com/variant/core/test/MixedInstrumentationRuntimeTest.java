package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.exception.Error;
import com.variant.core.impl.VariantCore;
import com.variant.core.xdm.Schema;
import com.variant.core.xdm.State;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;
import com.variant.server.ParserResponse;
import com.variant.server.hook.HookListener;
import com.variant.server.hook.TestTargetingHook;

/**
 * Test mixed instrumentation, i.e. when an experience is not defined
 * on a particular state, as denoted by the tests/onState/isDefined element.
 *
 * @author Igor 
 *
 */
public class MixedInstrumentationRuntimeTest extends BaseTestCore {
	
	private Random rand = new Random();
	private VariantCore core = rebootApi();
	private static final int ITERATIONS = 1000;
	
	private static class CounterMap<T> {
		private Map<T, Integer> map = new ConcurrentHashMap<T, Integer>();
		public void incr(T key) {
			map.put(key, get(key) + 1);
		}
		public Integer get(T key) {
			Integer result = map.get(key);
			if (result == null) {
				result = new Integer(0);
				map.put(key, result);
			}
			return result;
		}
		public void clear() {
			map.clear();
		}
	}
	
	/**
	 * Run time.
	 */
	@org.junit.Test
	public void test1() throws Exception {
		
		/*
		 *      S1 S2 S3
		 * T1.A  +  -  =
		 * T1.B  -  +  =
		 * T1.C  +  -  =
		 * T1.D  -  +  =
         *
		 * T2.A  -  +  -
		 * T2.B  +  +  +
		 * T2.C  -  +  +
		 * T2.D  +  +  -
         *
		 */
		final String SCHEMA = 
				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'},                                                   \n" +
			    	    "     {'name':'state3'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    //=========================================================================//			    	    
				        "  'tests':[                                                              \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test1',                                                  \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'A',                                                \n" +
			    	    "              'weight':10,                                               \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'B',                                                \n" +
			    	    "              'weight':20                                                \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'C',                                                \n" +
			    	    "              'weight':20                                                \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'D',                                                \n" +
			    	    "              'weight':20                                                \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                     \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.C'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                       \n" +
			    	    "              'isNonvariant':true                                        \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'covariantTestRefs':['test1'],                                   \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'A',                                                \n" +
			    	    "              'weight':10,                                               \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'B',                                                \n" +
			    	    "              'weight':20                                                \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'C',                                                \n" +
			    	    "              'weight':20                                                \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'D',                                                \n" +
			    	    "              'weight':20                                                \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                     \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test2.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state1/test1.C+test2.B'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state1/test1.C+test2.C'           \n" +
					    "                    }                                                    \n" +
					    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test2.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.B+test2.B'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'D'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.D+test2.B'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test2.C'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'C',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.B+test2.C'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'C',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'D'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.C+test2.D'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test2.C'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.B+test2.D'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'D'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.D+test2.D'           \n" +
					    "                    }                                                    \n" +
					    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state3/test2.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state3/test2.C'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		final Schema schema = core.getSchema();
		final State s1 = schema.getState("state1");
		final State s2 = schema.getState("state2");		
		final State s3 = schema.getState("state3");		
		final Test t1 = schema.getTest("test1");
		final Test t2 = schema.getTest("test2");

		// Target for S1 with nothing in tracker.
		CounterMap<Test.Experience> counters = new CounterMap<Test.Experience>();		
		for (int i = 0; i < ITERATIONS; i++) {
			String sessionId = VariantStringUtils.random64BitString(rand);
			VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
			VariantCoreStateRequest req = ssn.targetForState(s1);
			assertEquals(2, req.getLiveExperiences().size());
			assertMatches("A|C", req.getLiveExperience(t1).getName());
			assertMatches("B|D", req.getLiveExperience(t2).getName());
			counters.incr(req.getLiveExperience(t1));
			counters.incr(req.getLiveExperience(t2));
		}
		assertTrue(counters.get(experience("test1.A", schema)) > 0);
		assertTrue(counters.get(experience("test1.C", schema)) > 0);
		assertTrue(counters.get(experience("test2.B", schema)) > 0);
		assertTrue(counters.get(experience("test2.D", schema)) > 0);

		// Target for S2 with nothing in tracker.
		counters.clear();
		for (int i = 0; i < ITERATIONS; i++) {		
			String sessionId = VariantStringUtils.random64BitString(rand);
			VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
			VariantCoreStateRequest req = ssn.targetForState(s2);
			assertMatches("B|D", req.getLiveExperience(t1).getName());
			assertMatches("A|B|C|D", req.getLiveExperience(t2).getName());
			counters.incr(req.getLiveExperience(t1));
			counters.incr(req.getLiveExperience(t2));
		}
		assertTrue(counters.get(experience("test1.B", schema)) > 0);
		assertTrue(counters.get(experience("test1.D", schema)) > 0);
		assertTrue(counters.get(experience("test2.A", schema)) > 0);
		assertTrue(counters.get(experience("test2.B", schema)) > 0);
		assertTrue(counters.get(experience("test2.C", schema)) > 0);
		assertTrue(counters.get(experience("test2.D", schema)) > 0);
		
		// Target for S1 with test1.A already in the tracker,
		// which should be honored.
		counters.clear();
		for (int i = 0; i < ITERATIONS; i++) {		
			String sessionId = VariantStringUtils.random64BitString(rand);
			VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
			setTargetingStabile(ssn, "test1.A");
			VariantCoreStateRequest req = ssn.targetForState(s1);
			assertEquals(2, req.getLiveExperiences().size());
			assertMatches("A", req.getLiveExperience(t1).getName());
			assertMatches("B|D", req.getLiveExperience(t2).getName());
			counters.incr(req.getLiveExperience(t1));
			counters.incr(req.getLiveExperience(t2));
		}
		assertTrue(counters.get(experience("test1.A", schema)) > 0);
		assertTrue(counters.get(experience("test2.B", schema)) > 0);
		assertTrue(counters.get(experience("test2.D", schema)) > 0);
		
		
		// Target for S1 with test1.B already in the tracker,
		// which is undefined on S1, so should be discarded.
		counters.clear();
		for (int i = 0; i < ITERATIONS; i++) {		
			String sessionId = VariantStringUtils.random64BitString(rand);
			VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
			setTargetingStabile(ssn, "test1.B");
			VariantCoreStateRequest req = ssn.targetForState(s1);
			assertEquals(2, req.getLiveExperiences().size());
			assertMatches("A|C", req.getLiveExperience(t1).getName());
			assertMatches("B|D", req.getLiveExperience(t2).getName());
			counters.incr(req.getLiveExperience(t1));
			counters.incr(req.getLiveExperience(t2));
		}
		assertTrue(counters.get(experience("test1.A", schema)) > 0);
		assertTrue(counters.get(experience("test1.C", schema)) > 0);
		assertTrue(counters.get(experience("test2.B", schema)) > 0);
		assertTrue(counters.get(experience("test2.D", schema)) > 0);
		
		
		// Target for S1 with test1.A (should be honored) and test2.A
		// (should be discarded) already in the tracker.
		counters.clear();
		for (int i = 0; i < ITERATIONS; i++) {		
			String sessionId = VariantStringUtils.random64BitString(rand);
			VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
			setTargetingStabile(ssn, "test1.A", "test2.A");
			VariantCoreStateRequest req = ssn.targetForState(s1);
			assertEquals(2, req.getLiveExperiences().size());
			assertMatches("A", req.getLiveExperience(t1).getName());
			assertMatches("B|D", req.getLiveExperience(t2).getName());
			counters.incr(req.getLiveExperience(t1));
			counters.incr(req.getLiveExperience(t2));
		}
		assertTrue(counters.get(experience("test1.A", schema)) > 0);
		assertTrue(counters.get(experience("test2.B", schema)) > 0);
		assertTrue(counters.get(experience("test2.D", schema)) > 0);

		// Target for S1 with test1.B (should be discarded) and test2.B
		// (should be honored) already in the tracker.
		counters.clear();
		for (int i = 0; i < ITERATIONS; i++) {		
			String sessionId = VariantStringUtils.random64BitString(rand);
			VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
			setTargetingStabile(ssn, "test1.B", "test2.B");
			VariantCoreStateRequest req = ssn.targetForState(s1);
			assertEquals(2, req.getLiveExperiences().size());
			assertMatches("A|C", req.getLiveExperience(t1).getName());
			assertMatches("B", req.getLiveExperience(t2).getName());
			counters.incr(req.getLiveExperience(t1));
			counters.incr(req.getLiveExperience(t2));
		}
		assertTrue(counters.get(experience("test1.A", schema)) > 0);
		assertTrue(counters.get(experience("test1.C", schema)) > 0);
		assertTrue(counters.get(experience("test2.B", schema)) > 0);

		// Target for S1 with test1.A (should be honored) and test2.B
		// (should be honored) already in the tracker.
		counters.clear();
		for (int i = 0; i < ITERATIONS; i++) {		
			String sessionId = VariantStringUtils.random64BitString(rand);
			VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
			setTargetingStabile(ssn, "test1.A", "test2.B");
			VariantCoreStateRequest req = ssn.targetForState(s1);
			assertEquals(2, req.getLiveExperiences().size());
			assertMatches("A", req.getLiveExperience(t1).getName());
			assertMatches("B", req.getLiveExperience(t2).getName());
		}

		// Target for S1 and then for S1 again, which is OK always.
		String sessionId = VariantStringUtils.random64BitString(rand);
		VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
		VariantCoreStateRequest req = ssn.targetForState(s1);
		req.commit();
		req = ssn.targetForState(s1);
		assertEquals(2, req.getLiveExperiences().size());
		assertMatches("A|C", req.getLiveExperience(t1).getName());
		assertMatches("B|D", req.getLiveExperience(t2).getName());

		// Target for S1 and then for S3
		sessionId = VariantStringUtils.random64BitString(rand);
		ssn = core.getSession(sessionId, true).getBody();
		setTargetingStabile(ssn, "test1.A", "test2.B");
		req = ssn.targetForState(s1);
		req.commit();
		req = ssn.targetForState(s3);
		assertEquals(2, req.getLiveExperiences().size());
		assertMatches("A", req.getLiveExperience(t1).getName());
		assertMatches("B", req.getLiveExperience(t2).getName());

		// Target for S3 and then for S2
		sessionId = VariantStringUtils.random64BitString(rand);
		ssn = core.getSession(sessionId, true).getBody();
		setTargetingStabile(ssn, "test1.B", "test2.C");
		req = ssn.targetForState(s3);
		req.commit();
		req = ssn.targetForState(s2);
		assertEquals(2, req.getLiveExperiences().size());
		assertMatches("B", req.getLiveExperience(t1).getName());
		assertMatches("C", req.getLiveExperience(t2).getName());

		// target for S1 and then for S2, which is not allowed because
		// it'll break T1.
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				String sessionId = VariantStringUtils.random64BitString(rand);
				VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
				// Set experience in TT to make the exception message deterministic.
				setTargetingStabile(ssn, "test1.A");
				VariantCoreStateRequest req = ssn.targetForState(s1);
				req.commit();
				req = ssn.targetForState(s2);
			}
		}.assertThrown(Error.RUN_STATE_UNDEFINED_IN_EXPERIENCE, "test1.A", "state2");
		
		// target for S3 and then for S2, which is not allowed because
		// it'll break T1.
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() { 
				String sessionId = VariantStringUtils.random64BitString(rand);
				VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
				// Set experience in TT to make the exception message deterministic.
				setTargetingStabile(ssn, "test1.C");
				VariantCoreStateRequest req = ssn.targetForState(s3);
				req.commit();
				req = ssn.targetForState(s2);
			}
		}.assertThrown(Error.RUN_STATE_UNDEFINED_IN_EXPERIENCE, "test1.C", "state2");

		// Targeting hook listener returns a good experience.
		core.addHookListener(new TargetingHookListener(t1, t1.getExperience("B")));
		sessionId = VariantStringUtils.random64BitString(rand);
		ssn = core.getSession(sessionId, true).getBody();
		req = ssn.targetForState(s2);
		assertEquals(2, req.getLiveExperiences().size());
		assertMatches("B", req.getLiveExperience(t1).getName());
		assertMatches("A|B|C|D", req.getLiveExperience(t2).getName());

		// Targeting hook listener returns experience from the wrong test.
		final TargetingHookListener badListener1 = new TargetingHookListener(t1, t2.getExperience("C"));
		core.addHookListener(badListener1);
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() {		
				String sessionId = VariantStringUtils.random64BitString(rand);
				VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
				ssn.targetForState(s2);
			}
		}.assertThrown(Error.RUN_HOOK_TARGETING_BAD_EXPERIENCE, badListener1.getClass().getName(), t1.getName(), t2.getExperience("C").toString());

		// Targeting hook listener returns experience uninstrumented on the state.
		core.clearHookListeners();
		final TargetingHookListener badListener2 = new TargetingHookListener(t1, t1.getExperience("C"));
		core.addHookListener(badListener2);
		new VariantRuntimeExceptionInterceptor() { 
			@Override public void toRun() {		
				String sessionId = VariantStringUtils.random64BitString(rand);
				VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
				ssn.targetForState(s2);
			}
		}.assertThrown(Error.RUN_HOOK_TARGETING_BAD_EXPERIENCE, badListener2.getClass().getName(), t1.getName(), t1.getExperience("C").toString());

	}
	
	/**
	 * 
	 */
	private static class TargetingHookListener implements HookListener<TestTargetingHook> {

		private Test forTest;
		private Experience targetExp;
		
		private TargetingHookListener(Test forTest, Experience targetExp) {
			this.forTest = forTest;
			this.targetExp = targetExp;
		}
		
		@Override
		public Class<TestTargetingHook> getHookClass() {
			return TestTargetingHook.class;
		}

		@Override
		public void post(TestTargetingHook hook) {
			if (hook.getTest().equals(forTest)) hook.setTargetedExperience(targetExp);
		}
		
	}
 }

