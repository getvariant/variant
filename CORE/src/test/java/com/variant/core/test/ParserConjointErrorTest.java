package com.variant.core.test;

import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_EXPERIENCE_TEST_REF_NONVARIANT;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_EXPERIENCE_TEST_REF_UNDEFINED;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_TESTREF_UNDEFINED;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_TEST_DISJOINT;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_VARIANT_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_VARIANT_TEST_NOT_CONJOINT;
import static com.variant.core.schema.parser.error.SemanticError.ELEMENT_NOT_OBJECT;
import static com.variant.core.schema.parser.error.SemanticError.ELEMENT_NOT_STRING;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_LIST;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_STRING;
import static com.variant.core.schema.parser.error.SemanticError.PROPER_VARIANT_MISSING;
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
public class ParserConjointErrorTest extends BaseTestCore {
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void conjointTestsNotList_Test() throws Exception {
		
		String schema = 
				"{                                                              \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                  \n" +
			    "     {  'name':'state1'                                       \n" +
			    "     },                                                        \n" +
			    "     {                                                         \n" +
			    "        'name':'state2'                                        \n" +
			    "     }                                                         \n" +
			    "  ],                                                           \n" +
				"  'variations':[                                               \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                   \n" +
			    "              'stateRef':'state1',                             \n" +
			    "              'variants':[                                     \n" +
			    "                 {                                             \n" +
			    "                    'experienceRef': 'B'                       \n" +
			    "                 },                                            \n" +
			    "                 {                                             \n" +
			    "                    'experienceRef': 'C'                       \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'conjointVariationRefs': 'test1',                         \n" +
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
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/conjointVariationRefs"), 
				PROPERTY_NOT_LIST, "conjointVariationRefs");
		assertMessageEqual(expected, actual);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void conjointTestDisjoint_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
	            "   'states':[                                                  \n" +
	            "     {  'name':'state1'                                        \n" +
	            "     },                                                        \n" +
	            "     {                                                         \n" +
	            "        'name':'state2'                                        \n" +
	            "     }                                                         \n" +
	            "  ],                                                           \n" +
				"  'variations':[                                               \n" +
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
	    	    "        'conjointVariationRefs': ['test1'],                         \n" +
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
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/conjointVariationRefs[0]"), 
				CONJOINT_TEST_DISJOINT, "test2", "test1");
		assertMessageEqual(expected, actual);
	}

	
	/**
	 * @throws Exception
	 */
	@Test
	public void conjointTestrefNotString_Test() throws Exception {
		
		String schema = 
				"{                                                              \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     }                                                         \n" +
				"  ],                                                           \n" +
				"  'variations':[                                               \n" +
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
	    	    "        'conjointVariationRefs': [1,{}],                          \n" +
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
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(schema);

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
				new Location("/variations[1]/conjointVariationRefs[0]"), 
				ELEMENT_NOT_STRING, "conjointVariationRefs");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/conjointVariationRefs[1]"), 
				ELEMENT_NOT_STRING, "conjointVariationRefs");
		assertMessageEqual(expected, actual);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void conjointTestrefUndefined_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     }                                                         \n" +
				"  ],                                                           \n" +
				"  'variations':[                                              \n" +
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
	    	    "        'conjointVariationRefs': ['bad'],                         \n" +
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
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/conjointVariationRefs[0]"), 
				CONJOINT_TESTREF_UNDEFINED, "bad", "test2");
		assertMessageEqual(expected, actual);	
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void conjointExperienceRefsNotList_Test() throws Exception {	
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3'                                        \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'isNonvariant':true                             \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                            \n" +
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
	    	    "        'conjointVariationRefs': ['test1'],                       \n" +
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
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                             \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs':'notAList'      \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'variationRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ]                                         \n" +
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
		ParserResponse response = (ParserResponse) parser.parse(schema);

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
				new Location("/variations[1]/onStates[1]/variants[4]/conjointExperienceRefs/"), 
				PROPERTY_NOT_LIST, "conjointExperienceRefs");
		assertMessageEqual(actual, expected);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants/"), 
				CONJOINT_VARIANT_MISSING, "C", "test1.B");
		assertMessageEqual(expected, actual);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void conjointExperienceRefNotObject_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3'                                       \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'isNonvariant':true                             \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                            \n" +
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
	    	    "        'conjointVariationRefs': ['test1'],                       \n" +
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
			    "              'stateRef':'state2',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [23]           \n" +  // instead of test1.C
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'variationRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ]                                         \n" +
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
		ParserResponse response = (ParserResponse) parser.parse(schema);

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
				new Location("/variations[1]/onStates[1]/variants[2]/conjointExperienceRefs[0]/"), 
				ELEMENT_NOT_OBJECT, "conjointExperienceRefs");
		assertMessageEqual(actual, expected);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants/"), 
				CONJOINT_VARIANT_MISSING, "B", "test1.C");
		assertMessageEqual(expected, actual);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void conjointExperienceTestRefNotString_Test() throws Exception {
		
		
		String schema = 
				"{                                                              \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                        \n" +
				"     {  'name':'state3'                                        \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
	    	    "        'conjointVariationRefs': ['test1'],                       \n" +
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': {},                      \n" +  // Not a string.
	    	    "                          'experienceRef': 34                 \n" +  // Not a string
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'variationRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ]                                         \n" +
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
		ParserResponse response = (ParserResponse) parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(3, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants[4]/conjointExperienceRefs[0]/variationRef"), 
				PROPERTY_NOT_STRING, "variationRef");
		assertMessageEqual(actual, expected);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants[4]/conjointExperienceRefs[0]/experienceRef"), 
				PROPERTY_NOT_STRING, "experienceRef");
		assertMessageEqual(actual, expected);
		actual = response.getMessages().get(2);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants/"), 
				CONJOINT_VARIANT_MISSING, "C", "test1.B");
		assertMessageEqual(expected, actual);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void conjointExperienceTestRefUndefined_Test() throws Exception {
		
		String schema = 
				"{                                                              \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                        \n" +
				"     {  'name':'state3'                                        \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
	    	    "        'conjointVariationRefs': ['test1'],                       \n" +
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'bad',                   \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'variationRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ]                                         \n" +
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
		ParserResponse response = (ParserResponse) parser.parse(schema);
		
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
				new Location("/variations[1]/onStates[1]/variants[4]/conjointExperienceRefs[0]/variationRef"), 
				CONJOINT_EXPERIENCE_TEST_REF_UNDEFINED, "bad");
		assertMessageEqual(actual, expected);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants/"), 
				CONJOINT_VARIANT_MISSING, "C", "test1.B");
		assertMessageEqual(expected, actual);
	}


	/**
	 * @throws Exception
	 */
	@Test
	public void conjointExperienceExperienceRefUndefined_Test() throws Exception {
		
		
		String schema = 
				"{                                                              \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3'                                        \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'isNonvariant':true                             \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                            \n" +
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
	    	    "        'conjointVariationRefs': ['test1'],                       \n" +
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'Bad'              \n" +  // Non-existent
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'variationRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ]                                         \n" +
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
		ParserResponse response = (ParserResponse) parser.parse(schema);

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
				new Location("/variations[1]/onStates[1]/variants[4]/conjointExperienceRefs[0]/experienceRef"), 
				CONJOINT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, "test1", "Bad");
		assertMessageEqual(actual, expected);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants/"), 
				CONJOINT_VARIANT_MISSING,  "C", "test1.B");
		assertMessageEqual(expected, actual);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void conjointVariantTestNotConjoint_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3'                                        \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
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
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants[1]/conjointExperienceRefs[0]/experienceRef"), 
				CONJOINT_VARIANT_TEST_NOT_CONJOINT, "test1");
		assertMessageEqual(expected, actual);

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void conjointExperienceTestRefNonvariant_Test() throws Exception {
		
		String schema = 
				"{                                                              \n" +
			    "  'meta':{                                                     \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3'                                        \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
                "        'conjointVariationRefs':['test1'],                        \n" +
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    // Invalid because state1 is nonvariant in test1.
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
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
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
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
		ParserResponse response = (ParserResponse) parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[0]/variants[1]/conjointExperienceRefs[0]/experienceRef"), 
				CONJOINT_EXPERIENCE_TEST_REF_NONVARIANT, "test1", "state1");
		assertMessageEqual(expected, actual);

	}


	/**
	 * @throws Exception
	 */
	@Test
	public void conjointVariantMissing_Test() throws Exception {
		
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                     \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"   'states':[                                                  \n" +
				"     {  'name':'state1'                                        \n" +
				"     },                                                        \n" +
				"     {                                                         \n" +
				"        'name':'state2'                                        \n" +
				"     },                                                       \n" +
				"     {  'name':'state3'                                        \n" +
				"     }                                                        \n" +
				"  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
                "        'conjointVariationRefs': ['test1'],                       \n" +
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
/*			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
*/			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ]                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
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
                "        'conjointVariationRefs': ['test1', 'test2'],              \n" +
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
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
/*			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
*/			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
/*			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
*/			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +

			    "                 {                                            \n" +
			    "                    'experienceRef': 'C'                      \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ]                                        \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'conjointExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'variationRef': 'test2',                 \n" +
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
		ParserResponse response = (ParserResponse) parser.parse(schema);

		assertTrue(response.hasMessages());
		assertNull(response.getSchema());
		assertNull(response.getSchemaSrc());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(3, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants/"), 
					PROPER_VARIANT_MISSING, "B");
		assertMessageEqual(actual, expected);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[2]/onStates[0]/variants/"), 
				CONJOINT_VARIANT_MISSING, "B", "test2.C");
		assertMessageEqual(actual, expected);
		actual = response.getMessages().get(2);
		expected = new ParserMessageImpl(
				new Location("/variations[2]/onStates[1]/variants/"), 
				CONJOINT_VARIANT_MISSING, "B", "test1.B,test2.B");
		assertMessageEqual(expected, actual);

	}

}
