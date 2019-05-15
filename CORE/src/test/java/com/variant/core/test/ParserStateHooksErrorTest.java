package com.variant.core.test;

import static com.variant.core.schema.parser.error.SemanticError.ELEMENT_NOT_OBJECT;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_LIST;
import static com.variant.core.schema.parser.error.SemanticError.UNSUPPORTED_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.error.UserError.Severity;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserMessageImpl;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;
import com.variant.core.schema.parser.error.SemanticError.Location;

/**
 * Parse time exceptions related to hooks with test scope.
 * @author Igor
 *
 */
public class ParserStateHooksErrorTest extends BaseTestCore {
	
	/**
	 * HOOKS_NOT_LIST
	 * @throws Exception
	 */
	@Test
	public void hooksNotListTest() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'_schema_name',                                  \n" +
			    "      'comment':'a comment *&^'                               \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
			    "        'hooks':'cannot be a string'                          \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'TEST',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'path',                      \n" +
			    "                          'value':'/path/to/state1/test1.B'   \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/hooks/"), PROPERTY_NOT_LIST, "hooks");
		assertMessageEqual(expected, actual);
	}

	/**
	 * HOOKS_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void hooksNotObjectTest() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'_schema_name',                                  \n" +
			    "      'comment':'a comment *&^'                               \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     {                                                        \n" +
			    "        'nAmE':'state1',                                      \n" +
			    "        'hooks':[1,2,3]                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'TEST',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'path',                      \n" +
			    "                          'value':'/path/to/state1/test1.B'   \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(3, response.getMessages().size());
		for (int i = 0; i < 3; i++) {
			ParserMessage actual = response.getMessages().get(i);
			ParserMessage expected = new ParserMessageImpl(
					new Location(String.format("/states[0]/hooks[%s]/", i)), ELEMENT_NOT_OBJECT, "hooks");
			assertMessageEqual(expected, actual);
		}
	}

	/**
	 * HOOK_UNSUPPORTED_PROPERTY, HOOK_CLASS_NAME_MISSING
	 * @throws Exception
	 */
	@Test
	public void hookClassNameMissingTest() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'_schema_name',                                  \n" +
			    "      'comment':'a comment *&^'                               \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
			    "        'hooks':[{'class-Name':'c.v.s'}]                      \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'TEST',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'path',                      \n" +
			    "                          'value':'/path/to/state1/test1.B'   \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(2, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/hooks[0]/class-Name"), UNSUPPORTED_PROPERTY, "class-Name");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(new Location("/states[0]/hooks[0]/"), PROPERTY_MISSING, "class");
		assertMessageEqual(expected, actual);
	}

	/**
	 * HOOK_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void metaUnsupportedPropertyTest() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'_schema_name',                                  \n" +
			    "      'comment':'a comment *&^'                               \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     {                                                        \n" +
			    "        'hooks':[{'CLASS':'c.v.s', 'foo':true}],              \n" +
			    "        'name':'state1'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'TEST',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'path',                      \n" +
			    "                          'value':'/path/to/state1/test1.B'   \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/hooks[0]/foo"), UNSUPPORTED_PROPERTY, "foo");
		assertMessageEqual(expected, actual);
	}

}
