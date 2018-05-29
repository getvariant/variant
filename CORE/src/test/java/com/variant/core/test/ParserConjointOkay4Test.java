package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.variant.core.impl.UserError.Severity;
import com.variant.core.schema.Schema;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.util.CollectionsUtils;

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
public class ParserConjointOkay4Test extends BaseTestCore {
	
	/**
	 * 
	 */
	@org.junit.Test
	public void test() throws Exception {
		
		final String SCHEMA = 

		"{                                                                                \n" +
			    "  'meta':{                                                               \n" +		    	    
			    "      'name':'schema_name',                                              \n" +
			    "      'comment':'schema comment'                                         \n" +
			    "  },                                                                     \n" +
	    	    //==========================================================================//
	    	   
	    	    "   'states':[                                                            \n" +
	    	    "     {  'name':'state1',                                                 \n" +
				"        'parameters': [                                                  \n" +
				"          {                                                              \n" +
				"            'name':'path',                                               \n" +
				"            'value':'/path/to/state1'                                    \n" +
				"          },                                                             \n" +
				"          {                                                              \n" +
				"            'name':'foo',                                                \n" +
				"            'value':'bar'                                                \n" +
				"          }                                                              \n" +
				"        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'NAME':'state2',                                                 \n" +
				"        'parameters': [                                                  \n" +
				"          {                                                              \n" +
				"            'nAmE':'path',                                               \n" +
				"            'value':'/path/to/state2'                                    \n" +
				"          },                                                             \n" +
				"          {                                                              \n" +
				"            'name':'foo',                                                \n" +
				"            'value':'bar'                                                \n" +
				"          }                                                              \n" +
				"        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'nAmE':'state3',                                                 \n" +
				"        'parameters': [                                                  \n" +
				"          {                                                              \n" +
				"            'name':'foo',                                                \n" +
				"            'value':'bar'                                                \n" +
				"          },                                                             \n" +
				"          {                                                              \n" +
				"            'NAME':'path',                                               \n" +
				"            'value':'/path/to/state3'                                    \n" +
				"          }                                                              \n" +
				"        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'name':'state4',                                                 \n" +
				"        'parameters': [                                                  \n" +
				"          {                                                              \n" +
				"            'name':'foo',                                                \n" +
				"            'value':'bar'                                                \n" +
				"          },                                                             \n" +
				"          {                                                              \n" +
				"            'Name':'path',                                               \n" +
				"            'value':'/path/to/state4'                                    \n" +
				"          }                                                              \n" +
				"        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    "     {                                                                  \n" +
				"        'parameters': [                                                  \n" +
				"          {                                                              \n" +
				"            'name':'foo',                                               \n" +
				"            'value':'bar'                                                \n" +
				"          },                                                             \n" +
				"          {                                                              \n" +
				"            'NAME':'path',                                               \n" +
				"            'value':'/path/to/state5'                                    \n" +
				"          }                                                              \n" +
				"        ],                                                               \n" +
	    	    "        'name':'state5'                                                  \n" +
	    	    "     }                                                                   \n" +
	            "  ],                                                                     \n" +
	            
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
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state3',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state3/test1.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state3/test1.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
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
	    	    "        'conjointTestRefs': ['test1'],                                  \n" +
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
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test2.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.B+test2.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.C+test2.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test2.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.B+test2.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.C+test2.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state3',                                         \n" +
	    	    "              'variants':[                                               \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state3/test2.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state3/test1.B+test2.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state3/test1.C+test2.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state3/test2.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state3/test1.B+test2.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state3/test1.C+test2.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +

	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state4',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state4/test2.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state4/test2.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           }                                                            \n" +
	    	    "        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    //--------------------------------------------------------------------------//	
	    	    "     {                                                                   \n" +
	    	    "        'name':'test3',                                                  \n" +
	    	    "        'conjointTestRefs': ['test1'],                                  \n" +
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
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state1/test3.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state1/test3.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state2',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test3.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.B+test3.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.C+test3.B'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test3.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.B+test3.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'conjointExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
				"                    'parameters': [                                      \n" +
				"                       {                                                 \n" +
				"                          'name':'path',                                 \n" +
				"                          'value':'/path/to/state2/test1.C+test3.C'              \n" +
				"                       }                                                 \n" +
				"                    ]                                                    \n" +
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

		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNotNull(response.getSchema());
		assertNotNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertFalse(response.hasMessages(Severity.WARN));
		assertFalse(response.hasMessages(Severity.INFO));
		
		Schema schema = response.getSchema();

		final Test test1 = schema.getTest("test1");
		final Test test2 = schema.getTest("test2");
		final Test test3 = schema.getTest("test3");

		// 
		// Test concurrence.
		//
		assertFalse(test1.isSerialWith(test2));
		assertFalse(test1.isSerialWith(test3));
		assertFalse(test2.isSerialWith(test1));
		assertFalse(test2.isSerialWith(test3));
		assertFalse(test3.isSerialWith(test1));
		assertFalse(test3.isSerialWith(test2));

		assertTrue(test1.isConcurrentWith(test2));
		assertTrue(test1.isConcurrentWith(test3));
		assertTrue(test2.isConcurrentWith(test1));
		assertTrue(test2.isConcurrentWith(test3));
		assertTrue(test3.isConcurrentWith(test1));
		assertTrue(test3.isConcurrentWith(test2));

		assertTrue(test1.isConjointWith(test2));
		assertTrue(test1.isConjointWith(test3));
		assertTrue(test2.isConjointWith(test1));
		assertFalse(test2.isConjointWith(test3));
		assertTrue(test3.isConjointWith(test1));
		assertFalse(test3.isConjointWith(test2));
		
		assertNull(test1.getConjointTests());
		assertEquals(CollectionsUtils.list(test1), test2.getConjointTests());
		assertEquals(CollectionsUtils.list(test1), test3.getConjointTests());

		// 
		// test1 onState objects
		//
		List<Test.OnState> onStates = test1.getOnStates();
		assertEquals(4, onStates.size());
		
		// state2
		Test.OnState onState = onStates.get(0);
		assertFalse(onState.isNonvariant());
		assertEquals(schema.getState("state2"), onState.getState());
		List<StateVariant> variants = onState.getVariants();
		assertEquals(2, variants.size());

		StateVariant variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/state2/test1.B", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/state2/test1.C", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		// state3
		onState = onStates.get(1);
		assertFalse(onState.isNonvariant());
		assertEquals(schema.getState("state3"), onState.getState());
		variants = onState.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/state3/test1.B", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/state3/test1.C", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		// state4
		onState = onStates.get(2);
		assertTrue(onState.isNonvariant());
		assertEquals(schema.getState("state4"), onState.getState());
		variants = onState.getVariants();
		assertEquals(0, variants.size());

		// state5
		onState = onStates.get(3);
		assertTrue(onState.isNonvariant());
		assertEquals(schema.getState("state5"), onState.getState());
		variants = onState.getVariants();
		assertEquals(0, variants.size());

		// 
		// test2 onState objects
		//
		onStates = test2.getOnStates();
		assertEquals(4, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertTrue(onState.isNonvariant());
		assertTrue(onState.getVariants().isEmpty());
		assertEquals(schema.getState("state1"), onState.getState());

		// state2
		onState = onStates.get(1);
		assertFalse(onState.isNonvariant());
		assertEquals(schema.getState("state2"), onState.getState());
		variants = onState.getVariants();
		assertEquals(6, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state2/test2.B", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test2.B", variant.getParameters().get("path"));

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test2.B", variant.getParameters().get("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state2/test2.C", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test2.C", variant.getParameters().get("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test2.C", variant.getParameters().get("path"));

		// state3
		onState = onStates.get(2);
		assertFalse(onState.isNonvariant());
		assertEquals(schema.getState("state3"), onState.getState());
		variants = onState.getVariants();
		assertEquals(6, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state3/test2.B", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state3/test1.B+test2.B", variant.getParameters().get("path"));

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state3/test1.C+test2.B", variant.getParameters().get("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state3/test2.C", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state3/test1.B+test2.C", variant.getParameters().get("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state3/test1.C+test2.C", variant.getParameters().get("path"));

		// state4
		onState = onStates.get(3);
		assertFalse(onState.isNonvariant());
		assertEquals(schema.getState("state4"), onState.getState());
		variants = onState.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state4/test2.B", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state4/test2.C", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		// 
		// test3 onState objects
		//
		onStates = test3.getOnStates();
		assertEquals(5, onStates.size());
		
		// state1
		onState = onStates.get(0);
		assertFalse(onState.isNonvariant());
		assertEquals(schema.getState("state1"), onState.getState());
		variants = onState.getVariants();
		assertEquals(2, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/state1/test3.B", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/state1/test3.C", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		// state2
		onState = onStates.get(1);
		assertFalse(onState.isNonvariant());
		assertEquals(schema.getState("state2"), onState.getState());
		variants = onState.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/state2/test3.B", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test3.B", variant.getParameters().get("path"));

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test3.B", variant.getParameters().get("path"));

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/state2/test3.C", variant.getParameters().get("path"));
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test3.C", variant.getParameters().get("path"));

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getConjointExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getConjointExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test3.C", variant.getParameters().get("path"));

		// state3
		onState = onStates.get(2);
		assertTrue(onState.isNonvariant());
		assertEquals(schema.getState("state3"), onState.getState());
		variants = onState.getVariants();
		assertEquals(0, variants.size());

		// state4
		onState = onStates.get(3);
		assertTrue(onState.isNonvariant());
		assertEquals(schema.getState("state4"), onState.getState());
		variants = onState.getVariants();
		assertEquals(0, variants.size());

		// state5
		onState = onStates.get(4);
		assertTrue(onState.isNonvariant());
		assertEquals(schema.getState("state5"), onState.getState());
		variants = onState.getVariants();
		assertEquals(0, variants.size());

	}

	
}

