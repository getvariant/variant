package com.variant.core.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.TestQualificationHook;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantCollectionsUtils;


public class SchemaParserOkayTest extends BaseTest {
	


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
			    	    "        'idleDaysToLive':0,                                              \n" +
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
			    	    "        'idleDaysToLive':3,                                              \n" +
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
		
		ParserResponse response = engine.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		VariantSession session = engine.getSession("foo");
		State state1 = engine.getSchema().getState("state1");
		engine.clearHookListeners();
		VariantStateRequest req = engine.newStateRequest(session, state1, "");
		assertTrue(req.getTargetedExperiences().isEmpty());
		assertTrue(req.getDisqualifiedTests().isEmpty());
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
			    	    "        'idleDaysToLive':0,                                              \n" +
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
			    	    "        'idleDaysToLive':3,                                              \n" +
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
		
		ParserResponse response = engine.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		VariantSession session = engine.getSession("foo");
		State state1 = engine.getSchema().getState("state1");
		com.variant.core.schema.Test test2 = engine.getSchema().getTest("test2");
		engine.clearHookListeners();
		engine.addHookListener(new TestQualificationHookListener(test2));
		VariantStateRequest req = engine.newStateRequest(session, state1, "");
		assertEquals(VariantCollectionsUtils.set(test2), req.getDisqualifiedTests());
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
			    	    "        'idleDaysToLive':0,                                              \n" +
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
			    	    "        'idleDaysToLive':3,                                              \n" +
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
		
		ParserResponse response = engine.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		VariantSession session = engine.getSession("foo");
		State state1 = engine.getSchema().getState("state1");
		com.variant.core.schema.Test test1 = engine.getSchema().getTest("test1");
		com.variant.core.schema.Test test2 = engine.getSchema().getTest("test2");
		engine.clearHookListeners();
		engine.addHookListener(new TestQualificationHookListener(test1));
		engine.addHookListener(new TestQualificationHookListener(test2));
		VariantStateRequest req = engine.newStateRequest(session, state1, "");
		assertEquals(VariantCollectionsUtils.set(test1, test2), req.getDisqualifiedTests());
		assertTrue(req.getTargetedExperiences().isEmpty());
		assertEquals("/path/to/state1", req.getResolvedParameterMap().get("path"));
	}
	
	/**
	 * 
	 */
	private static class TestQualificationHookListener implements HookListener<TestQualificationHook> {

		private ArrayList<com.variant.core.schema.Test> testList = new ArrayList<com.variant.core.schema.Test>();
		private com.variant.core.schema.Test testToDisqualify;
		
		private TestQualificationHookListener(com.variant.core.schema.Test testToDisqualify) {
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

