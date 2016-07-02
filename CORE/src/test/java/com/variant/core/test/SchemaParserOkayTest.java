package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.TestQualificationHook;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.util.VariantStringUtils;


public class SchemaParserOkayTest extends BaseTestCore {
	
	private static final Random rand = new Random();

	private VariantCore core = rebootApi();

	/**
	 * All tests are off.
	 */
	@Test
	public void allTestsOffTest() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {  'name':'state1',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state1'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state2',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state2'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state3',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state3'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     }                                                                    \n" +
			            "  ],                                                                      \n" +
			            
			    	    //=========================================================================//
			    	    
				        "  'tests':[                                                              \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test1',                                                  \n" +
			    	    "        'isOn':false,                                                    \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'A',                                                \n" +
			    	    "              'weight':10,                                               \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'B',                                                \n" +
			    	    "              'weight':20                                                \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'isOn': false,                                                   \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'C',                                                \n" +
			    	    "              'weight':0.5,                                              \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'D',                                                \n" +
			    	    "              'weight':0.6                                               \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                        \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state2/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                        \n" +
			    	    "              'isNonvariant':true                                        \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		VariantCoreSession session = core.getSession(VariantStringUtils.random64BitString(rand));
		State state1 = core.getSchema().getState("state1");
		core.clearHookListeners();
		VariantCoreStateRequest req = session.targetForState(state1);
		assertTrue(req.getTargetedExperiences().isEmpty());
		assertTrue(session.getTraversedStates().isEmpty());
		assertTrue(session.getTraversedTests().isEmpty());
		assertEquals("/path/to/state1", req.getResolvedParameterMap().get("path"));
	}

	/**
	 * One test is off, one disqualified.
	 */
	@Test
	public void oneOffOneDisqualifiedTest() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {  'name':'state1',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state1'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state2',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state2'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state3',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state3'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     }                                                                    \n" +
			            "  ],                                                                      \n" +
			            
			    	    //=========================================================================//
			    	    
				        "  'tests':[                                                              \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test1',                                                  \n" +
			    	    "        'isOn':false,                                                    \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'A',                                                \n" +
			    	    "              'weight':10,                                               \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'B',                                                \n" +
			    	    "              'weight':20                                                \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'isOn': true,                                                    \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'C',                                                \n" +
			    	    "              'weight':0.5,                                              \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'D',                                                \n" +
			    	    "              'weight':0.6                                               \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state2/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                       \n" +
			    	    "              'isNonvariant':true                                        \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		VariantCoreSession session = core.getSession(VariantStringUtils.random64BitString(rand));
		State state1 = core.getSchema().getState("state1");
		com.variant.core.schema.Test test2 = core.getSchema().getTest("test2");
		core.clearHookListeners();
		core.addHookListener(new TestDisqualifier(test2));
		VariantCoreStateRequest req = session.targetForState(state1);
		assertEquals(0, session.getTraversedStates().size());
		assertEquals(0, session.getTraversedTests().size());
		assertTrue(req.getTargetedExperiences().isEmpty());
		assertEquals("/path/to/state1", req.getResolvedParameterMap().get("path"));
	}

	/**
	 * All tests are disqualified.
	 */
	@Test
	public void allTestsDisqualifiedTest() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {  'name':'state1',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state1'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state2',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state2'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state3',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state3'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     }                                                                    \n" +
			            "  ],                                                                      \n" +
			            
			    	    //=========================================================================//
			    	    
				        "  'tests':[                                                              \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test1',                                                  \n" +
			    	    "        'isOn':true ,                                                    \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'A',                                                \n" +
			    	    "              'weight':10,                                               \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'B',                                                \n" +
			    	    "              'weight':20                                                \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'isOn': true,                                                    \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'C',                                                \n" +
			    	    "              'weight':0.5,                                              \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'D',                                                \n" +
			    	    "              'weight':0.6                                               \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                        \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state2/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                        \n" +
			    	    "              'isNonvariant':true                                        \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		VariantCoreSession session = core.getSession(VariantStringUtils.random64BitString(rand));
		State state1 = core.getSchema().getState("state1");
		com.variant.core.schema.Test test1 = core.getSchema().getTest("test1");
		com.variant.core.schema.Test test2 = core.getSchema().getTest("test2");
		core.clearHookListeners();
		core.addHookListener(new TestDisqualifier(test1));
		core.addHookListener(new TestDisqualifier(test2));
		VariantCoreStateRequest req = session.targetForState(state1);
		assertTrue(session.getTraversedStates().isEmpty());
		assertTrue(session.getTraversedTests().isEmpty());
		assertTrue(req.getTargetedExperiences().isEmpty());
		assertEquals("/path/to/state1", req.getResolvedParameterMap().get("path"));
	}
	
	/**
	 * 
	 */
	private static class TestDisqualifier implements HookListener<TestQualificationHook> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		private com.variant.core.schema.Test testToDisqualify;
		
		private TestDisqualifier(com.variant.core.schema.Test testToDisqualify) {
			this.testToDisqualify = testToDisqualify;
		}

		@Override
		public Class<TestQualificationHook> getHookClass() {
			return TestQualificationHook.class;
		}

		@Override
		public void post(TestQualificationHook hook) {
			if (hook.getTest().equals(testToDisqualify)) {
				testList.add(hook.getTest());
				hook.setQualified(false);
			}
		}		
	}

	
}

