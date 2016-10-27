package com.variant.core.test;

import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_VARIANT_COVARIANT_UNDEFINED;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_VARIANT_MISSING;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_COVARIANT_VARIANT_PROPER_UNDEFINED;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_EXPERIENCEREF_ISCONTROL;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_EXPERIENCEREF_PARAMS_NOT_ALLOWED;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_ISDEFINED_NOT_BOOLEAN;
import static com.variant.core.xdm.impl.MessageTemplate.PARSER_VARIANT_MISSING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import com.variant.core.impl.VariantCore;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.ParserMessage.Severity;
import com.variant.core.schema.ParserResponse;
import com.variant.core.xdm.impl.ParserMessageImplFacade;

/**
 * Test mixed instrumentation, i.e. when an experience is not defined
 * on a particular state, as denoted by the tests/onState/isDefined element.
 *
 * @author Igor 
 *
 */
public class MixedInstrumentationParsetimeTest extends BaseTestCore {
	
	private Random rand = new Random();
	private VariantCore core = rebootApi();

	/**
	 * Parse time errors.
	 */
	@org.junit.Test
	public void errorsTest1() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    //=========================================================================//
			    	    
				        "  'tests':[                                                              \n" +
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
			    	    "                    'isDefined':false,                                   \n" +
						"                    'parameters':{  // not allowed                       \n" + 
			    	    "                       'path':'/path/to/state1/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
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
			    	    "                    'isDefined':false                                    \n" +
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
			    	    "                    'isDefined':34   // not a boolean.                   \n" +
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
			    	    "                    'experienceRef':'A',                                 \n" + 
			    	    "                    'isDefined': {'foo':'bar'} // Not a boolean          \n" + 
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test3.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test5',                                                  \n" +
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
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test4.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);

		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(6, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_EXPERIENCEREF_PARAMS_NOT_ALLOWED, "test1", "state1", "B").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_VARIANT_MISSING, "B", "test1", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(2);
		assertEquals(new ParserMessageImplFacade(PARSER_VARIANT_MISSING, "B", "test2", "state2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(3);
		assertEquals(new ParserMessageImplFacade(PARSER_ISDEFINED_NOT_BOOLEAN, "test3", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(4);
		assertEquals(new ParserMessageImplFacade(PARSER_ISDEFINED_NOT_BOOLEAN, "test4", "state2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(5);
		assertEquals(new ParserMessageImplFacade(PARSER_EXPERIENCEREF_ISCONTROL, "A", "test4", "state2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * More parse time errors.
	 */
	@org.junit.Test
	public void errorsTest2() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    //=========================================================================//			    	    
				        "  'tests':[                                                              \n" +
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
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
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
			    	    "        'covariantTestRefs':['test1'],                                   \n" +
			    	    "        'onStates':[                                                     \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                       \n" +
				        "                 // ERROR: need the def for the local B                  \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                       \n" +
				        "                 // Not an error: don't need hybrid def for T1.B         \n" +
				        "                 // becose proper B is off => the entire slice is too    \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);
		
		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_VARIANT_MISSING, "B", "test2", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * More parse time errors.
	 */
	@org.junit.Test
	public void errorsTest3() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    //=========================================================================//			    	    
				        "  'tests':[                                                              \n" +
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
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
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
			    	    "        'covariantTestRefs':['test1'],                                   \n" +
			    	    "        'onStates':[                                                     \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test2.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
				        "                 // Error: covar A is off => the entire slice is too    \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test1.B-test2.B'          \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
				        "                 // Error: proper B is off => the entire slice is too    \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.B-test2.B'          \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);
		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_COVARIANT_UNDEFINED, "B", "test1.B", "test1.B", "test2", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_PROPER_UNDEFINED, "B", "test1.B", "test2", "state2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}
	
	/**
	 * More parse time errors.
	 */
	@org.junit.Test
	public void errorsTest4() throws Exception {
		
		final String SCHEMA = 

				"{                                                                                 \n" +
			    	    //==========================================================================//
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    //=========================================================================//			    	    
				        "  'tests':[                                                              \n" +
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
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
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
			    	    "        'covariantTestRefs':['test1'],                                   \n" +
			    	    "        'onStates':[                                                     \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test2.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
				        "                 // Error: covar A is off => the entire slice is too    \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test1.B-test2.B'          \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
				        "                 // Error: proper B is off => the entire slice is too    \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.B-test2.B'          \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);
		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_COVARIANT_UNDEFINED, "B", "test1.B", "test1.B", "test2", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_PROPER_UNDEFINED, "B", "test1.B", "test2", "state2").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
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
			    	    //==========================================================================//
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    //=========================================================================//			    	    
				        "  'tests':[                                                              \n" +
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
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.C'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                      \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'covariantTestRefs':['test1'],                                   \n" +
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
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test2.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
/*	This covariant variant is missing.		
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state1/test1.C+test2.B'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
*/
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state1/test1.C+test2.D'           \n" +
					    "                    }                                                    \n" +
					    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);
		assertTrue(response.hasMessages());
		assertEquals(Severity.ERROR, response.highestMessageSeverity());
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImplFacade(PARSER_COVARIANT_VARIANT_MISSING, "B", "test1.C", "test2", "state1").getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());

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
			    	    //==========================================================================//
			    	    "   'states':[                                                             \n" +
			    	    "     {'name':'state1'},                                                   \n" +
			    	    "     {'name':'state2'},                                                   \n" +
			    	    "     {'name':'state3'}                                                    \n" +
			            "  ],                                                                      \n" +
			    	    //=========================================================================//			    	    
				        "  'tests':[                                                              \n" +
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
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.C'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                      \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test1.D'                  \n" +
			    	    "                    }                                                    \n" +
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
			    	    "        'covariantTestRefs':['test1'],                                   \n" +
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
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test2.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state1/test1.C+test2.B'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state1/test2.D'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state1/test1.C+test2.C'           \n" +
					    "                    }                                                    \n" +
					    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test2.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.B+test2.B'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'B',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'D'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.D+test2.B'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test2.C'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'C',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.B+test2.C'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'C',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'D'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.C+test2.D'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state2/test2.C'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.B+test2.D'           \n" +
					    "                    }                                                    \n" +
					    "                 },                                                      \n" +
					    "                 {                                                       \n" +
					    "                    'experienceRef': 'D',                                \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'D'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
						"                    'parameters':{                                       \n" +
					    "                      'path':'/path/to/state2/test1.D+test2.D'           \n" +
					    "                    }                                                    \n" +
					    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'A',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state3/test2.B'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
						"                    'parameters':{                                       \n" + 
			    	    "                       'path':'/path/to/state3/test2.C'                  \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
					    "                 {                                                       \n" +
			    	    "                    'experienceRef':'D',                                 \n" +
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";

		ParserResponse response = core.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
	}
}

