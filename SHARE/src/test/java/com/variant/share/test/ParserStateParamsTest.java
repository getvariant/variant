package com.variant.share.test;

import static com.variant.share.schema.parser.error.SemanticError.PARAM_NAME_DUPE;
import static com.variant.share.schema.parser.error.SemanticError.PARAM_NAME_INVALID;
import static com.variant.share.schema.parser.error.SemanticError.PARAM_VALUE_INVALID;
import static com.variant.share.schema.parser.error.SemanticError.PROPERTY_NOT_OBJECT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
		
      if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		Schema schema = response.getSchema();
		assertFalse(schema.getState("state1").get().getParameters().isPresent());

	}

	/**
	 */
	@Test
	public void notOblectTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
	    	    "        'parameters':  [                                      \n" +
			    "           {                                                  \n" +
			    "              'name':'value'                                  \n" +
			    "           }                                                  \n" +
             "         ]                                                    \n" +
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
		ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/parameters/"), PROPERTY_NOT_OBJECT, "parameters");
		assertMessageEqual(expected, actual);
	}

	/**
	 */
	@Test
	public void emptyObjectTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
	    	    "        'parameters':{}                                       \n" +
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

      if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = response.getSchema();
		State s1 = schema.getState("state1").get();
		assertTrue(s1.getParameters().isPresent());
		assertTrue(s1.getParameters().get().isEmpty());

	}

	/**
	 */
	@Test
	public void validTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
	    	    "        'parameters': {                                       \n" +
			    "           'foo':'bar',                                       \n" +
             "           'foo2':'bar'                                       \n" +
			    "        }                                                     \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                               \n" +
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

		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

      Schema schema = response.getSchema();
      State s1 = schema.getState("state1").get();
      assertTrue(s1.getParameters().isPresent());
      assertEquals("bar", s1.getParameters().get().get("foo"));
      assertEquals("bar", s1.getParameters().get().get("fOo"));
      assertEquals("bar", s1.getParameters().get().get("foo2"));
      assertEquals("bar", s1.getParameters().get().get("Foo2"));
	}

   /**
    */
   @Test
   public void dupeNameTest() throws Exception {
      
      String config = 
            "{                                                             \n" +
             "  'meta':{                                                    \n" +                
             "      'name':'schema_name',                                   \n" +
             "      'comment':'schema comment'                              \n" +
             "  },                                                          \n" +
             "  'states':[                                                  \n" +
             "     {                                                        \n" +
             "        'name':'state1',                                      \n" +
             "        'parameters': {                                       \n" +
             "           'Name':'name',                                     \n" +
             "           'foo':'foo',                                       \n" +
             "           'nAmE':'foo2',                                     \n" +
             "           'bar':'bar'                                        \n" +
             "        }                                                     \n" +
             "     }                                                        \n" +
             "  ],                                                          \n" +
            "  'variations':[                                               \n" +
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
      ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/parameters/"), PARAM_NAME_DUPE, "nAmE");
      assertMessageEqual(expected, actual);

   }
	
	/**
	 */
	@Test
	public void badNameTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                   \n" +
			    "      'comment':'schema comment'                              \n" +
			    "  },                                                          \n" +
			    "  'states':[                                                  \n" +
			    "     {                                                        \n" +
			    "        'name':'state1',                                      \n" +
             "        'parameters': {                                       \n" +
             "           'Name':'name',                                     \n" +
             "           'foo':'foo',                                       \n" +
             "           '$nAmE':'foo2',                                    \n" +
             "           'bar':'bar'                                        \n" +
             "        }                                                     \n" +
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
      ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/parameters/$nAmE"), PARAM_NAME_INVALID, "$nAmE");
      assertMessageEqual(expected, actual);
	}
	
	/**
    */
   @Test
   public void valueNotStringTest() throws Exception {
      
      String config = 
            "{                                                             \n" +
             "  'meta':{                                                    \n" +                
             "      'name':'schema_name',                                   \n" +
             "      'comment':'schema comment'                              \n" +
             "  },                                                          \n" +
             "  'states':[                                                  \n" +
             "     {                                                        \n" +
             "        'name':'state1',                                      \n" +
             "        'parameters': {                                       \n" +
             "           'bar':45                                           \n" +
             "        }                                                     \n" +
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
      ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/parameters/bar"), PARAM_VALUE_INVALID, "bar");
      assertMessageEqual(expected, actual);
   }
   
   /**
    */
   @Test
   public void valueNotStringTest2() throws Exception {
      
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
             "                    'experienceRef':'A',                       \n" +
             "                    'parameters': {                           \n" +
             "                      'name': ['not', 'a', 'string']          \n" +
             "                     }                                        \n" +
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
      ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/parameters/name"), PARAM_VALUE_INVALID, "name");
      assertMessageEqual(expected, actual);
   }
   
   /**
    */
   @Test
   public void valueNullTest() throws Exception {
      
      String config = 
            "{                                                             \n" +
             "  'meta':{                                                    \n" +                
             "      'name':'schema_name',                                   \n" +
             "      'comment':'schema comment'                              \n" +
             "  },                                                          \n" +
             "  'states':[                                                  \n" +
             "     {                                                        \n" +
             "        'name':'state1',                                      \n" +
             "        'parameters': {                                       \n" +
             "           'bar':null                                         \n" +
             "        }                                                     \n" +
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
      ParserMessage expected = new ParserMessageImpl(new Location("/states[0]/parameters/bar"), PARAM_VALUE_INVALID, "bar");
      assertMessageEqual(expected, actual);
   }
}
