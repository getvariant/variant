package com.variant.core.test;

import static com.variant.core.xdm.impl.MessageTemplate.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.TestQualificationHook;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.ParserMessage.Severity;
import com.variant.core.xdm.Schema;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.State;
import com.variant.core.xdm.StateVariant;
import com.variant.core.xdm.impl.ParserMessageImplFacade;


public class ParserMixedTest extends BaseTestCore {
	
	private VariantCore core = rebootApi();

	/**
	 * Compile time errors.
	 */
	@org.junit.Test
	public void compileTimeErrorsTest() throws Exception {
		
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
						"                    'parameters':{                                       \n" + // not allowed.
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
			    	    "                    'experienceRef':'A',                                 \n" + // control allowed, but B is missing.
			    	    "                    'isDefined':false                                    \n" +
			    	    "                 }                                                       \n" +
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
			    	    "                    'experienceRef':'B',                                 \n" + // not a boolean.
			    	    "                    'isDefined':34                                       \n" +
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
			    	    "                    'isDefined': {'foo':'bar'}                           \n" + // Not a boolean
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
			    	    "                    'experienceRef':'A',                                 \n" + // control allowed.
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
	 * Compile time Okay.
	 */
	@org.junit.Test
	public void compileOkayTest() throws Exception {
		
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
			    	    "     }                                                                   \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";
		
		ParserResponse response = core.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

	}

}

