package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.parser.ParserError;
import com.variant.core.schema.parser.ParserMessageImpl;
import com.variant.core.schema.parser.SchemaParser;

/**
 * Parse time exceptions related to hooks with schema scope.
 * @author Igor
 *
 */
public class ParserSchemaFlusherErrorTest extends BaseTestCore {
	
	/**
	 * FLUSHER_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void flusherNotObjectTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'_schema_name',                                  \n" +
			    "      'comment':'a comment *&^',                              \n" +
			    "      'flusher':'cannot be a string'                          \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.B'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(config);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.FLUSHER_NOT_OBJECT).getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * FLUSHER_CLASS_NAME_INVALID 
	 * @throws Exception
	 */
	@Test
	public void flusherClassNameInvalidTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'_schema_name',                                  \n" +
			    "      'comment':'a comment *&^',                              \n" +
			    "      'flusher': {                                            \n" +
			    "        'class': []                                           \n" +  
			    "       }                                                      \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.B'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(config);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.FLUSHER_CLASS_NAME_INVALID).getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * FLUSHER_CLASS_NAME_MISSING + FLUSHER_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void flusherClassNameMissingTest() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'_schema_name',                                  \n" +
			    "      'comment':'a comment *&^',                              \n" +
			    "      'flusher': {                                            \n" +
			    "        'invalid':{}                                          \n" +
			    "       }                                                      \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
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
                "                    'parameters': {                           \n" +
			    "                       'path':'/path/to/state1/test1.B'       \n" +
			    "                    }                                         \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(config);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(2, response.getMessages().size());
		ParserMessage error = response.getMessages().get(0);
		assertEquals(new ParserMessageImpl(ParserError.FLUSHER_UNSUPPORTED_PROPERTY, "invalid").getText(), error.getText());
		assertEquals(Severity.WARN, error.getSeverity());
		error = response.getMessages().get(1);
		assertEquals(new ParserMessageImpl(ParserError.FLUSHER_CLASS_NAME_MISSING).getText(), error.getText());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

}
