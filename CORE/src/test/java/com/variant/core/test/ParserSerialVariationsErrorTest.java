package com.variant.core.test;

import static com.variant.core.schema.parser.error.SemanticError.*;
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
 * Parse time exceptions
 * @author Igor
 *
 */
public class ParserSerialVariationsErrorTest extends BaseTestCore {
	
	/**
	 * NO_TESTS_CLAUSE
	 * @throws Exception
	 */
	@Test
	public void noTestsClause_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +			    	   
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "         'parameters': [                                      \n" +
			    "            {                                                 \n" +
			    "               'name':'foo',                                  \n" +
			    "               'value':'bar'                                  \n" +
			    "            },                                                \n" +
			    "            {                                                 \n" +
			    "               'name':'bar',                                  \n" +
			    "               'value':'foo'                                  \n" +
			    "            }                                                 \n" +
			    "        ]                                                     \n" +
			    "      }                                                       \n" +
			    "   ]                                                          \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/"), PROPERTY_MISSING, "variations");
		assertMessageEqual(expected, actual);
	}

	/**
	 * NO_TESTS
	 * @throws Exception
	 */
	@Test
	public void noTests_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +			    	   
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations/"), PROPERTY_EMPTY_LIST, "variations");
		assertMessageEqual(expected, actual);
	}

	/**
	 * TEST_ISON_NOT_BOOLEAN
	 * @throws Exception
	 */
	@Test
	public void testIsOnNotBoolean_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'Test1',                                       \n" +
			    "        'isOn':'false',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'foo',                       \n" +
			    "                          'value':'bar'                       \n" +
			    "                       },                                     \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                       \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/isOn"), PROPERTY_NOT_BOOLEAN, "isOn");
		assertMessageEqual(expected, actual);
	}

	/**
	 * TEST_NAME_MISSING
	 * @throws Exception
	 */
	@Test
	public void testNameMissing_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
//			    "        'name':'Test1',                                       \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/"), NAME_MISSING);
		assertMessageEqual(expected, actual);
	}

	/**
	 * TEST_NAME_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void testNameNotString_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':23,                                            \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/name"), NAME_INVALID);
		assertMessageEqual(expected, actual);
	}
	
	/**
	 * TEST_NAME_DUPE
	 * @throws Exception
	 */
	@Test
	public void testNameDupe_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'path',                      \n" +
			    "                          'value':'/path/to/state1/test1.A'   \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'path',                      \n" +
			    "                          'value':'/path/to/state1/test1.A'   \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[1]/"), DUPE_OBJECT, "test1");
		assertMessageEqual(expected, actual);
	}

	/**
	 * TEST_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void testUnsupportedProperty_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'unsupported':[],                                     \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A'                       \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/unsupported"), UNSUPPORTED_PROPERTY, "unsupported");
		assertMessageEqual(expected, actual);
	}

	/**
	 * EXPERIENCES_NOT_LIST
	 * @throws Exception
	 */
	@Test
	public void experienceNotList_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':{'foo':'bar'},                          \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/experiences/"), PROPERTY_NOT_LIST, "experiences");
		assertMessageEqual(expected, actual);
	}


	/**
	 * EXPERIENCES_LIST_EMPTY + PARSER_IS_CONTROL_MISSING + EXPERIENCEREF_UNDEFINED
	 * @throws Exception
	 */
	@Test
	public void experiencesListEmpty_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A'                       \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/experiences/"), PROPERTY_EMPTY_LIST, "experiences");
		assertMessageEqual(expected, actual);
	}

	/**
	 * EXPERIENCE_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void experiencesNotObject_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':false                               \n" +
			    "           },                                                 \n" +
			    "           []                                                 \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'B'                       \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/experiences[2]/"), ELEMENT_NOT_OBJECT, "experiences");
		assertMessageEqual(expected, actual);
	}

	/**
	 * EXPERIENCE_NAME_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void experienceNameNotString_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':234,                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'' ,                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'$abc' ,                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'a&bc' ,                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'_%-bc' ,                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'7abc' ,                                     \n" +
			    "              'weight':50                                     \n" +
			    "           }                                                 \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {'experienceRef':'B'}                        \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(6, response.getMessages().size());
		for (int i = 0; i < 6; i++) {
			ParserMessage actual = response.getMessages().get(i);
			ParserMessage expected = new ParserMessageImpl(
					new Location(String.format("/variations[0]/experiences[%s]/name", i+2)), NAME_INVALID);
			assertMessageEqual(expected, actual);
		}
	}

	/**
	 * ISCONTROL_NOT_BOOLEAN
	 * @throws Exception
	 */
	@Test
	public void isControlNotBoolean_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'isControl':'false',                            \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/experiences[0]/isControl"), PROPERTY_NOT_BOOLEAN, "isControl");
		assertMessageEqual(expected, actual);
	}

	/**
	 * WEIGHT_NOT_NUMBER + 
	 * @throws Exception
	 */
	@Test
	public void weightNotNumber_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':'40'                                   \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A'                       \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/experiences[0]/weight"), PROPERTY_NOT_NUMBER, "weight");
		assertMessageEqual(expected, actual);
	}

	/**
	 * EXPERIENCE_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void experienceUnsupportedProperty_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'unsupported':{}                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/experiences[0]/unsupported"), UNSUPPORTED_PROPERTY, "unsupported");
		assertMessageEqual(expected, actual);
	}

	/**
	 * ONSTATES_NOT_LIST
	 * @throws Exception
	 */
	@Test
	public void onStatesNotList_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A'                       \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "                                                              \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/onStates/"), PROPERTY_NOT_LIST, "onStates");
		assertMessageEqual(expected, actual);
	}

	/**
	 * ONSTATES_LIST_EMPTY
	 * @throws Exception
	 */
	@Test
	public void onStatesListEmpty_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates': []                                         \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/onStates/"), PROPERTY_EMPTY_LIST, "onStates");
		assertMessageEqual(expected, actual);
	}
	

	/**
	 * ONSTATE_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void onStateNotObject_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[45]                                        \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/"), ELEMENT_NOT_OBJECT, "onStates");
		assertMessageEqual(expected, actual);
	}

	/**
	 * STATEREF_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void stateRefNotString_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':3456789,                             \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'foo',                       \n" +
			    "                          'value':'bar'                       \n" +
			    "                       },                                     \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                       \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
	    assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/stateRef"), PROPERTY_NOT_STRING, "stateRef");
		assertMessageEqual(expected, actual);
	}

	/**
	 * STATEREF_MISSING
	 * @throws Exception
	 */
	@Test
	public void stateRefMissing_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/"), PROPERTY_MISSING, "stateRef");
		assertMessageEqual(expected, actual);
	}

	/**
	 * STATEREF_DUPE
	 * @throws Exception
	 */
	@Test
	public void stateRefDupe_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A'                       \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A'                       \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/onStates[1]/"), DUPE_OBJECT, "state1");
		assertMessageEqual(expected, actual);
	}

	/**
	 * STATEREF_UNDEFINED
	 * @throws Exception
	 */
	@Test
	public void stateRefUndefined_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'State1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/stateRef"), STATEREF_UNDEFINED, "State1");
		assertMessageEqual(expected, actual);
	}


	/**
	 * VARIANTS_NOT_LIST
	 * @throws Exception
	 */
	@Test
	public void variantsNotList_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':                                     \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A'                       \n" +
			    "                 }                                            \n" +
			    "                                                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants"), PROPERTY_NOT_LIST, "variants");
		assertMessageEqual(expected, actual);
	}


	/**
	 * VARIANTS_LIST_EMPTY
	 * @throws Exception
	 */
	@Test
	public void variantsListEmpty_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'VARIANTS':[                                    \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(2, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/"), PROPERTY_MISSING, "states");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/stateRef"), STATEREF_UNDEFINED, "state1");
		assertMessageEqual(expected, actual);

	}

	/**
	 * VARIANTS_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void variantsUnsupportedProperty_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "  'states':[                                                  \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'VARIANTS':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'foo',                       \n" +
			    "                          'value':'bar'                       \n" +
			    "                       },                                     \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                       \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ],                                        \n" +
                "                    'unsupported': 'unsupported property'     \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/variants[0]/unsupported"), UNSUPPORTED_PROPERTY, "unsupported");
		assertMessageEqual(expected, actual);
	}

	/**
	 * VARIANT_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void variantNotObject_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[45,'foo']                           \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(2, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/variants[0]/"), ELEMENT_NOT_OBJECT, "variants");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/variants[1]/"), ELEMENT_NOT_OBJECT, "variants");
		assertMessageEqual(expected, actual);
	}

	/**
	 * EXPERIENCEREF_MISSING 
	 * @throws Exception
	 */
	@Test
	public void experienceRefMissing_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
//			    "                    'experienceRef':'A',                      \n" +
                "                    'parameters': [                           \n" +
                "                       {                                      \n" +
                "                          'name':'bar',                        \n" +
                "                          'value':'foo'                       \n" +
                "                       }                                      \n" +
                "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants[0]/"), PROPERTY_MISSING, "experienceRef");
		assertMessageEqual(expected, actual);
	}
	
	/**
	 * EXPERIENCEREF_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void experienceRefNotString_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': true,                    \n" +
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants[0]/experienceRef"), PROPERTY_NOT_STRING, "experienceRef");
		assertMessageEqual(expected, actual);
	}

	/**
	 * EXPERIENCEREF_UNDEFINED 
	 * @throws Exception
	 */
	@Test
	public void experienceRefUndefined_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'foo',                   \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants[0]/experienceRef"), 
				EXPERIENCEREF_UNDEFINED, "foo");
		assertMessageEqual(expected, actual);
	}

	/**
	 * EXPERIENCEREF_ISCONTROL
	 * @throws Exception
	 */
	@Test
	public void experienceIsControl_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A'                     \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants[1]/experienceRef"), EXPERIENCEREF_ISCONTROL);
		assertMessageEqual(expected, actual);
	}

	/**
	 * EXPERIENCEREF_PARAMS_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void experiencePathNotString_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'NAME':'test1',                                       \n" +
			    "        'EXPERIENCES':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'NAME':'B',                                     \n" +
			    "              'WEIGHT':50,                                    \n" +
			    "              'ISCONTROL':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
                "                    'parameters': ['foo','bar']               \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
                "                    'parameters': 'foo, bar'                  \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(3, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants[0]/parameters[0]/"), 
				ELEMENT_NOT_OBJECT, "parameters");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants[0]/parameters[1]/"), 
				ELEMENT_NOT_OBJECT, "parameters");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(2);
		expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[1]/variants[0]/parameters/"), 
				PROPERTY_NOT_LIST, "parameters");
		assertMessageEqual(expected, actual);
	}

	/**
	 * EXPERIENCE_NAME_DUPE
	 * @throws Exception
	 */
	@Test
	public void experienceNameDupe_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'TEST',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'foo',                       \n" +
			    "                          'value':'bar'                       \n" +
			    "                       },                                     \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                       \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/experiences[2]/"), DUPE_OBJECT, "B");
		assertMessageEqual(expected, actual);
	}

	/**
	 * CONTROL_EXPERIENCE_MISSING
	 * @throws Exception
	 */
	@Test
	public void controlExperienceMissing_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'TEST',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50                                    \n" +
			    "              //'isControl':true                                \n" + 
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':50                                    \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/"), CONTROL_EXPERIENCE_MISSING, "TEST");
		assertMessageEqual(expected, actual);
	}

	/**
	 * CONTROL_EXPERIENCE_DUPE
	 * @throws Exception
	 */
	@Test
	public void controlExperienceDupe_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'TEST',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/experiences[2]/"), CONTROL_EXPERIENCE_DUPE, "C", "TEST");
		assertMessageEqual(expected, actual);
	}

	/**
	 * VARIANT_DUPE
	 * @throws Exception
	 */
	@Test
	public void variantDupe_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'TEST',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'bar',                        \n" +
			    "                          'value':'foo'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/variations[0]/onStates[0]/variants[1]/"), DUPE_OBJECT, "A");
		assertMessageEqual(expected, actual);
	}

	/**
	 * UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void onStateUnsupportedProperty_Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'Test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': [                           \n" +
			    "                       {                                      \n" +
			    "                          'name':'foo',                       \n" +
			    "                          'value':'bar'                       \n" +
			    "                       }                                      \n" +
			    "                    ]                                         \n" +
			    "                 }                                            \n" +
			    "              ],                                              \n" +
			    "              /*  no longer supported */                     \n" +
			    "              'isNonvariant':false                            \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/"), UNSUPPORTED_PROPERTY, "isNonvariant");
		assertMessageEqual(expected, actual);
	}

	/**
	 * TEST_NAME_INVALID
	 * @throws Exception
	 */
	@Test
	public void testNameInvalid() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'  },                                   \n" +
	    	    "     {  'name':'state2'  }                                    \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'',                                            \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    "     {                                                        \n" +
			    "        'name':'%abc',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    "     {                                                        \n" +
			    "        'name':'a%bc',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    "     {                                                        \n" +
			    "        'name':'_%bc',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    "     {                                                        \n" +
			    "        'name':'7abc',                                        \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                          \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                            \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(5, response.getMessages().size());
		for (int i = 0; i < 5; i++) {
			ParserMessage actual = response.getMessages().get(i);
			ParserMessage expected = new ParserMessageImpl(
					new Location(String.format("/variations[%s]/name", i)), NAME_INVALID);
			assertMessageEqual(expected, actual);
		}
	}

}
