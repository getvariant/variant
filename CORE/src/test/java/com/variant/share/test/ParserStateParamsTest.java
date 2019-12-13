package com.variant.share.test;

import static com.variant.share.schema.parser.error.SemanticError.NAME_MISSING;
import static com.variant.share.schema.parser.error.SemanticError.PROPERTY_NOT_LIST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.share.schema.Schema;
import com.variant.share.schema.State;
import com.variant.share.schema.parser.ParserMessage;
import com.variant.share.schema.parser.ParserMessageImpl;
import com.variant.share.schema.parser.ParserResponse;
import com.variant.share.schema.parser.SchemaParser;
import com.variant.share.schema.parser.error.SemanticError.Location;

/**
 * Test state parameters
 */
public class ParserStateParamsTest extends BaseTestCore {


	/**
	 * 
	 */
	@Test
	public void noParamsTest() {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1'                                       \n" +
			    "     }                                                        \n" +
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
		
		assertFalse(response.hasMessages());
		assertEquals(0, response.getMessages().size());
		
		Schema schema = response.getSchema();
		assertFalse(schema.getState("state1").get().getParameters().isPresent());

	}

	/**
	 */
	@Test
	public void notListTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
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
		ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/parameters/"), PROPERTY_NOT_LIST, "parameters");
		assertMessageEqual(expected, actual);
	}

	/**
	 */
	@Test
	public void emptyListTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
	    	    "        'parameters': []                                      \n" +
			    "     }                                                        \n" +
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

		assertFalse(response.hasMessages());
		assertEquals(0, response.getMessages().size());

		Schema schema = response.getSchema();
		State s1 = schema.getState("state1").get();
		assertTrue(s1.getParameters().isPresent());
		assertTrue(s1.getParameters().get().isEmpty());

	}

	/**
	 */
	@Test
	public void noNameTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
	    	    "        'parameters': [                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'foo',                                   \n" +
			    "              'value':'bar'                                   \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
//			    "              'name':'foo',                                   \n" +
			    "              'value':'bar'                                   \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
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
		ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/parameters[1]/"), NAME_MISSING);
		assertMessageEqual(expected, actual);
	}

	/**
	 */
	@Test
	public void noValueTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
	    	    "        'parameters': [                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'foo1'                                    \n" +
//			    "              'value':'bar'                                   \n" + // default is null
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'foo2',                                   \n" +
			    "              'value': null                                   \n" + // explicit null
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
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
		assertFalse(response.hasMessages());
		assertEquals(0, response.getMessages().size());
		
		Schema schema = response.getSchema();
		State s1 = schema.getState("state1").get();
		assertTrue(s1.getParameters().get().containsKey("foo1"));
		assertNull(s1.getParameters().get().get("foo1"));
		assertTrue(s1.getParameters().get().containsKey("foo2"));
		assertNull(s1.getParameters().get().get("foo2"));
	}
	
	/**
	 */
	@Test
	public void nameCaseTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
	    	    "        'parameters': [                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'foo1',                                   \n" +
			    "              'value':'bar1'                                   \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'nAmE':'foo2',                                   \n" +
			    "              'value': 'bar2'                                 \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
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

		assertFalse(response.hasMessages());
		assertEquals(0, response.getMessages().size());
		
		Schema schema = response.getSchema();
		State s1 = schema.getState("state1").get();
		assertEquals("bar1", s1.getParameters().get().get("foo1"));
		assertEquals("bar2", s1.getParameters().get().get("foo2"));
		assertNull(s1.getParameters().get().get("Foo1"));

	}

	/**
	 */
	@Test
	public void valueCaseTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
	    	    "        'parameters': [                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'foo1',                                   \n" +
			    "              'value':'bar1'                                   \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'foo2',                                   \n" +
			    "              'vAlUe': 'bar2'                                 \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
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

		assertFalse(response.hasMessages());
		assertEquals(0, response.getMessages().size());
		
		Schema schema = response.getSchema();
		State s1 = schema.getState("state1").get();
		assertEquals("bar1", s1.getParameters().get().get("foo1"));
		assertEquals("bar2", s1.getParameters().get().get("foo2"));
		assertNull(s1.getParameters().get().get("Foo1"));

	}
}
