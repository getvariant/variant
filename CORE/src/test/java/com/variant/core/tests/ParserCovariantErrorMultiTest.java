package com.variant.core.tests;

import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_DUPE;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_VARIANT_DUPE;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_VARIANT_MISSING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.schema.impl.ParserMessageImplFacade;
import com.variant.core.schema.impl.SchemaParser;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;

/**
 * Parse time exceptions
 * @author Igor
 *
 */
public class ParserCovariantErrorMultiTest extends BaseTest {
	
	/**
	 * PARSER_COVARIANT_VARIANT_DUPE
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantVariantDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1',                                       \n" +
				"        'parameters':{                                         \n" +
				"           'path':'/path/to/state1'                            \n" +
				"        }                                                      \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'parameters':                                          \n" +
				"        {  'path':'/path/to/state2' },                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3',                                       \n" +
				"        'parameters':{                                         \n" +
				"           'path':'/path/to/state3'                            \n" +
				"        }                                                      \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'isNonvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'           \n" +
			    "                    }                                            \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.B'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'           \n" +
			    "                   }                                            \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'           \n" +
			    "                    }                                            \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state3',                              \n" +
			    "              'isNonvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test3',                                       \n" +
                "        'covariantTestRefs': ['test1', 'test2'],              \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test3.B'           \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test2.B+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test2.C+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                       'path':'/path/to/state1/test1.C'           \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test2.B+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.C+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test3.B'           \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.B+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.C+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test2.B+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test2.C+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test2.B+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test2.C+test3.B'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +

			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test3.C'           \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.B+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.C+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test2.B+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test2.C+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test2.B+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test2.C+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    // DUPE:
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test2.B+test3.C'   \n" +
			    "                    }                                           \n" +
			    "                 }                                           \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state3',                              \n" +
			    "              'isNonvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = SchemaParser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_DUPE, "test1.B, test2.B", "test3", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_DUPE
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1',                                       \n" +
				"        'parameters':{                                         \n" +
				"           'path':'/path/to/state1'                            \n" +
				"        }                                                      \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'parameters':                                          \n" +
				"        {  'path':'/path/to/state2' },                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3',                                       \n" +
				"        'parameters':{                                         \n" +
				"           'path':'/path/to/state3'                            \n" +
				"        }                                                      \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'isNonvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'           \n" +
			    "                    }                                            \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.B'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'           \n" +
			    "                    }                                            \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'           \n" +
			    "                    }                                            \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state3',                              \n" +
			    "              'isNonvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test3',                                       \n" +
                "        'covariantTestRefs': ['test1', 'test2'],              \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test3.B'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test2.B+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test2.C+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test2.B+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.C+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test3.B'           \n" +
			    "                    }                                           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.B+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.C+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test2.B+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test2.C+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test2.B+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test2.C+test3.B'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +

			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test3.C'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.B+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.C+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +  // Should have been test2
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test2.B+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B+test2.C+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test2.B+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C+test2.C+test3.C'   \n" +
			    "                    }                                            \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state3',                              \n" +
			    "              'isNonvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = SchemaParser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_EXPERIENCE_DUPE, "test1", "B", "test3", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_MISSING, "test1.B,test2.B", "test3", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

}
