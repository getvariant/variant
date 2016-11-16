package com.variant.core.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.variant.core.exception.Error.Severity;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.parser.ParserError;
import com.variant.core.schema.parser.ParserMessageImpl;
import com.variant.core.schema.parser.SchemaParser;

/**
 * Parse time exceptions
 * @author Igor
 *
 */
public class ParserSerialErrorTest extends BaseTestCore {
	
	/**
	 * JSON_PARSE
	 * @throws Exception
	 */
	@Test
	public void jsonParse_Test() throws Exception {
		
		String config = 
				"{                                                              \n" +
			    "   'states':[                                                  \n" +		    	    
			    "     {  'name':'state1',                                       \n" +
	    	    "        'parameters': {                                        \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" + // missing comma
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'Test1',                                       \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.JSON_PARSE, "Unexpected character (''' (code 39)): was expecting comma to separate OBJECT entries").getText(), error.getText());		
		assertEquals(Severity.FATAL, error.getSeverity());
		assertEquals(9, error.getLine().intValue());
		assertEquals(4, error.getColumn().intValue());
	}
	
	/**
	 * NO_VIEWS_CLAUSE + NO_TESTS_CLAUSE
	 * @throws Exception
	 */
	@Test
	public void noViewsClause_NoTestsClause_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(2, response.getMessages().size());

		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.NO_STATES_CLAUSE).getText(), error.getText());
		assertEquals(Severity.INFO, error.getSeverity());

		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.NO_TESTS_CLAUSE).getText(), error.getText());
		assertEquals(Severity.INFO, error.getSeverity());

	}

	/**
	 * NO_STATES_CLAUSE + stateRef_INVALID
	 * @throws Exception
	 */
	@Test
	public void noViewsClause_stateRefInvalid_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'Test1',                                       \n" +
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
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(2, response.getMessages().size());

		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.NO_STATES_CLAUSE).getText(), error.getText());
		assertEquals(Severity.INFO, error.getSeverity());

		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.STATEREF_UNDEFINED, "state1", "Test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * NO_STATES + stateRef_INVALID
	 * @throws Exception
	 */
	@Test
	public void noViews_stateRefInfalid_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
			    "   'states':[                                                  \n" +		    	    
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'Test1',                                       \n" +
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
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(2, response.getMessages().size());

		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.NO_STATES).getText(), error.getText());
		assertEquals(Severity.INFO, error.getSeverity());

		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.STATEREF_UNDEFINED, "state1", "Test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}
	/**
	 * VIEW_NAME_MISSING
	 * @throws Exception
	 */
	@Test
	public void viewNameMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                  \n" +
			    "     {  'name':'state1',                                       \n" +
	    	    "        'parameters': {                                        \n" +
			    "           'path':'/path/to/state1'                            \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                        \n" +
			    "           'path':'/path/to/state2'                            \n" +
			    "        }                                                      \n" +
//			    "        'name':'state2'                                        \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
		
		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.STATE_NAME_MISSING).getText(), error.getText());
	}

	/**
	 * VIEW_NAME_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void viewNameNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                  \n" +
			    "     {  'name':'state1',                                       \n" +
	    	    "        'parameters': {                                        \n" +
			    "           'path':'/path/to/state1'                            \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                        \n" +
			    "           'path':'/path/to/state2'                            \n" +
			    "        },                                                     \n" +
			    "        'name':'state2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':[1,2],                                         \n" + 
	    	    "        'parameters': {                                        \n" +
			    "           'path':'/path/to/state3'                           \n" +
			    "        }                                                     \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.STATE_NAME_INVALID).getText(), error.getText());
	}
	
	/**
	 * PARSER_TEST_ISON_NOT_BOOLEAN
	 * @throws Exception
	 */
	@Test
	public void testIsOnNotBoolean_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.TEST_ISON_NOT_BOOLEAN, "Test1").getText(), error.getText());
	}

	/**
	 * STATE_NAME_DUPE
	 * @throws Exception
	 */
	@Test
	public void viewNameDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state1'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());

		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.STATE_NAME_DUPE, "state1").getText(), error.getText());
	}

	/**
	 * NO_TESTS_CLAUSE
	 * @throws Exception
	 */
	@Test
	public void noTestsClause_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
		
		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.NO_TESTS_CLAUSE).getText(), error.getText());
	}

	/**
	 * NO_TESTS
	 * @throws Exception
	 */
	@Test
	public void noTests_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
		
		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.NO_TESTS).getText(), error.getText());
	}

	/**
	 * UNSUPPORTED_CLAUSE, VIEW_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void unsupportedClause_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2',                                      \n" +
			    "        'invalid property':'throw an error'                   \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ],                                                          \n" +
				"  'invalid clause': 'throw an error'                          \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.UNSUPPORTED_CLAUSE, "invalid clause").getText(), error.getText());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.STATE_UNSUPPORTED_PROPERTY, "invalid property", "state2").getText(), error.getText());
	}

	/**
	 * TEST_NAME_MISSING
	 * @throws Exception
	 */
	@Test
	public void testNameMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.TEST_NAME_MISSING).getText(), error.getText());
	}

	/**
	 * TEST_NAME_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void testNameNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.TEST_NAME_INVALID).getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}
	
	/**
	 * TEST_NAME_DUPE
	 * @throws Exception
	 */
	@Test
	public void testNameDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'Tests':[                                                   \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
		
		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.TEST_NAME_DUPE, "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * TEST_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void testUnsupportedProperty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
	
		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.TEST_UNSUPPORTED_PROPERTY, "unsupported", "test1").getText(), error.getText());
		assertEquals(Severity.WARN, error.getSeverity());
	}

	/**
	 * EXPERIENCES_NOT_LIST
	 * @throws Exception
	 */
	@Test
	public void experienceNotList_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':{'foo':'bar'},                          \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCES_NOT_LIST, "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}


	/**
	 * EXPERIENCES_LIST_EMPTY + PARSER_IS_CONTROL_MISSING + EXPERIENCEREF_UNDEFINED
	 * @throws Exception
	 */
	@Test
	public void experiencesListEmpty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "        ],                                                    \n" +
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCES_LIST_EMPTY, "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCE_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void experiencesNotObject_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "                    'experienceRef':'B',                      \n" +
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.B'           \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCE_NOT_OBJECT, "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCE_NAME_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void experienceNameNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(6, response.getMessages().size());
		for (int i = 0; i < 6; i++) {
			ParserMessage error = response.getMessages().get(i);
			assertEquals(new ParserMessageImpl(ParserError.EXPERIENCE_NAME_INVALID, "test1").getText(), error.getText());
			assertEquals(Severity.ERROR, error.getSeverity());
		}
	}

	/**
	 * ISCONTROL_NOT_BOOLEAN
	 * @throws Exception
	 */
	@Test
	public void isControlNotBoolean_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
	
		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.ISCONTROL_NOT_BOOLEAN, "test1", "A").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * WEIGHT_NOT_NUMBER + 
	 * @throws Exception
	 */
	@Test
	public void weightNotNumber_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.WEIGHT_NOT_NUMBER, "test1", "A").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCE_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void experienceUnsupportedProperty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
	
		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCE_UNSUPPORTED_PROPERTY, "unsupported", "test1", "A").getText(), error.getText());
		assertEquals(Severity.WARN, error.getSeverity());
	}

	/**
	 * onStates_NOT_LIST
	 * @throws Exception
	 */
	@Test
	public void onStatesNotList_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "        'onStates':                                            \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "                                                              \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
	
		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.ONSTATES_NOT_LIST, "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * onStates_LIST_EMPTY
	 * @throws Exception
	 */
	@Test
	public void onStatesListEmpty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
	
		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.ONSTATES_LIST_EMPTY, "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}
	
	/**
	 * ONVIEW_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void onViewNotObject_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
	
		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.ONSTATES_NOT_OBJECT, "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * stateRef_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void stateRefNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "              'stateRef':3456789,                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);
	
		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
	assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.STATEREF_NOT_STRING, "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_stateRef_MISSING
	 * @throws Exception
	 */
	@Test
	public void stateRefMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.STATEREF_MISSING, "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_stateRef_DUPE
	 * @throws Exception
	 */
	@Test
	public void stateRefDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.STATEREF_DUPE, "state1", "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * STATEREF_UNDEFINED
	 * @throws Exception
	 */
	@Test
	public void stateRefUndefined_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "              'isNonvariant': false,                           \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.STATEREF_UNDEFINED, "State1", "test1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * ISNONVARIANT_NOT_BOOLEAN
	 * @throws Exception
	 */
	@Test
	public void isNonvariantNotBoolean_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "              'ISNonvariant': 'false',                         \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.ISNONVARIANT_NOT_BOOLEAN, "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANTS_NOT_LIST
	 * @throws Exception
	 */
	@Test
	public void variantsNotList_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "              'ISNonvariant': false,                           \n" +
			    "              'variants':                                     \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "                                                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.VARIANTS_NOT_LIST, "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}


	/**
	 * VARIANTS_LIST_EMPTY
	 * @throws Exception
	 */
	@Test
	public void variantsListEmpty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.VARIANTS_LIST_EMPTY, "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANTS_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void variantsUnsupportedProperty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'          \n" +
			    "                    },                                        \n" +
                "                    'unsupported': 'unsupported property'     \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.VARIANTS_UNSUPPORTED_PROPERTY, "unsupported", "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANTS_ISNONVARIANT_INCOMPATIBLE
	 * @throws Exception
	 */
	@Test
	public void variantsIsNonvariantIncompatible_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "               'ISNONVARIANT': true,                           \n" +
			    "              'VARIANTS':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
	    	    "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'          \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.VARIANTS_ISNONVARIANT_INCOMPATIBLE, "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANTS_ISNONVARIANT_XOR
	 * @throws Exception
	 */
	@Test
	public void variantsIsNonvariantXor_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "              'stateRef':'state1'                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.VARIANTS_ISNONVARIANT_XOR, "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANT_NOT_OBJECT + PARSER_VARIANT_MISSING
	 * @throws Exception
	 */
	@Test
	public void variantNotObject_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(3, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.VARIANT_NOT_OBJECT, "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.VARIANT_NOT_OBJECT, "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(2);
		assertEquals(new ParserMessageImpl(ParserError.VARIANT_MISSING, "A", "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCEREF_MISSING + PARSER_VARIANT_MISSING
	 * @throws Exception
	 */
	@Test
	public void experienceRefMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCEREF_MISSING, "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.VARIANT_MISSING, "A", "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}
	
	/**
	 * EXPERIENCEREF_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void experienceRefNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCEREF_NOT_STRING, "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCEREF_UNDEFINED + PARSER_VARIANT_MISSING
	 * @throws Exception
	 */
	@Test
	public void experienceRefUndefined_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCEREF_UNDEFINED, "foo", "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.VARIANT_MISSING, "A", "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCEREF_ISCONTROL
	 * @throws Exception
	 */
	@Test
	public void experienceIsControl_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.B'           \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCEREF_ISCONTROL, "B", "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCEREF_PARAMS_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void experiencePathNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
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
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCEREF_PARAMS_NOT_OBJECT, "test1", "state1", "A").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_EXPERIENCE_NAME_DUPE + PARSER_VARIANT_MISSING
	 * @throws Exception
	 */
	@Test
	public void experienceNameDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.EXPERIENCE_NAME_DUPE, "B", "TEST").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.VARIANT_MISSING, "B", "TEST", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_CONTROL_EXPERIENCE_DUPE
	 * @throws Exception
	 */
	@Test
	public void controlExperienceDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.CONTROL_EXPERIENCE_DUPE, "C", "TEST").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_VARIANT_DUPE + PARSER_VARIANT_MISSING
	 * @throws Exception
	 */
	@Test
	public void variantDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'           \n" +
			    "                    }                                         \n" +
			    "                 },                                           \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.VARIANT_DUPE, "A", "TEST", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.VARIANT_MISSING, "B", "TEST", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_VARIANT_MISSING
	 * @throws Exception
	 */
	@Test
	public void variantMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'path':'/path/to/state1'                           \n" +
			    "        }                                                     \n" +
			    "     },                                                       \n" +
	    	    "     {  'parameters': {                                       \n" +
			    "           'path':'/path/to/state2'                           \n" +
			    "        },                                                    \n" +
			    "        'name':'state2'                                       \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.VARIANT_MISSING, "B", "TEST", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_STATE_NAME_INVALID
	 * @throws Exception
	 */
	@Test
	public void stateNameInvalid() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {'name':''},                                             \n" +
			    "     {'name':'%abc'},                                         \n" +
			    "     {'name':'a%bc'},                                         \n" +
			    "     {'name':'_%bc'},                                         \n" +
			    "     {'name':'7abc'},                                         \n" +
			    "     {'name':'_aBc9'},                                        \n" +
			    "     {'name':'aBc9'},                                         \n" +
			    "     {'name':'a7_Bc9'},                                       \n" +
			    "     {'name':'_0Bc9'},                                        \n" +
			    "     {'name':'state1'}                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(5, response.getMessages().size());
		for (int i = 0; i < 5; i++) {
			ParserMessage error = response.getMessages().get(i);
			assertEquals(new ParserMessageImpl(ParserError.STATE_NAME_INVALID).getText(), error.getText());
			assertEquals(Severity.ERROR, error.getSeverity());
		}
	}

	/**
	 * PARSER_TEST_NAME_INVALID
	 * @throws Exception
	 */
	@Test
	public void testNameInvalid() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     {'name':'state1'}                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
			    "                    'experienceRef': 'A',                     \n" +
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.A'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponse response = parser.parse(config);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(5, response.getMessages().size());
		for (int i = 0; i < 5; i++) {
			ParserMessage error = response.getMessages().get(i);
			assertEquals(new ParserMessageImpl(ParserError.TEST_NAME_INVALID).getText(), error.getText());
			assertEquals(Severity.ERROR, error.getSeverity());
		}
	}

}
