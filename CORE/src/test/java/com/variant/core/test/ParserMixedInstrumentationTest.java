package com.variant.core.test;

import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_VARIANT_CONJOINT_PHANTOM;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_VARIANT_PROPER_PHANTOM;
import static com.variant.core.schema.parser.error.SemanticError.EXPERIENCEREF_ISCONTROL;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_ALLOWED_PHANTOM_VARIANT;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_BOOLEAN;
import static com.variant.core.schema.parser.error.SemanticError.PROPER_VARIANT_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.CONJOINT_VARIANT_MISSING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.parser.ParserMessageImpl;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.schema.parser.error.SemanticError.Location;


/**
 * Test mixed instrumentation, i.e. when an experience is not defined
 * on a particular state, as denoted by the tests/onState/isPhantom element.
 *
 * @author Igor 
 *
 */
public class ParserMixedInstrumentationTest extends BaseTestCore {


	/**
	 * Parse time errors.
	 */
	@org.junit.Test
	public void errorsTest1() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
					    "  'meta':{                                                                \n" +
					    "      'name':'errorsTest1'                                                \n" +
					    "  },                                                                      \n" +
			    	    /* ======================================================================== */
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    /* ======================================================================= */
			    	    
				        "  'variations':[                                                         \n" +
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
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isPhantom':true,                                    \n" +
						"                    'parameters': [    // not allowed                    \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test1.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
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
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    // control allowed,                                  \n" +
			    	    "                    'experienceRef':'A',                                 \n" + 
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 }                                                       \n" +
			    	    "                 // B is missing.                                        \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test3',                                                  \n" +
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
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" + 
			    	    "                    'isPhantom':34   // not a boolean.                   \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test4',                                                  \n" +
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
			    	    "                    'experienceRef':'B',                                 \n" + 
			    	    "                    'isPhantom': {'foo':'bar'} // Not a boolean          \n" + 
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test3.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                  \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(SCHEMA);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));

		assertEquals(6, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants[0]/parameters/"), 
				PROPERTY_NOT_ALLOWED_PHANTOM_VARIANT, "parameters");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		assertTrue("PROPER_VARIANT_MISSING should be deleted!", false);
		expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants/"), 
				PROPER_VARIANT_MISSING, "B");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(2);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[0]/variants/"), 
				PROPER_VARIANT_MISSING, "B");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(3);
		expected = new ParserMessageImpl(
				new Location("/variations[2]/onStates[0]/variants[0]/isPhantom"), 
				PROPERTY_NOT_BOOLEAN, "isPhantom");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(4);
		expected = new ParserMessageImpl(
				new Location("/variations[3]/onStates[0]/variants[0]/isPhantom"), 
				PROPERTY_NOT_BOOLEAN, "isPhantom");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(5);
		expected = new ParserMessageImpl(
				new Location("/variations[3]/onStates[0]/variants[1]/experienceRef"), 
				EXPERIENCEREF_ISCONTROL, "A");
		assertMessageEqual(expected, actual);
	}

	/**
	 * More parse time errors.
	 */
	@org.junit.Test
	public void errorsTest2() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
					    "  'meta':{                                                                \n" +
					    "      'name':'errorsTest2'                                                \n" +
					    "  },                                                                      \n" +
			    	    /* ======================================================================== */
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    /* ======================================================================= */			    	    
				        "  'variations':[                                                         \n" +
			    	    "     {                                                                   \n" +
				        "        /// B is undef on S1 and A is undef on S2                        \n" +
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
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
				        "        // Covaraint with T1                                             \n" +
				        "        //  A is undef on S1 and B is undef on S2                        \n" +
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
			    	    "        'conjointVariationRefs':['test1'],                                   \n" +
			    	    "        'onStates':[                                                     \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 }                                                       \n" +
				        "                 // ERROR: need the def for the proper B                 \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 }                                                       \n" +
				        "                 // Not an error: don't need hybrid def for T1.B         \n" +
				        "                 // becose proper B is off => the entire slice is also   \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(SCHEMA);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));

		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[0]/variants/"), 
				PROPER_VARIANT_MISSING, "B");
		assertMessageEqual(expected, actual);

	}

	/**
	 * More parse time errors.
	 */
	@org.junit.Test
	public void errorsTest3() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
					    "  'meta':{                                                                \n" +
					    "      'name':'errorsTest3'                                                \n" +
					    "  },                                                                      \n" +
			    	    /* ======================================================================== */
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    /* ======================================================================= */			    	    
				        "  'variations':[                                                          \n" +
			    	    "     {                                                                   \n" +
				        "        /// B is undef on S1 and A is undef on S2                        \n" +
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
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
				        "        // Covaraint with T1                                             \n" +
				        "        //  A is undef on S1 and B is undef on S2                        \n" +
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
			    	    "        'conjointVariationRefs':['test1'],                                   \n" +
			    	    "        'onStates':[                                                     \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test2.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
				        "                 // Error: covar A is off => the entire slice is too    \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test1.B-test2.B'      \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
				        "                 // Error: proper B is off => the entire slice is too    \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.B-test2.B'      \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(SCHEMA);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));

		assertEquals(2, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[0]/variants/"), 
				CONJOINT_VARIANT_CONJOINT_PHANTOM, "test1.B", "state1");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants/"), 
				CONJOINT_VARIANT_PROPER_PHANTOM, "test2.B", "state2");
		assertMessageEqual(expected, actual);
	}
	
	/**
	 * More parse time errors.
	 */
	@org.junit.Test
	public void errorsTest4() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
					    "  'meta':{                                                                \n" +
					    "      'name':'errorsTest4'                                                \n" +
					    "  },                                                                      \n" +
			    	    /* ======================================================================== */
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    /* ======================================================================= */			    	    
				        "  'variations':[                                                         \n" +
			    	    "     {                                                                   \n" +
				        "        /// B is undef on S1 and A is undef on S2                        \n" +
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
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
				        "        // Covaraint with T1                                             \n" +
				        "        //  A is undef on S1 and B is undef on S2                        \n" +
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
			    	    "        'conjointVariationRefs':['test1'],                                   \n" +
			    	    "        'onStates':[                                                     \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test2.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
				        "                 // Error: covar A is undef on => the entire slice is too    \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test1.B-test2.B'      \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
				        "                 // Error: proper B is off => the entire slice is too    \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.B-test2.B'      \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(SCHEMA);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));

		assertEquals(2, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[0]/variants/"), 
				CONJOINT_VARIANT_CONJOINT_PHANTOM, "test1.B", "state1");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[1]/variants/"), 
				CONJOINT_VARIANT_PROPER_PHANTOM, "test2.B", "state2");
		assertMessageEqual(expected, actual);
	}
	
	/**
	 * No errors test
	 */
	@org.junit.Test
	public void errorsTest5() throws Exception {

		/*
		 *      S1 S2 S3
		 * T1.A  +  -  =
		 * T1.B  -  +  =
		 * T1.C  +  -  =
		 * T1.D  -  +  =
         *
		 * T2.A  -  +  +
		 * T2.B  +  +  +
		 * T2.C  -  +  -
		 * T2.D  +  +  -
         *
		 */
		final String SCHEMA = 
				"{                                                                                 \n" +
					    "  'meta':{                                                                \n" +
					    "      'name':'errorsTest5'                                                \n" +
					    "  },                                                                      \n" +
			    	    /* ======================================================================== */
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    /* ======================================================================= */			    	    
				        "  'variations':[                                                         \n" +
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
			    	    "                    'isPhantom':true                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test1.C'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
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
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'conjointVariationRefs':['test1'],                                   \n" +
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
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test2.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
/*	This conjoint variant is missing.		
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test1.C+test2.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
					    "                 },                                                      \n" +
*/
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'isPhantom':true                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test2.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test1.C+test2.D'      \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
					    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(SCHEMA);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));

		assertEquals(1, response.getMessages().size());
		assertTrue("CONJOINT_VARIANT_MISSING should be deleted!", false);
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[1]/onStates[0]/variants/"), 
				CONJOINT_VARIANT_MISSING, "B", "test1.C");
		assertMessageEqual(expected, actual);

	}
	
	/**
	 * No errors test
	 */
	@org.junit.Test
	public void okayTest1() throws Exception {

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
					    "  'meta':{                                                                \n" +
					    "      'name':'okayTest1'                                                  \n" +
					    "  },                                                                      \n" +
			    	    /* ======================================================================== */
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'},                                                   \n" +
			    	    "     {'name':'state3'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    /* ======================================================================= */			    	    
				        "  'variations':[                                                         \n" +
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
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test1.C'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 }                                                      \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
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
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                       \n" +
			    	    "              'isNonvariant':true                                        \n" +
			    	    "           }                                                            \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'conjointVariationRefs':['test1'],                                   \n" +
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
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test2.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test1.C+test2.B'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
					    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'isPhantom':true                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test2.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test1.C+test2.C'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
					    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
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
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
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
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'D'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.D+test2.B'      \n" +
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
					    "                    'experienceRef': 'C',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.B+test2.C'      \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
					    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'C',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'D'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.C+test2.D'      \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
					    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test2.C'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.B+test2.D'      \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
					    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'conjointExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'variationRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'D'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test1.D+test2.D'      \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
					    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 },                                                      \n" +
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
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state3/test2.C'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
			    	    "                    'isPhantom':true                                     \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";

		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(SCHEMA);

		assertFalse(response.hasMessages());
	}
}

