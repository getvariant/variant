package com.variant.core.test;

import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_TESTREF_NOT_STRING;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_TESTREF_UNDEFINED;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_TESTS_NOT_LIST;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_TEST_DISJOINT;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_VARIANT_MISSING;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT;
import static com.variant.core.schema.impl.MessageTemplate.PARSER_VARIANT_MISSING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.schema.impl.ParserMessageImplFacade;
import com.variant.core.schema.impl.ParserResponseImpl;
import com.variant.core.schema.impl.SchemaParser;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.Severity;

/**
 * Parse time exceptions
 * @author Igor
 *
 */
public class ParserCovariantErrorTest extends BaseTest {
	
	/**
	 * PARSER_COVARIANT_TESTS_NOT_LIST
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantTestsNotList_Test() throws Exception {
		
		String config = 
				"{                                                              \n" +
			    "   'states':[                                                  \n" +
			    "     {  'name':'state1',                                       \n" +
			    "        'parameters':{                                         \n" +
			    "           'path':'/path/to/state1'                            \n" +
			    "        }                                                      \n" +
			    "     },                                                        \n" +
			    "     {                                                         \n" +
			    "        'parameters':{                                         \n" +
			    "           'path':'/path/to/state2'                            \n" +
			    "        },                                                     \n" +
			    "        'name':'state2'                                        \n" +
			    "     }                                                         \n" +
			    "  ],                                                           \n" +
				"  'tests':[                                                    \n" +
			    "     {                                                         \n" +
			    "        'name':'test1',                                        \n" +
			    "        'experiences':[                                        \n" +
			    "           {                                                   \n" +
			    "              'name':'A',                                      \n" +
			    "              'weight':10,                                     \n" +
			    "              'isControl':true                                 \n" +
			    "           },                                                  \n" +
			    "           {                                                   \n" +
			    "              'name':'B',                                      \n" +
			    "              'weight':20                                      \n" +
			    "           },                                                  \n" +
			    "           {                                                   \n" +
			    "              'name':'C',                                      \n" +
			    "              'weight':30                                      \n" +
			    "           }                                                   \n" +
			    "        ],                                                     \n" +
			    "        'onStates':[                                            \n" +
			    "           {                                                   \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                     \n" +
			    "                 {                                             \n" +
			    "                    'experienceRef': 'B',                      \n" +
			    "                    'parameters':{                             \n" +
			    "                      'path':'/path/to/state1/test1.B'         \n" +
			    "                    }                                          \n" +
			    "                 },                                            \n" +
			    "                 {                                             \n" +
			    "                    'experienceRef': 'C',                      \n" +
			    "                    'parameters':{                             \n" +
			    "                      'path':'/path/to/state1/test1.C'         \n" +
			    "                    }                                          \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': 'test1',                         \n" +
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
			    "                      'path':'/path/to/state1/test1.B'        \n" +
			    "                    }                                          \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'         \n" +
			    "                    }                                          \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_TESTS_NOT_LIST, "test2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_COVARIANT_TEST_DISJOINT
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantTestDisjoint_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
	            "   'states':[                                                  \n" +
	            "     {  'name':'state1',                                       \n" +
	            "        'parameters':{                                         \n" +
	            "           'path':'/path/to/state1'                            \n" +
	            "        }                                                      \n" +
	            "     },                                                        \n" +
	            "     {                                                         \n" +
	            "        'parameters':{                                         \n" +
	            "           'path':'/path/to/state2'                            \n" +
	            "        },                                                     \n" +
	            "        'name':'state2'                                        \n" +
	            "     }                                                         \n" +
	            "  ],                                                           \n" +
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
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'parameters': {                           \n" +
			    "                      'path':'/path/to/state1/test1.B'        \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'        \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': ['test1'],                         \n" +
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
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B'        \n" +
			    "                   }                                          \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'        \n" +
			    "                   }                                          \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_TEST_DISJOINT, "test1", "test2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	
	/**
	 * PARSER_COVARIANT_TESTREF_NOT_STRING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantTestrefNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1',                                       \n" +
				"        'parameters':{                                         \n" +
				"           'path':'/path/to/state1'                            \n" +
				"        }                                                      \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'parameters':{                                         \n" +
				"           'path':'/path/to/state2'                            \n" +
				"        },                                                     \n" +
				"        'name':'state2'                                        \n" +
				"     }                                                         \n" +
				"  ],                                                           \n" +
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
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.B'        \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
				"                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'        \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': [1,{}],                          \n" +
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
			    "                      'path':'/path/to/state1/test1.B'        \n" +
			    "                   }                                          \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'        \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_TESTREF_NOT_STRING, "test2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_TESTREF_NOT_STRING, "test2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}
	
	/**
	 * PARSER_COVARIANT_TESTREF_UNDEFINED
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantTestrefUndefined_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1',                                       \n" +
				"        'parameters':{                                         \n" +
				"           'path':'/path/to/state1'                            \n" +
				"        }                                                      \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'parameters':{                                         \n" +
				"           'path':'/path/to/state2'                            \n" +
				"        },                                                     \n" +
				"        'name':'state2'                                        \n" +
				"     }                                                         \n" +
				"  ],                                                           \n" +
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
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.B'        \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'        \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': ['bad'],                         \n" +
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
			    "                      'path':'/path/to/state1/test1.B'        \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'        \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_TESTREF_UNDEFINED, "bad", "test2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST + PARSER_COVARIANT_VARIANT_MISSING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceRefsNotList_Test() throws Exception {
		
		
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
			    "                      'path':'/path/to/state2/test1.B'        \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'        \n" +
			    "                    }                                         \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                             \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.B'        \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test1.C'        \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                             \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B'           \n" +
	    	    "                    }                                            \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'           \n" +
	    	    "                    }                                            \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs':'notAList',     \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.C'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.C'   \n" +
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
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST, "test2", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_MISSING, "test1.B", "test2", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT + PARSER_COVARIANT_VARIANT_MISSING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceRefNotObject_Test() throws Exception {
		
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [23],          \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'           \n" +
	    	    "                    }                                            \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.C'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.C'   \n" +
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
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT, "test2", "state2", "B").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_MISSING, "test1.C", "test2", "state2", "B").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING + PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceTestRefNotString_Test() throws Exception {
		
		
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
			    "                      'path':'/path/to/state2/test1.B'        \n" +
			    "                    }                                         \n" +
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'           \n" +
	    	    "                    }                                            \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': {},                      \n" +
	    	    "                          'experienceRef': 34                 \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.C'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.C'   \n" +
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
		
		ParserResponseImpl response = SchemaParser.parse(api, config);
		
		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(3, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING, "test2", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING, "test2", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(2);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_MISSING, "test1.B", "test2", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceTestRefUndefined_Test() throws Exception {
		
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                             \n" +
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'           \n" +
	    	    "                    }                                            \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'bad',                   \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.C'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.C'   \n" +
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
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED, "bad", "test2", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_MISSING, "test1.B", "test2", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}


	/**
	 * PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceExperienceRefUndefined_Test() throws Exception {
		
		
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.C'           \n" +
	    	    "                    }                                            \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'Bad'              \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.C'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.C'   \n" +
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
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, "test1", "Bad", "test2", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_MISSING, "test1.B", "test2", "state2", "C").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantVariantTestNotCovariant_Test() throws Exception {
		
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test2.B'           \n" +
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
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT, "test1", "B", "test2", "state2", "B").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceTestRefNonvariant_Test() throws Exception {
		
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
                "        'covariantTestRefs':['test1'],                        \n" +
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
			    "                      'path':'/path/to/state1/test2.B'           \n" +
	    	    "                   }                                            \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    // Invalid because state1 is nonvariant in test1.
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state1/test1.B+test2.B'   \n" +
	    	    "                   }                                            \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state1/test2.C'           \n" +
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.B'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.C'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test2.C'           \n" +
	    	    "                    }                                            \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.B+test2.C'   \n" +
	    	    "                    }                                            \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'parameters':{                            \n" +
	    	    "                      'path':'/path/to/state2/test1.C+test2.C'   \n" +
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
		
		ParserResponseImpl response = SchemaParser.parse(api, config);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_EXPERIENCE_TEST_REF_NONVARIANT, "test1", "test2", "state1", "B").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}


	/**
	 * PARSER_COVARIANT_VARIANT_MISSING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantVariantMissing_Test() throws Exception {
		
		
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
/*			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'parameters':{                            \n" +
			    "                      'path':'/path/to/state2/test1.B'           \n" +
			    "                    }                                            \n" +
			    "                 },                                           \n" +
*/			    "                 {                                            \n" +
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
/*			    "                 {                                            \n" +
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
*/			    "                 {                                            \n" +
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
			    "                    }                                            \n" +
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
/*			    "                 {                                            \n" +
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
*/			    "                 {                                            \n" +
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
	    	    "                          'testRef': 'test2',                 \n" +
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
		
		ParserResponseImpl response = SchemaParser.parse(api, config);
		
		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(3, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_VARIANT_MISSING, "B", "test2", "state2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_MISSING, "test2.C", "test3", "state1", "B").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(2);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_MISSING, "test1.B,test2.B", "test3", "state2", "B").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

}
