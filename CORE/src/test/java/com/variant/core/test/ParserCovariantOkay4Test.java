package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.variant.core.impl.VariantCore;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantCollectionsUtils;


/**
 * All Tests:
 * Tests States
 *       1 2 3 4 5
 * test1   + + - -
 * test2 - + + +  
 * test3 + + - - -
 * blank: not instrumented, -: nonvariant, +: has variants.
 * 
 * T1, T2(T1), T3(T1)
 * 
 * @author Igor
 */
public class ParserCovariantOkay4Test extends BaseTestCore {
	
	private VariantCore core = rebootApi();

	/**
	 * 
	 */
	@org.junit.Test
	public void test() throws Exception {
		
		final String SCHEMA = 

		"{                                                                                \n" +
	    	    //==========================================================================//
	    	   
	    	    "   'states':[                                                             \n" +
	    	    "     {  'name':'state1',                                                  \n" +
	    	    "        'parameters': {                                                   \n" +
	    	    "           'path':'/path/to/state1'                                       \n" +
	    	    "        }                                                                 \n" +
	    	    "     },                                                                   \n" +
	    	    "     {  'NAME':'state2',                                                  \n" +
	    	    "        'parameters': {                                                   \n" +
	    	    "           'path':'/path/to/state2'                                       \n" +
	    	    "        }                                                                 \n" +
	    	    "     },                                                                   \n" +
	    	    "     {  'nAmE':'state3',                                                  \n" +
	    	    "        'parameters': {                                                   \n" +
	    	    "           'path':'/path/to/state3'                                       \n" +
	    	    "        }                                                                 \n" +
	    	    "     },                                                                   \n" +
	    	    "     {  'name':'state4',                                                  \n" +
	    	    "        'parameters': {                                                   \n" +
	    	    "           'path':'/path/to/state4'                                       \n" +
	    	    "        }                                                                 \n" +
	    	    "     },                                                                   \n" +
	    	    "     {  'name':'state5',                                                  \n" +
	    	    "        'parameters': {                                                   \n" +
	    	    "           'path':'/path/to/state5'                                       \n" +
	    	    "        }                                                                 \n" +
	    	    "     }                                                                    \n" +
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
	    	    "              'weight':30                                                \n" +
	    	    "           }                                                             \n" +
	    	    "        ],                                                               \n" +
	    	    "        'onStates':[                                                      \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state2',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.B'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.C'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state3',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state3/test1.B'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state3/test1.C'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state4',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state5',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    //--------------------------------------------------------------------------//	
	    	    "     {                                                                   \n" +
	    	    "        'name':'test2',                                                  \n" +
	    	    "        'covariantTestRefs': ['test1'],                                  \n" +
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
	    	    "              'weight':30                                                \n" +
	    	    "           }                                                             \n" +
	    	    "        ],                                                               \n" +
	    	    "        'onStates':[                                                      \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state1',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state2',                                         \n" +
	    	    "              'variants':[                                               \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test2.B'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.B+test2.B'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.C+test2.B'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test2.C'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.B+test2.C'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.C+test2.C'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state3',                                         \n" +
	    	    "              'variants':[                                               \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state3/test2.B'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state3/test1.B+test2.B'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state3/test1.C+test2.B'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state3/test2.C'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state3/test1.B+test2.C'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state3/test1.C+test2.C'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +

	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state4',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state4/test2.B'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state4/test2.C'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           }                                                            \n" +
	    	    "        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    //--------------------------------------------------------------------------//	
	    	    "     {                                                                   \n" +
	    	    "        'name':'test3',                                                  \n" +
	    	    "        'covariantTestRefs': ['test1'],                                  \n" +
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
	    	    "              'weight':30                                                \n" +
	    	    "           }                                                             \n" +
	    	    "        ],                                                               \n" +
	    	    "        'onStates':[                                                      \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state1',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state1/test3.B'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state1/test3.C'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state2',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test3.B'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.B+test3.B'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.C+test3.B'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test3.C'                      \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.B+test3.C'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'parameters': {                                      \n" +
	    	    "                       'path':'/path/to/state2/test1.C+test3.C'              \n" +
	    	    "                    }                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state3',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state4',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state5',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     }                                                                   \n" +
	    	    //--------------------------------------------------------------------------//	
	     	    "  ]                                                                      \n" +
	    	    "}                                                                         ";

		ParserResponse response = core.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = core.getSchema();
		final Test test1 = schema.getTest("test1");
		final Test test2 = schema.getTest("test2");
		final Test test3 = schema.getTest("test3");

		// 
		// Declared test covariance.
		//
		assertEquals(0, test1.getCovariantTests().size());
		assertEquals(VariantCollectionsUtils.list(test1), test2.getCovariantTests());
		assertEquals(VariantCollectionsUtils.list(test1), test3.getCovariantTests());

		// 
		// test1 OnView objects
		//
		List<Test.OnState> onStates = test1.getOnStates();
		assertEquals(4, onStates.size());
		
		// state2
		Test.OnState onView = onStates.get(0);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getState("state2"), onView.getState());
		List<Test.OnState.Variant> variants = onView.getVariants();
		assertEquals(2, variants.size());

		Test.OnState.Variant variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/state2/test1.B", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/state2/test1.C", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		// state3
		onView = onStates.get(1);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getState("state3"), onView.getState());
		variants = onView.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/state3/test1.B", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/state3/test1.C", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		// state4
		onView = onStates.get(2);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getState("state4"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// state5
		onView = onStates.get(3);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getState("state5"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// 
		// test2 OnView objects
		//
		onStates = test2.getOnStates();
		assertEquals(4, onStates.size());
		
		// state1
		onView = onStates.get(0);
		assertTrue(onView.isNonvariant());
		assertTrue(onView.getVariants().isEmpty());
		assertEquals(schema.getState("state1"), onView.getState());

		// state2
		onView = onStates.get(1);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getState("state2"), onView.getState());
		variants = onView.getVariants();
		assertEquals(6, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state2/test2.B", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test2.B", variant.getParameterMap().get("path"));

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test2.B", variant.getParameterMap().get("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state2/test2.C", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test2.C", variant.getParameterMap().get("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test2.C", variant.getParameterMap().get("path"));

		// state3
		onView = onStates.get(2);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getState("state3"), onView.getState());
		variants = onView.getVariants();
		assertEquals(6, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state3/test2.B", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state3/test1.B+test2.B", variant.getParameterMap().get("path"));

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state3/test1.C+test2.B", variant.getParameterMap().get("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state3/test2.C", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state3/test1.B+test2.C", variant.getParameterMap().get("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state3/test1.C+test2.C", variant.getParameterMap().get("path"));

		// state4
		onView = onStates.get(3);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getState("state4"), onView.getState());
		variants = onView.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state4/test2.B", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state4/test2.C", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		// 
		// test3 OnView objects
		//
		onStates = test3.getOnStates();
		assertEquals(5, onStates.size());
		
		// state1
		onView = onStates.get(0);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getState("state1"), onView.getState());
		variants = onView.getVariants();
		assertEquals(2, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/state1/test3.B", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/state1/test3.C", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		// state2
		onView = onStates.get(1);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getState("state2"), onView.getState());
		variants = onView.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/state2/test3.B", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test3.B", variant.getParameterMap().get("path"));

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test3.B", variant.getParameterMap().get("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/state2/test3.C", variant.getParameterMap().get("path"));
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test3.C", variant.getParameterMap().get("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test3.C", variant.getParameterMap().get("path"));

		// state3
		onView = onStates.get(2);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getState("state3"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// state4
		onView = onStates.get(3);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getState("state4"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// state5
		onView = onStates.get(4);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getState("state5"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

	}

	
}

