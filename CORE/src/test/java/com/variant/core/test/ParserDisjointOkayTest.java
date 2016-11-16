package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.variant.core.impl.UserHooker;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.core.schema.parser.SchemaParser;


public class ParserDisjointOkayTest extends BaseTestCore {
	
	/**
	 * All tests are off, no state params
	 */
	@org.junit.Test
	public void allTestsOffTest() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'NAME':'state2'},                                                   \n" +
			    	    "     {'NaMe':'state3'}                                                    \n" +
			            "  ],                                                                      \n" +
			            
			    	    //=========================================================================//
			    	    
				        "  'tests':[                                                              \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test1',                                                  \n" +
			    	    "        'isOn':false,                                                    \n" +
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
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'isOn': false,                                                   \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'C',                                                \n" +
			    	    "              'weight':0.5,                                              \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'D',                                                \n" +
			    	    "              'weight':0.6                                               \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                        \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state2/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                        \n" +
			    	    "              'isNonvariant':true                                        \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponseImpl response = (ParserResponseImpl) parser.parse(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		Schema schema = response.getSchema();
		Test test1 = schema.getTest("test1");
		Test test2 = schema.getTest("test2");
		assertFalse(test1.isOn());
		assertFalse(test2.isOn());
		assertFalse(test1.isSerialWith(test2));
		assertFalse(test2.isSerialWith(test1));
		assertTrue(test1.isConcurrentWith(test2));
		assertTrue(test2.isConcurrentWith(test1));
		assertFalse(test1.isCovariantWith(test2));
		assertFalse(test2.isCovariantWith(test1));

	}

	/**
	 * One test is off, one disqualified.
	 */
	@org.junit.Test
	public void oneOffOneDisqualifiedTest() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {  'name':'state1',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state1'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state2',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state2'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state3',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state3'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     }                                                                    \n" +
			            "  ],                                                                      \n" +
			            
			    	    //=========================================================================//
			    	    
				        "  'tests':[                                                              \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test1',                                                  \n" +
			    	    "        'isOn':false,                                                    \n" +
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
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'isOn': true,                                                    \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'C',                                                \n" +
			    	    "              'weight':0.5,                                              \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'D',                                                \n" +
			    	    "              'weight':0.6                                               \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state2/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                       \n" +
			    	    "              'isNonvariant':true                                        \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponseImpl response = (ParserResponseImpl) parser.parse(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		Schema schema = response.getSchema();
		Test test1 = schema.getTest("test1");
		Test test2 = schema.getTest("test2");
		assertFalse(test1.isOn());
		assertTrue(test2.isOn());
		assertFalse(test1.isSerialWith(test2));
		assertFalse(test2.isSerialWith(test1));
		assertTrue(test1.isConcurrentWith(test2));
		assertTrue(test2.isConcurrentWith(test1));
		assertFalse(test1.isCovariantWith(test2));
		assertFalse(test2.isCovariantWith(test1));

	}

	/**
	 * All tests are disqualified.
	 */
	@org.junit.Test
	public void allTestsDisqualifiedTest() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {  'name':'state1',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state1'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state2',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state2'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state3',                                                  \n" +
						"        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state3'                                       \n" +
			    	    "        }                                                                 \n" +
			    	    "     }                                                                    \n" +
			            "  ],                                                                      \n" +
			            
			    	    //=========================================================================//
			    	    
				        "  'tests':[                                                              \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test1',                                                  \n" +
			    	    "        'isOn':true ,                                                    \n" +
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
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'isOn': true,                                                    \n" +
			    	    "        'experiences':[                                                  \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'C',                                                \n" +
			    	    "              'weight':0.5,                                              \n" +
			    	    "              'isControl':true                                           \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'name':'D',                                                \n" +
			    	    "              'weight':0.6                                               \n" +
			    	    "           }                                                             \n" +
			    	    "        ],                                                               \n" +
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                        \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                        \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state2/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                        \n" +
			    	    "              'isNonvariant':true                                        \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponseImpl response = (ParserResponseImpl) parser.parse(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		Schema schema = response.getSchema();
		Test test1 = schema.getTest("test1");
		Test test2 = schema.getTest("test2");
		assertTrue(test1.isOn());
		assertTrue(test2.isOn());
		assertFalse(test1.isSerialWith(test2));
		assertFalse(test2.isSerialWith(test1));
		assertTrue(test1.isConcurrentWith(test2));
		assertTrue(test2.isConcurrentWith(test1));
		assertFalse(test1.isCovariantWith(test2));
		assertFalse(test2.isCovariantWith(test1));
	}
		
	// Happy path schema is used by other tests too.
	public static final String SCHEMA = 

	"{                                                                                 \n" +
    	    //==========================================================================//
    	   
    	    "   'sTaTeS':[                                                             \n" +
    	    "     {  'name':'state1',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'path':'/path/to/state1'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'NAME':'state2',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'Path':'/path/to/state2'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'nAmE':'state3',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'PATH':'/path/to/state3'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state4',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'path':'/path/to/state4'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state5',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'path':'/path/to/state5'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state6',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'path':'/path/to/state6'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state7',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'path':'/path/to/state7'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state8',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'path':'/path/to/state8'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state9',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'path':'/path/to/state9'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state10',                                                 \n" +
			"        'parameters':{                                                    \n" +
    	    "           'path':'/path/to/state10'                                      \n" +
    	    "        }                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'State1',                                                  \n" +
			"        'parameters':{                                                    \n" +
    	    "           'path':'/path/to/State1'                                       \n" +
    	    "        }                                                                 \n" +
    	    "     }                                                                    \n" +
            "  ],                                                                      \n" +
            
    	    //=========================================================================//
    	    
	        "  'TeSts':[                                                              \n" +
    	    "     {                                                                   \n" +
    	    "        'name':'test1',                                                  \n" +
    	    "        'isOn':true,                                                     \n" +
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
    	    "              'weight':30                                                \n" +
    	    "           }                                                             \n" +
    	    "        ],                                                               \n" +
    	    "        'onStates':[                                                     \n" +
    	    "           {                                                             \n" +
    	    "              'stateRef':'state1',                                       \n" +
    	    "              'variants':[                                               \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'B',                                 \n" +
			"                    'parameters':{                                       \n" +
    	    "                       'path':'/path/to/state1/test1.B'                  \n" +
    	    "                    }                                                    \n" +
    	    "                 },                                                      \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'C',                                 \n" +
			"                    'parameters':{                                       \n" +
    	    "                       'path':'/path/to/state1/test1.C'                  \n" +
    	    "                    }                                                    \n" +
    	    "                 }                                                       \n" +
    	    "              ]                                                          \n" +
    	    "           }                                                             \n" +
    	    "        ]                                                                \n" +
    	    "     },                                                                  \n" +
    	    //--------------------------------------------------------------------------//	
    	    "     {                                                                   \n" +
    	    "        'name':'test2',                                                  \n" +
    	    "        'isOn': false,                                                   \n" +
    	    "        'experiences':[                                                  \n" +
    	    "           {                                                             \n" +
    	    "              'name':'C',                                                \n" +
    	    "              'weight':0.5,                                              \n" + 
    	    "              'isControl':true                                           \n" +
    	    "           },                                                            \n" +
    	    "           {                                                             \n" +
    	    "              'name':'D',                                                \n" +
    	    "              'weight':0.6                                               \n" +
    	    "           }                                                             \n" +
    	    "        ],                                                               \n" +
    	    "        'onStates':[                                                      \n" +
    	    "           {                                                             \n" +
    	    "              'stateRef':'state3',                                        \n" +
    	    "              'variants':[                                               \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'D',                                 \n" +
			"                    'parameters':{                                       \n" +
    	    "                       'path':'/path/to/state3/test2.D'                  \n" +
    	    "                    }                                                    \n" +
    	    "                 }                                                       \n" +
    	    "              ]                                                          \n" +
    	    "           },                                                            \n" +
    	    "           {                                                             \n" +
    	    "              'stateRef':'state2',                                        \n" +
    	    "              'isNonvariant':false,                                      \n" +
    	    "              'variants':[                                               \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'D',                                 \n" +
			"                    'parameters':{                                       \n" +
    	    "                       'path':'/path/to/state2/test2.D'                  \n" +
    	    "                    }                                                    \n" +
    	    "                 }                                                       \n" +
    	    "              ]                                                          \n" +
    	    "           },                                                            \n" +
    	    "           {                                                             \n" +
    	    "              'stateRef':'state4',                                        \n" +
    	    "              'isNonvariant':true                                        \n" +
    	    "           }                                                             \n" +
    	    "        ]                                                                \n" +
    	    "     },                                                                  \n" +
    	    //--------------------------------------------------------------------------//	
    	    "     {                                                                   \n" +
    	    "        'name':'Test1',                                                  \n" +
    	    "        'experiences':[                                                  \n" +
    	    "           {                                                             \n" +
    	    "              'name':'A',                                                \n" +
    	    "              'weight':10,                                               \n" +
    	    "              'isControl':false                                          \n" +
    	    "           },                                                            \n" +
    	    "           {                                                             \n" +
    	    "              'isControl':true,                                          \n" +
    	    "              'name':'B',                                                \n" +
    	    "              'weight':20                                                \n" +
    	    "           }                                                             \n" +
    	    "        ],                                                               \n" +
    	    "        'onStates':[                                                     \n" +
    	    "           {                                                             \n" +
    	    "              'stateRef':'state1',                                       \n" +
    	    "              'variants':[                                               \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'A',                                 \n" +
			"                    'parameters':{                                       \n" +
    	    "                       'path':'/path/to/state1/Test1.A'                  \n" +
    	    "                    }                                                    \n" +
    	    "                 }                                                       \n" +
    	    "              ]                                                          \n" +
    	    "           }                                                             \n" +
    	    "        ]                                                                \n" +
    	    "     }                                                                   \n" +
    	    "  ]                                                                      \n" +
    	    "}                                                                         ";


	/**
	 * Happy path.
	 */
	@SuppressWarnings("serial")
	@org.junit.Test
	public void happyPathTest() throws Exception {
		
		SchemaParser parser = new SchemaParser(new UserHooker());
		ParserResponseImpl response = (ParserResponseImpl) parser.parse(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		final Schema schema = response.getSchema();

		//
		// Views.
		//
		
		String[][] expectedStates = {
				{"state1", "/path/to/state1"},
				{"state2", "/path/to/state2"},
				{"state3", "/path/to/state3"},
				{"state4", "/path/to/state4"},
				{"state5", "/path/to/state5"},
				{"state6", "/path/to/state6"},
				{"state7", "/path/to/state7"},
				{"state8", "/path/to/state8"},
				{"state9", "/path/to/state9"},
				{"state10", "/path/to/state10"},
				{"State1",  "/path/to/State1"}
						
		};
		
		// Verify views returned as a list.
		List<State> actualStates = schema.getStates();
		assertEquals(expectedStates.length, actualStates.size());
		verifyState(expectedStates[0], actualStates.get(0));
		verifyState(expectedStates[1], actualStates.get(1));
		verifyState(expectedStates[2], actualStates.get(2));
		verifyState(expectedStates[3], actualStates.get(3));
		verifyState(expectedStates[4], actualStates.get(4));
		verifyState(expectedStates[5], actualStates.get(5));
		verifyState(expectedStates[6], actualStates.get(6));
		verifyState(expectedStates[7], actualStates.get(7));
		verifyState(expectedStates[8], actualStates.get(8));
		verifyState(expectedStates[9], actualStates.get(9));
		verifyState(expectedStates[10], actualStates.get(10));
		
		// Verify views returned individually by name.
		for (String[] expectedView: expectedStates) {	
			verifyState(expectedView, schema.getState(expectedView[0]));
		}

		// Verify non-existent views.
		assertNull(schema.getState("non-existent'"));		

		for (String[] expectedView: expectedStates) {
			assertNull(schema.getState(expectedView[0].toUpperCase()));
		}
		
		// Instrumented tests.
		State view = schema.getState("state1");
		ArrayList<Test> expectedInstrumentedTests = new ArrayList<Test>() {{
			add(schema.getTest("test1"));
			add(schema.getTest("Test1"));
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());
		assertFalse(view.isNonvariantIn(schema.getTest("test1")));
		assertFalse(view.isNonvariantIn(schema.getTest("Test1")));
		try {
			assertFalse(view.isNonvariantIn(schema.getTest("non-existent")));
		}
		catch (NullPointerException npe ) { /* expected */ }

		view = schema.getState("state2");
		expectedInstrumentedTests = new ArrayList<Test>() {{
			add(schema.getTest("test2"));
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());
		assertFalse(view.isNonvariantIn(schema.getTest("test2")));
		
		view = schema.getState("state3");
		expectedInstrumentedTests = new ArrayList<Test>() {{
			add(schema.getTest("test2"));
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());
		assertFalse(view.isNonvariantIn(schema.getTest("test2")));

		view = schema.getState("state4");
		expectedInstrumentedTests = new ArrayList<Test>() {{
			add(schema.getTest("test2"));
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());
		assertTrue(view.isNonvariantIn(schema.getTest("test2")));
		
		view = schema.getState("state5");
		expectedInstrumentedTests = new ArrayList<Test>();
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());

		//
		// Tests.
		//

		List<Test> actualTests = schema.getTests();
		
		assertEquals(3, actualTests.size());
		verifyTest1(actualTests.get(0), schema);
		verifyTest1(schema.getTest("test1"), schema);
		verifyTest2(actualTests.get(1), schema);
		verifyTest2(schema.getTest("test2"), schema);
		verifyTest3(actualTests.get(2), schema);
		verifyTest3(schema.getTest("Test1"), schema);
		
		assertNull(schema.getTest("Test2"));
	
	}
	
	/**
	 * 
	 * @param expectedState
	 * @param actualState
	 */
	private static void verifyState(String[] expectedState, State actualState) {
		assertNotNull(actualState);
		assertEquals(expectedState[0], actualState.getName());
		assertEquals(expectedState[1], actualState.getParameter("path"));		
		assertEquals(expectedState[1], actualState.getParameter("Path"));		
	}
	
	/**
	 * 
	 * @param test
	 */
	private static void verifyTest1(Test test, Schema config) {
		
		assertNotNull(test);
		assertEquals("test1", test.getName());
		assertTrue(test.isOn());
		
		// Experiences
		List<Test.Experience> actualExperiences = test.getExperiences();
		assertEquals(3, actualExperiences.size());
		Test.Experience exp = actualExperiences.get(0);
		assertEquals("A", exp.getName());
		assertEquals(10, exp.getWeight().doubleValue(), 0.000001);
		assertTrue(exp.isControl());
		assertEquals(exp, test.getControlExperience());
		assertEquals(test, exp.getTest());
		exp = actualExperiences.get(1);
		assertEquals("B", exp.getName());
		assertEquals(20, exp.getWeight().doubleValue(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getTest());
		exp = actualExperiences.get(2);
		assertEquals("C", exp.getName());
		assertEquals(30, exp.getWeight().doubleValue(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getTest());
		
		// onStates
		List<Test.OnState> actualonStates = test.getOnStates();
		assertEquals(1, actualonStates.size());

		Test.OnState tov = actualonStates.get(0);
		assertEquals(test, tov.getTest());
		assertEquals(config.getState("state1"), tov.getState());
		assertFalse(tov.isNonvariant());
		List<StateVariant> actualVariants =  tov.getVariants();
		assertEquals(2, actualVariants.size());
		StateVariant variant = actualVariants.get(0);
		assertEquals(test.getExperience("B"), variant.getExperience());
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state1/test1.B", variant.getParameter("path"));
		variant = actualVariants.get(1);
		assertEquals(test.getExperience("C"), variant.getExperience());
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state1/test1.C", variant.getParameter("path"));
		
		
	}
	
	/**
	 * 
	 * @param test
	 */
	private static void verifyTest2(Test test, Schema config) {

		assertNotNull(test);
		assertEquals("test2", test.getName());
		assertFalse(test.isOn());
		
		// Experiences
		List<Test.Experience> actualExperiences = test.getExperiences();
		assertEquals(2, actualExperiences.size());
		Test.Experience exp = actualExperiences.get(0);
		assertEquals("C", exp.getName());
		assertEquals(0.5, exp.getWeight().doubleValue(), 0.000001);
		assertTrue(exp.isControl());
		assertEquals(exp, test.getControlExperience());
		assertEquals(test, exp.getTest());
		exp = actualExperiences.get(1);
		assertEquals("D", exp.getName());
		assertEquals(0.6, exp.getWeight().doubleValue(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getTest());
		
		// onStates
		List<Test.OnState> actualonStates = test.getOnStates();
		assertEquals(3, actualonStates.size());

		Test.OnState tov = actualonStates.get(0);
		assertEquals(test, tov.getTest());
		assertEquals(config.getState("state3"), tov.getState());
		assertFalse(tov.isNonvariant());
		List<StateVariant> actualVariants =  tov.getVariants();
		assertEquals(1, actualVariants.size());
		StateVariant variant = actualVariants.get(0);
		assertEquals(test.getExperience("D"), variant.getExperience());
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state3/test2.D", variant.getParameter("path"));

		tov = actualonStates.get(1);
		assertEquals(test, tov.getTest());
		assertEquals(config.getState("state2"), tov.getState());
		assertFalse(tov.isNonvariant());
		actualVariants =  tov.getVariants();
		assertEquals(1, actualVariants.size());
		variant = actualVariants.get(0);
		assertEquals(test.getExperience("D"), variant.getExperience());
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state2/test2.D", variant.getParameter("path"));
		
		tov = actualonStates.get(2);
		assertEquals(test, tov.getTest());
		assertEquals(config.getState("state4"), tov.getState());
		assertTrue(tov.isNonvariant());
		assertTrue(tov.getVariants().isEmpty());

	}

	/**
	 * 
	 * @param test
	 */
	private static void verifyTest3(Test test, Schema config) {
		
		assertNotNull(test);
		assertEquals("Test1", test.getName());
		assertTrue(test.isOn());
		
		// Experiences
		List<Test.Experience> actualExperiences = test.getExperiences();
		assertEquals(2, actualExperiences.size());
		Test.Experience exp = actualExperiences.get(0);
		assertEquals("A", exp.getName());
		assertEquals(10, exp.getWeight().doubleValue(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getTest());
		exp = actualExperiences.get(1);
		assertEquals("B", exp.getName());
		assertEquals(20, exp.getWeight().doubleValue(), 0.000001);
		assertTrue(exp.isControl());
		assertEquals(exp, test.getControlExperience());
		assertEquals(test, exp.getTest());
		
		// onStates
		List<Test.OnState> actualonStates = test.getOnStates();
		assertEquals(1, actualonStates.size());

		Test.OnState tov = actualonStates.get(0);
		assertEquals(test, tov.getTest());
		assertEquals(config.getState("state1"), tov.getState());
		assertFalse(tov.isNonvariant());
		List<StateVariant> actualVariants =  tov.getVariants();
		assertEquals(1, actualVariants.size());
		StateVariant variant = actualVariants.get(0);
		assertEquals(test.getExperience("A"), variant.getExperience());
		assertNull(variant.getCovariantExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state1/Test1.A", variant.getParameter("path"));
		
	}

	
}

