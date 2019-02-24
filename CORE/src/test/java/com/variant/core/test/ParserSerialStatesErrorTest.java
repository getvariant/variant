package com.variant.core.test;

import static com.variant.core.schema.parser.error.SemanticError.DUPE_OBJECT;
import static com.variant.core.schema.parser.error.SemanticError.NAME_INVALID;
import static com.variant.core.schema.parser.error.SemanticError.NAME_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_EMPTY_LIST;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_LIST;
import static com.variant.core.schema.parser.error.SemanticError.STATEREF_UNDEFINED;
import static com.variant.core.schema.parser.error.SemanticError.UNSUPPORTED_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.error.UserError.Severity;
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
public class ParserSerialStatesErrorTest extends BaseTestCore {
	
	/**
	 * STATES_CLAUSE_NOT_LIST, STATEREF_UNDEFINED
	 * 
	 * @throws Exception
	 */
	@Test
	public void statesClauseNotList_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "  'states':'state1, state2',                                  \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'Test1',                                       \n" +
			    "        'isOn': false,                                       \n" +
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
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(2, response.getMessages().size());

		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states"), PROPERTY_NOT_LIST, "states");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/stateRef"), STATEREF_UNDEFINED, "state1");
		assertMessageEqual(expected, actual);
	}

	/**
	 * NO_STATES_CLAUSE + STATEREF_INVALID
	 * @throws Exception
	 */
	@Test
	public void noViewsClause_stateRefInvalid_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(2, response.getMessages().size());

		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/"), PROPERTY_MISSING, "states");
		assertMessageEqual(expected, actual);

		actual = response.getMessages().get(1);
	    expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/stateRef"), STATEREF_UNDEFINED, "state1");
		assertMessageEqual(expected, actual);
	}

	/**
	 * NO_STATES + STATEREF_UNDEFINED
	 * @throws Exception
	 */
	@Test
	public void noViews_stateRefInfalid_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                  \n" +		    	    
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(2, response.getMessages().size());

		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states/"), PROPERTY_EMPTY_LIST, "states");
		assertMessageEqual(expected, actual);

		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/stateRef"), STATEREF_UNDEFINED, "state1");
		assertMessageEqual(expected, actual);

	}

	/**
	 * STATE_NAME_MISSING
	 * @throws Exception
	 */
	@Test
	public void stateNameMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                  \n" +
			    "     {  'name':'state1'                                       \n" +
			    "     },                                                       \n" +
	    	    "     {                                                        \n" +
//			    "        'name':'state2'                                        \n" +
			    "     }                                                        \n" +
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
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states[1]/"), NAME_MISSING);
		assertMessageEqual(expected, actual);
	}

	/**
	 * STATE_NAME_INVALID
	 * @throws Exception
	 */
	@Test
	public void viewNameNotString_Test() throws Exception {
		
		String config = 
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
			    "     },                                                        \n" +
			    "     {  'name':[1,2]                                           \n" + 
			    "     }                                                        \n" +
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
		ParserResponse response = parser.parse(config);
	
		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states[2]/name"), NAME_INVALID);
		assertMessageEqual(expected, actual);
	}

	/**
	 * STATE_NAME_DUPE
	 * @throws Exception
	 */
	@Test
	public void stateNameDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                  \n" +
			    "     {  'name':'state1'                                        \n" +
			    "     },                                                       \n" +
	    	    "     {                                                        \n" +
			    "        'name':'state1'                                       \n" +
			    "     }                                                        \n" +
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
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());

		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states[1]/"), DUPE_OBJECT, "state1");
		assertMessageEqual(expected, actual);
	}

	/**
	 * UNSUPPORTED_CLAUSE, STATE_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void unsupportedClause_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     {  'name':'state1'                                       \n" +
			    "     },                                                       \n" +
	    	    "     {                                                       \n" +
			    "        'name':'state2',                                      \n" +
			    "        'invalid property':'throw an error'                   \n" +
			    "     }                                                        \n" +
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
			    "  ],                                                          \n" +
				"  'invalid clause': 'throw an error'                          \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(2, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(
				new Location("/invalid clause"), UNSUPPORTED_PROPERTY, "invalid clause");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(
				new Location("/states[1]/invalid property"), UNSUPPORTED_PROPERTY, "invalid property");
		assertMessageEqual(expected, actual);
	}

	/**
	 * STATE_NAME_INVALID
	 * @throws Exception
	 */
	@Test
	public void stateNameInvalid() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
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
		ParserResponse response = parser.parse(config);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertTrue(response.hasMessages(Severity.INFO));
		assertEquals(5, response.getMessages().size());
		for (int i = 0; i < 5; i++) {
			ParserMessage actual = response.getMessages().get(i);
			ParserMessage expected = new ParserMessageImpl(
					new Location(String.format("/states[%s]/name", i)), 
					NAME_INVALID);
			assertMessageEqual(expected, actual);
		}
	}

	/**
	 * PARAMS_NOT_LIST, PARAM_NOT_OBJECT
	 * 
	 * @throws Exception
	 */
	@Test
	public void paramsNotList_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
	    	    "        'parameters':                                         \n" +
			    "           {                                                  \n" +
			    "              'name':'foo',                                   \n" +
			    "              'value':'bar'                                   \n" +
			    "           }                                                  \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'Test1',                                       \n" +
			    "        'isOn': false,                                       \n" +
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
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(1, response.getMessages().size());

		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/parameters/"), PROPERTY_NOT_LIST, "parameters");
		assertMessageEqual(expected, actual);

	}

	/**
	 * STATES_CLAUSE_NOT_LIST, STATEREF_UNDEFINED
	 * 
	 * @throws Exception
	 */
	@Test
	public void params_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "  'states':                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'state1'                                       \n" +
			    "     }                                                        \n" +
			    "   ,                                                          \n" +
				"  'variations':[                                              \n" +
			    "     {                                                        \n" +
			    "        'name':'Test1',                                       \n" +
			    "        'isOn': false,                                       \n" +
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
		ParserResponse response = parser.parse(config);

		assertTrue(response.hasMessages());
		assertEquals(2, response.getMessages().size());

		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/states"), PROPERTY_NOT_LIST, "states");
		assertMessageEqual(expected, actual);

		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(new Location("/variations[0]/onStates[0]/stateRef"), STATEREF_UNDEFINED, "state1");
		assertMessageEqual(expected, actual);

	}

}
