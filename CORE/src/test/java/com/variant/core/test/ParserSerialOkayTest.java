package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.variant.core.schema.Flusher;
import com.variant.core.schema.Hook;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.SchemaParser;


public class ParserSerialOkayTest extends BaseTestCore {
	
	/**
	 * All tests are off, no state params
	 */
	@org.junit.Test
	public void allTestsOffTest() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
					    "  'meta':{                                                                \n" +		    	    
					    "     'name':'allTestsOffTest',                                            \n" +
					    "     'comment':'!@#$%^&*',                                                \n" +
					    "     'hooks':[                                                            \n" +
					    "        {'name':'one', 'class':'c.v.s.one'},                              \n" +
					    "        {'name':'two', 'class':'c.v.s.two'},                              \n" +
					    "        {'name':'anotherOne', 'class':'c.v.s.one'},                       \n" +
					    "        {'name':'three', 'class':'c.v.s.three'},                          \n" +
					    "        {'name':'four', 'class':'c.v.s.four'},                            \n" +
					    "        {'name':'anotherFour', 'class':'c.v.s.four'}                      \n" +
					    "      ],                                                                  \n" +
					    "      'flusher': {                                                        \n" +
					    "        'class':'flusher.class.Foo',                                      \n" +  
					    "        'init':{'url':'jdbc:postgresql://localhost/variant\','user':'variant','password': 'variant'} \n" +
					    "       }                                                                  \n" +
 		    		    "  },                                                                      \n" +
			    	    //==========================================================================//
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'NAME':'state2'},                                                   \n" +
			    	    "     {'NaMe':'state3'}                                                    \n" +
			            "  ],                                                                      \n" +
			            
			    	    //=========================================================================//
			    	    
				        "  'variations':[                                                              \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test1',                                                  \n" +
			    	    "        'isOn':true,                                                     \n" +
					    "        'hooks':[                                                        \n" +
					    "           {                                                             \n" +
					    "              'name':'one',                                              \n" +
					    "              'class':'c.v.s.one',                                       \n" +
					    "              'init':'just a string with \"quotes\\''                    \n" +
					    "           },                                                            \n" +
					    "           {                                                             \n" +
					    "              'name':'two',                                              \n" +
					    "              'class':'c.v.s.two',                                       \n" +
					    "              'init':90                                                  \n" +
					    "           },                                                            \n" +
					    "           {                                                             \n" +
					    "              'name':'three',                                            \n" +
					    "              'class':'c.v.s.three',                                     \n" +
					    "              'init':''                                                  \n" +
					    "           },                                                            \n" +
					    "           {                                                             \n" +
					    "              'name':'four',                                             \n" +
					    "              'class':'c.v.s.four',                                      \n" +
					    "              'init':{'foo':'a string','bar':[1,2],'obj':{}}             \n" +
					    "           }                                                             \n" +
					    "        ],                                                               \n" +
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
						"                    'parameters': [                                      \n" +
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
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test2.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                        \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test2.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
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
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNotNull(response.getSchemaSrc());
		Schema schema = response.getSchema();
		assertNotNull(response.getSchemaSrc());
		assertEquals("allTestsOffTest", schema.getMeta().getName());
		assertEquals("!@#$%^&*", schema.getMeta().getComment());
		Flusher flusher = schema.getMeta().getFlusher();
		assertNotNull(flusher);
		assertEquals("flusher.class.Foo", flusher.getClassName());
		assertEquals("{\"url\":\"jdbc:postgresql://localhost/variant\",\"user\":\"variant\",\"password\":\"variant\"}", flusher.getInit());

		Variation test1 = schema.getVariation("test1").get();
		Variation test2 = schema.getVariation("test2").get();
		assertTrue(test1.isOn());
		assertFalse(test2.isOn());
		assertFalse(test1.isSerialWith(test2));
		assertFalse(test2.isSerialWith(test1));
		assertTrue(test1.isConcurrentWith(test2));
		assertTrue(test2.isConcurrentWith(test1));
		assertFalse(test1.isConjointWith(test2));
		assertFalse(test2.isConjointWith(test1));

		List<Hook> hooks = schema.getMeta().getHooks();
		assertEquals(6, hooks.size());
		Hook hook = hooks.get(0);
		assertEquals("one", hook.getName());
		assertEquals("c.v.s.one", hook.getClassName());
		assertNull(hook.getInit());
		hook = hooks.get(1);
		assertEquals("two", hook.getName());
		assertEquals("c.v.s.two", hook.getClassName());
		assertNull(hook.getInit());
		hook = hooks.get(2);
		assertEquals("anotherOne", hook.getName());
		assertEquals("c.v.s.one", hook.getClassName());
		assertNull(hook.getInit());
		hook = hooks.get(3);
		assertEquals("three", hook.getName());
		assertEquals("c.v.s.three", hook.getClassName());
		assertNull(hook.getInit());
		hook = hooks.get(4);
		assertEquals("four", hook.getName());
		assertEquals("c.v.s.four", hook.getClassName());
		assertNull(hook.getInit());
		hook = hooks.get(5);
		assertEquals("anotherFour", hook.getName());
		assertEquals("c.v.s.four", hook.getClassName());
		assertNull(hook.getInit());

		hooks = test1.getHooks();
		assertEquals(4, hooks.size());
		hook = hooks.get(0);
		assertEquals("one", hook.getName());
		assertEquals("c.v.s.one", hook.getClassName());
		assertEquals("\"just a string with \\\"quotes'\"", hook.getInit());
		hook = hooks.get(1);
		assertEquals("two", hook.getName());
		assertEquals("c.v.s.two", hook.getClassName());
		assertEquals("90", hook.getInit());
		hook = hooks.get(2);
		assertEquals("three", hook.getName());
		assertEquals("c.v.s.three", hook.getClassName());
		assertEquals("\"\"", hook.getInit());
		hook = hooks.get(3);
		assertEquals("four", hook.getName());
		assertEquals("c.v.s.four", hook.getClassName());
		assertEquals("{\"foo\":\"a string\",\"bar\":[1,2],\"obj\":{}}", hook.getInit());

		assertTrue(test2.getHooks().isEmpty());
	}

	/**
	 * One test is off, one disqualified.
	 */
	@org.junit.Test
	public void oneOffOneDisqualifiedTest() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
					    "  'meta':{                                                                \n" +		    	    
					    "      'NAME':'oneOffOneDisqualifiedTest',                                 \n" +
					    "      'CommenT':'oneOffOneDisqualifiedTest comment',                      \n" +
					    "      'flusher': {                                                        \n" +
					    "        'class':'flusher.class.Foo'                                       \n" +  
					    "       }                                                                  \n" +
					    "  },                                                                      \n" +
			    	    //==========================================================================//
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {  'name':'state1',                                                  \n" +
						"         'parameters': [                                                  \n" +
						"            {                                                             \n" +
						"               'name':'path',                                             \n" +
						"               'value':'/path/to/state1'                                  \n" +
						"            }                                                             \n" +
						"        ]                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state2',                                                  \n" +
						"         'parameters': [                                                  \n" +
						"            {                                                             \n" +
						"               'name':'path',                                             \n" +
						"               'value':'/path/to/state2'                                  \n" +
						"            }                                                             \n" +
						"        ]                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state3',                                                  \n" +
						"         'parameters': [                                                  \n" +
						"            {                                                             \n" +
						"               'name':'path',                                             \n" +
						"               'value':'/path/to/state3'                                  \n" +
						"            }                                                             \n" +
						"        ]                                                                 \n" +
			    	    "     }                                                                    \n" +
			            "  ],                                                                      \n" +
			            
			    	    //=========================================================================//
			    	    
				        "  'variations':[                                                         \n" +
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
						"                    'parameters': [                                      \n" +
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
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test2.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test2.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
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
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNotNull(response.getSchemaSrc());
		Schema schema = response.getSchema();
		assertEquals("oneOffOneDisqualifiedTest", schema.getMeta().getName());
		assertEquals("oneOffOneDisqualifiedTest comment", schema.getMeta().getComment());

		Flusher flusher = schema.getMeta().getFlusher();
		assertNotNull(flusher);
		assertEquals("flusher.class.Foo", flusher.getClassName());
		assertNull(flusher.getInit());

		Variation test1 = schema.getVariation("test1").get();
		Variation test2 = schema.getVariation("test2").get();
		assertFalse(test1.isOn());
		assertTrue(test2.isOn());
		assertFalse(test1.isSerialWith(test2));
		assertFalse(test2.isSerialWith(test1));
		assertTrue(test1.isConcurrentWith(test2));
		assertTrue(test2.isConcurrentWith(test1));
		assertFalse(test1.isConjointWith(test2));
		assertFalse(test2.isConjointWith(test1));

	}

	/**
	 * All tests are disqualified.
	 */
	@org.junit.Test
	public void allTestsDisqualifiedTest() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
					    "  'meta':{                                                                \n" +		    	    
					    "      'name':'allTestsDisqualifiedTest'                                   \n" +
					    "  },                                                                      \n" +
			    	    //==========================================================================//
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {  'name':'state1',                                                  \n" +
						"         'parameters': [                                                  \n" +
						"            {                                                             \n" +
						"               'name':'path',                                             \n" +
						"               'value':'/path/to/state1'                                  \n" +
						"            }                                                             \n" +
						"        ]                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state2',                                                  \n" +
						"         'parameters': [                                                  \n" +
						"            {                                                             \n" +
						"               'name':'path',                                             \n" +
						"               'value':'/path/to/state2'                                  \n" +
						"            }                                                             \n" +
						"        ]                                                                 \n" +
			    	    "     },                                                                   \n" +
			    	    "     {  'NAME':'state3',                                                  \n" +
						"         'parameters': [                                                  \n" +
						"            {                                                             \n" +
						"               'name':'path',                                             \n" +
						"               'value':'/path/to/state3'                                  \n" +
						"            }                                                             \n" +
						"        ]                                                                 \n" +
			    	    "     }                                                                    \n" +
			            "  ],                                                                      \n" +
			            
			    	    //=========================================================================//
			    	    
				        "  'variations':[                                                         \n" +
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
						"                    'parameters': [                                      \n" +
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
			    	    "        'onStates':[                                                     \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state1/test2.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'isNonvariant':false,                                      \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters': [                                      \n" +
						"                       {                                                 \n" +
						"                          'name':'path',                                 \n" +
						"                          'value':'/path/to/state2/test2.D'              \n" +
						"                       }                                                 \n" +
						"                    ]                                                    \n" +
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
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNotNull(response.getSchemaSrc());
		Schema schema = response.getSchema();
		assertEquals("allTestsDisqualifiedTest", schema.getMeta().getName());
		assertNull(schema.getMeta().getComment());
		assertNull(schema.getMeta().getFlusher());

		Variation test1 = schema.getVariation("test1").get();
		Variation test2 = schema.getVariation("test2").get();
		assertTrue(test1.isOn());
		assertTrue(test2.isOn());
		assertFalse(test1.isSerialWith(test2));
		assertFalse(test2.isSerialWith(test1));
		assertTrue(test1.isConcurrentWith(test2));
		assertTrue(test2.isConcurrentWith(test1));
		assertFalse(test1.isConjointWith(test2));
		assertFalse(test2.isConjointWith(test1));
	}
		
	// Happy path schema is used by other tests too.
	public static final String SCHEMA = 

	"{                                                                                 \n" +
		    "  'meta':{                                                                \n" +
		    "      'name':'happy_path_schema',                                         \n" +
		    "     'hooks':[                                                            \n" +
		    "        {'name':'one', 'class':'c.v.s.one'},                              \n" +
		    "        {'name':'two', 'class':'c.v.s.two'},                              \n" +
		    "        {'name':'anotherOne', 'class':'c.v.s.one'},                       \n" +
		    "        {'name':'three', 'class':'c.v.s.three'},                          \n" +
		    "        {'name':'four', 'class':'c.v.s.four'},                            \n" +
		    "        {'name':'anotherFour', 'class':'c.v.s.four'}                      \n" +
		    "      ]                                                                   \n" +
		    "  },                                                                      \n" +
    	    //==========================================================================//
    	   
    	    "   'sTaTeS':[                                                             \n" +
    	    "     {  'name':'state1',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state1'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'NAME':'state2',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state2'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'nAmE':'state3',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state3'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state4',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state4'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state5',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state5'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state6',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state6'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state7',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state7'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state8',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state8'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state9',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state9'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'state10',                                                 \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/state10'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     },                                                                   \n" +
    	    "     {  'name':'State1',                                                  \n" +
			"         'parameters': [                                                  \n" +
			"            {                                                             \n" +
			"               'name':'path',                                             \n" +
			"               'value':'/path/to/State1'                                  \n" +
			"            }                                                             \n" +
			"        ]                                                                 \n" +
    	    "     }                                                                    \n" +
            "  ],                                                                      \n" +
            
    	    //=========================================================================//
    	    
	        "  'variations':[                                                         \n" +
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
			"                    'parameters': [                                      \n" +
			"                       {                                                 \n" +
			"                          'name':'path',                                 \n" +
			"                          'value':'/path/to/state1/test1.B'              \n" +
			"                       }                                                 \n" +
			"                    ]                                                    \n" +
    	    "                 },                                                      \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'C',                                 \n" +
			"                    'parameters': [                                      \n" +
			"                       {                                                 \n" +
			"                          'name':'path',                                 \n" +
			"                          'value':'/path/to/state1/test1.C'              \n" +
			"                       }                                                 \n" +
			"                    ]                                                    \n" +
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
			"                    'parameters': [                                      \n" +
			"                       {                                                 \n" +
			"                          'name':'path',                                 \n" +
			"                          'value':'/path/to/state3/test2.D'              \n" +
			"                       }                                                 \n" +
			"                    ]                                                    \n" +
    	    "                 }                                                       \n" +
    	    "              ]                                                          \n" +
    	    "           },                                                            \n" +
    	    "           {                                                             \n" +
    	    "              'stateRef':'state2',                                        \n" +
    	    "              'isNonvariant':false,                                      \n" +
    	    "              'variants':[                                               \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'D',                                 \n" +
			"                    'parameters': [                                      \n" +
			"                       {                                                 \n" +
			"                          'name':'path',                                 \n" +
			"                          'value':'/path/to/state2/test2.D'              \n" +
			"                       }                                                 \n" +
			"                    ]                                                    \n" +
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
			"                    'parameters': [                                      \n" +
			"                       {                                                 \n" +
			"                          'name':'path',                                 \n" +
			"                          'value':'/path/to/state1/Test1.A'              \n" +
			"                       }                                                 \n" +
			"                    ]                                                    \n" +
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
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = (ParserResponse) parser.parse(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		assertNotNull(response.getSchemaSrc());
		final Schema schema = response.getSchema();
		assertEquals("happy_path_schema", schema.getMeta().getName());
		assertNull(schema.getMeta().getComment());

		//
		// Hooks
		//
		
		List<Hook> hooks = schema.getMeta().getHooks();
		assertEquals(6, hooks.size());
		Hook hook = hooks.get(0);
		assertEquals("one", hook.getName());
		assertEquals("c.v.s.one", hook.getClassName());
		hook = hooks.get(1);
		assertEquals("two", hook.getName());
		assertEquals("c.v.s.two", hook.getClassName());
		hook = hooks.get(2);
		assertEquals("anotherOne", hook.getName());
		assertEquals("c.v.s.one", hook.getClassName());
		hook = hooks.get(3);
		assertEquals("three", hook.getName());
		assertEquals("c.v.s.three", hook.getClassName());
		hook = hooks.get(4);
		assertEquals("four", hook.getName());
		assertEquals("c.v.s.four", hook.getClassName());
		hook = hooks.get(5);
		assertEquals("anotherFour", hook.getName());
		assertEquals("c.v.s.four", hook.getClassName());

		//
		// States.
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
		
		// Verify states returned as a list.
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
		
		// Verify states returned individually by name.
		for (String[] expectedView: expectedStates) {	
			verifyState(expectedView, schema.getState(expectedView[0]).get());
		}

		// Verify non-existent views.
		assertFalse(schema.getState("non-existent'").isPresent());		

		for (String[] expectedView: expectedStates) {
			assertFalse(schema.getState(expectedView[0].toUpperCase()).isPresent());
		}
		
		// Instrumented tests.
		State view = schema.getState("state1").get();
		ArrayList<Variation> expectedInstrumentedTests = new ArrayList<Variation>() {{
			add(schema.getVariation("test1").get());
			add(schema.getVariation("Test1").get());
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedVariations());
		assertFalse(view.isNonvariantIn(schema.getVariation("test1").get()));
		assertFalse(view.isNonvariantIn(schema.getVariation("Test1").get()));
		try {
			assertFalse(view.isNonvariantIn(schema.getVariation("non-existent").get()));
		}
		catch (NoSuchElementException nse) { /* expected */ }

		view = schema.getState("state2").get();
		expectedInstrumentedTests = new ArrayList<Variation>() {{
			add(schema.getVariation("test2").get());
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedVariations());
		assertFalse(view.isNonvariantIn(schema.getVariation("test2").get()));
		
		view = schema.getState("state3").get();
		expectedInstrumentedTests = new ArrayList<Variation>() {{
			add(schema.getVariation("test2").get());
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedVariations());
		assertFalse(view.isNonvariantIn(schema.getVariation("test2").get()));

		view = schema.getState("state4").get();
		expectedInstrumentedTests = new ArrayList<Variation>() {{
			add(schema.getVariation("test2").get());
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedVariations());
		assertTrue(view.isNonvariantIn(schema.getVariation("test2").get()));
		
		view = schema.getState("state5").get();
		expectedInstrumentedTests = new ArrayList<Variation>();
		assertEquals(expectedInstrumentedTests, view.getInstrumentedVariations());

		//
		// Tests.
		//

		List<Variation> actualTests = schema.getVariations();
		
		assertEquals(3, actualTests.size());
		verifyTest1(actualTests.get(0), schema);
		verifyTest1(schema.getVariation("test1").get(), schema);
		verifyTest2(actualTests.get(1), schema);
		verifyTest2(schema.getVariation("test2").get(), schema);
		verifyTest3(actualTests.get(2), schema);
		verifyTest3(schema.getVariation("Test1").get(), schema);
		
		assertFalse(schema.getVariation("Test2").isPresent());
	
	}
	
	/**
	 * 
	 * @param expectedState
	 * @param actualState
	 */
	private static void verifyState(String[] expectedState, State actualState) {
		assertNotNull(actualState);
		assertEquals(expectedState[0], actualState.getName());
		assertEquals(expectedState[1], actualState.getParameters().get("path"));		
		assertNotEquals(expectedState[1], actualState.getParameters().get("Path"));		
	}
	
	/**
	 * 
	 * @param test
	 */
	private static void verifyTest1(Variation test, Schema config) {
		
		assertNotNull(test);
		assertEquals("test1", test.getName());
		assertTrue(test.isOn());
		
		// Experiences
		List<Variation.Experience> actualExperiences = test.getExperiences();
		assertEquals(3, actualExperiences.size());
		Variation.Experience exp = actualExperiences.get(0);
		assertEquals("A", exp.getName());
		assertEquals(10, exp.getWeight().doubleValue(), 0.000001);
		assertTrue(exp.isControl());
		assertEquals(exp, test.getControlExperience());
		assertEquals(test, exp.getVariation());
		exp = actualExperiences.get(1);
		assertEquals("B", exp.getName());
		assertEquals(20, exp.getWeight().doubleValue(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getVariation());
		exp = actualExperiences.get(2);
		assertEquals("C", exp.getName());
		assertEquals(30, exp.getWeight().doubleValue(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getVariation());
		
		// onStates
		List<Variation.OnState> actualonStates = test.getOnStates();
		assertEquals(1, actualonStates.size());

		Variation.OnState tov = actualonStates.get(0);
		assertEquals(test, tov.getVariation());
		assertEquals(config.getState("state1").get(), tov.getState());
		assertFalse(tov.isNonvariant());
		List<StateVariant> actualVariants =  tov.getVariants();
		assertEquals(2, actualVariants.size());
		StateVariant variant = actualVariants.get(0);
		assertEquals(test.getExperience("B").get(), variant.getExperience());
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state1/test1.B", variant.getParameters().get("path"));
		variant = actualVariants.get(1);
		assertEquals(test.getExperience("C").get(), variant.getExperience());
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state1/test1.C", variant.getParameters().get("path"));
		
		
	}
	
	/**
	 * 
	 * @param test
	 */
	private static void verifyTest2(Variation test, Schema config) {

		assertNotNull(test);
		assertEquals("test2", test.getName());
		assertFalse(test.isOn());
		
		// Experiences
		List<Variation.Experience> actualExperiences = test.getExperiences();
		assertEquals(2, actualExperiences.size());
		Variation.Experience exp = actualExperiences.get(0);
		assertEquals("C", exp.getName());
		assertEquals(0.5, exp.getWeight().doubleValue(), 0.000001);
		assertTrue(exp.isControl());
		assertEquals(exp, test.getControlExperience());
		assertEquals(test, exp.getVariation());
		exp = actualExperiences.get(1);
		assertEquals("D", exp.getName());
		assertEquals(0.6, exp.getWeight().doubleValue(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getVariation());
		
		// onStates
		List<Variation.OnState> actualonStates = test.getOnStates();
		assertEquals(3, actualonStates.size());

		Variation.OnState tov = actualonStates.get(0);
		assertEquals(test, tov.getVariation());
		assertEquals(config.getState("state3").get(), tov.getState());
		assertFalse(tov.isNonvariant());
		List<StateVariant> actualVariants =  tov.getVariants();
		assertEquals(1, actualVariants.size());
		StateVariant variant = actualVariants.get(0);
		assertEquals(test.getExperience("D").get(), variant.getExperience());
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state3/test2.D", variant.getParameters().get("path"));

		tov = actualonStates.get(1);
		assertEquals(test, tov.getVariation());
		assertEquals(config.getState("state2").get(), tov.getState());
		assertFalse(tov.isNonvariant());
		actualVariants =  tov.getVariants();
		assertEquals(1, actualVariants.size());
		variant = actualVariants.get(0);
		assertEquals(test.getExperience("D").get(), variant.getExperience());
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state2/test2.D", variant.getParameters().get("path"));
		
		tov = actualonStates.get(2);
		assertEquals(test, tov.getVariation());
		assertEquals(config.getState("state4").get(), tov.getState());
		assertTrue(tov.isNonvariant());
		assertTrue(tov.getVariants().isEmpty());

	}

	/**
	 * 
	 * @param test
	 */
	private static void verifyTest3(Variation test, Schema config) {
		
		assertNotNull(test);
		assertEquals("Test1", test.getName());
		assertTrue(test.isOn());
		
		// Experiences
		List<Variation.Experience> actualExperiences = test.getExperiences();
		assertEquals(2, actualExperiences.size());
		Variation.Experience exp = actualExperiences.get(0);
		assertEquals("A", exp.getName());
		assertEquals(10, exp.getWeight().doubleValue(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getVariation());
		exp = actualExperiences.get(1);
		assertEquals("B", exp.getName());
		assertEquals(20, exp.getWeight().doubleValue(), 0.000001);
		assertTrue(exp.isControl());
		assertEquals(exp, test.getControlExperience());
		assertEquals(test, exp.getVariation());
		
		// onStates
		List<Variation.OnState> actualonStates = test.getOnStates();
		assertEquals(1, actualonStates.size());

		Variation.OnState tov = actualonStates.get(0);
		assertEquals(test, tov.getVariation());
		assertEquals(config.getState("state1").get(), tov.getState());
		assertFalse(tov.isNonvariant());
		List<StateVariant> actualVariants =  tov.getVariants();
		assertEquals(1, actualVariants.size());
		StateVariant variant = actualVariants.get(0);
		assertEquals(test.getExperience("A").get(), variant.getExperience());
		assertNull(variant.getConjointExperiences());
		assertTrue(variant.isProper());
		assertEquals("/path/to/state1/Test1.A", variant.getParameters().get("path"));
		
	}

}

