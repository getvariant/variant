package com.variant.core.test;

import static com.variant.core.schema.parser.error.SemanticError.COVARIANT_EXPERIENCE_DUPE;
import static com.variant.core.schema.parser.error.SemanticError.COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED;
import static com.variant.core.schema.parser.error.SemanticError.COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT;
import static com.variant.core.schema.parser.error.SemanticError.COVARIANT_VARIANT_DUPE;
import static com.variant.core.schema.parser.error.SemanticError.COVARIANT_VARIANT_MISSING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.parser.ParserMessageImpl;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.schema.parser.error.SemanticError.Location;

/**
 * Parse time exceptions
 * @author Igor
 *
 */
public class ParserCovariantErrorMultiTest extends BaseTestCore {
	
	/**
	 * PARSER_COVARIANT_VARIANT_DUPE
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantVariantDupe_Test() throws Exception {
		
		String schema = 
				"{                                                              \n" +
			    "  'meta':{                                                     \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1',                                       \n" +
				"        'parameters': [                                        \n" +
				"          {'name':'foo', 'value':'bar'}                        \n" +
				"        ]                                                      \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3'                                       \n" +
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
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
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
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
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
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" + // Dupe variant
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
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
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));

		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected =  new ParserMessageImpl(new Location("/tests[2]/onStates[1]/variants[10]/"), COVARIANT_VARIANT_DUPE, "test1.C", "test3", "state2", "C");
		assertMessageEqual(actual, expected);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceDupe_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1',                                       \n" +
				"        'parameters': [                                        \n" +
				"           {                                                   \n" +
				"             'name': 'path',                                   \n" +
				"             'value': '/path/to/state1'                        \n" +
				"           },                                                  \n" +
				"           {                                                   \n" +
				"             'name': 'bar',                                    \n" +
				"             'value': 'foo'                                    \n" +
				"           }                                                   \n" +
				"        ]                                                      \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'parameters': [                                        \n" +
				"           {                                                   \n" +
				"             'name': 'path',                                   \n" +
				"             'value': '/path/to/state2'                        \n" +
				"           },                                                  \n" +
				"           {                                                   \n" +
				"             'name': 'bar',                                    \n" +
				"             'value': 'foo'                                    \n" +
				"           }                                                   \n" +
				"        ],                                                     \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3',                                       \n" +
				"        'parameters': [                                        \n" +
				"           {                                                   \n" +
				"             'name': 'path',                                   \n" +
				"             'value': '/path/to/state3'                        \n" +
				"           },                                                  \n" +
				"           {                                                   \n" +
				"             'name': 'bar',                                    \n" +
				"             'value': 'foo'                                    \n" +
				"           }                                                   \n" +
				"        ]                                                      \n" +
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
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
                "        'covariantTestRefs': ['test1'],                       \n" +
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
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
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
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
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
	    	    "                     ]                                        \n" +
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
	    	    "                     ]                                        \n" +
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
	    	    "                     ]                                        \n" +
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
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +

			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
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
	    	    "                     ]                                        \n" +
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
	    	    "                    ]                                         \n" +
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
	    	    "                     ]                                        \n" +
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
	    	    "                     ]                                        \n" +
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
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));

		assertEquals(2, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/tests[2]/onStates[1]/variants[14]/covariantExperienceRefs[1]/experienceRef/"), 
				COVARIANT_EXPERIENCE_DUPE, "test1", "B");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/tests[2]/onStates[1]/variants/"), COVARIANT_VARIANT_MISSING, "C", "test1.B,test2.B", "test3", "state2");
		assertMessageEqual(expected, actual);

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT
	 * PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED
	 */
	@Test
	public void testMore() throws Exception {
		
		final String schema = 

		"{                                                                                \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
	    	    //==========================================================================//
	    	   
	    	    "   'states':[                                                             \n" +
	    	    "     {  'name':'state1',                                                  \n" +
				"        'parameters': [                                        \n" +
				"           {                                                   \n" +
				"             'name': 'path',                                   \n" +
				"             'value': '/path/to/state1'                        \n" +
				"           },                                                  \n" +
				"           {                                                   \n" +
				"             'name': 'bar',                                    \n" +
				"             'value': 'foo'                                    \n" +
				"           }                                                   \n" +
				"        ]                                                      \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'NAME':'state2',                                                 \n" +
				"        'parameters': [                                        \n" +
				"           {                                                   \n" +
				"             'name': 'path',                                   \n" +
				"             'value': '/path/to/state2'                        \n" +
				"           },                                                  \n" +
				"           {                                                   \n" +
				"             'name': 'bar',                                    \n" +
				"             'value': 'foo'                                    \n" +
				"           }                                                   \n" +
				"        ]                                                      \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'nAmE':'state3',                                                 \n" +
				"        'parameters': [                                        \n" +
				"           {                                                   \n" +
				"             'name': 'path',                                   \n" +
				"             'value': '/path/to/state3'                        \n" +
				"           },                                                  \n" +
				"           {                                                   \n" +
				"             'name': 'bar',                                    \n" +
				"             'value': 'foo'                                    \n" +
				"           }                                                   \n" +
				"        ]                                                      \n" +
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
	    	    "           }                                                             \n" +
	    	    "        ],                                                               \n" +
	    	    "        'onStates':[                                                     \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state2',                                       \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B'                                  \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state3',                                       \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B'                                  \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    //--------------------------------------------------------------------------//	
	    	    "     {                                                                   \n" +
	    	    "        'name':'test2',                                                  \n" +
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
	    	    "        'onStates':[                                                     \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state1',                                       \n" +
	    	    "              'isNonvariant':true                                        \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state2',                                       \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B'                                  \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state3',                                       \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B'                                  \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    //--------------------------------------------------------------------------//	
	    	    "     {                                                                   \n" +
	    	    "        'name':'test3',                                                  \n" +
	    	    "        'covariantTestRefs': ['test2', 'test1'],                         \n" +
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
	    	    "              'stateRef':'state1',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B'                                  \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state2',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B'                                  \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ]                                                   \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test2',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ]                                                   \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +  // Invalid because T1 and T2 are not covariant.
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       },                                                \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test2',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ]                                                   \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" + // Invalid because T1 and T2 are not covariant + there's no T2.C
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       },                                                \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test2',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ]                                                   \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" + // Invalid because T1 and T2 are not covariant + there's no T1.C
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       },                                                \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test2',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ]                                                   \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" + // Invalid because T1 and T2 are not covariant + there's no T1.C nor T2.C
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       },                                                \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test2',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ]                                                   \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'stateRef':'state3',                                        \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     }                                                                   \n" +
	    	    //--------------------------------------------------------------------------//	
	     	    "  ]                                                                      \n" +
	    	    "}                                                                         ";

		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));

		assertEquals(4, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/tests[2]/onStates[1]/variants[3]/covariantExperienceRefs[1]/"), 
				COVARIANT_EXPERIENCE_REF_TESTS_NOT_COVARIANT, "test1, test2");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/tests[2]/onStates[1]/variants[4]/covariantExperienceRefs[1]/experienceRef/"), 
				COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, "test2", "C");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(2);
		expected = new ParserMessageImpl(
				new Location("/tests[2]/onStates[1]/variants[5]/covariantExperienceRefs[0]/experienceRef/"), 
				COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, "test1", "C");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(3);
		expected = new ParserMessageImpl(
				new Location("/tests[2]/onStates[1]/variants[6]/covariantExperienceRefs[0]/experienceRef/"), 
				COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, "test1", "C");
		assertMessageEqual(expected, actual);
	}
	
}
