package com.variant.share.test;

import static com.variant.share.schema.parser.error.SemanticError.NAME_INVALID;
import static com.variant.share.schema.parser.error.SemanticError.NAME_MISSING;
import static com.variant.share.schema.parser.error.SemanticError.PROPERTY_MISSING;
import static com.variant.share.schema.parser.error.SemanticError.PROPERTY_NOT_OBJECT;
import static com.variant.share.schema.parser.error.SemanticError.PROPERTY_NOT_STRING;
import static com.variant.share.schema.parser.error.SemanticError.UNSUPPORTED_PROPERTY;
import static com.variant.share.schema.parser.error.SyntaxError.JSON_SYNTAX_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.share.error.UserError.Severity;
import com.variant.share.schema.parser.ParserMessage;
import com.variant.share.schema.parser.ParserMessageImpl;
import com.variant.share.schema.parser.ParserResponse;
import com.variant.share.schema.parser.SchemaParser;
import com.variant.share.schema.parser.error.SyntaxError;
import com.variant.share.schema.parser.error.SemanticError.Location;

/**
 * Parse time exceptions
 * @author Igor
 *
 */
public class ParserSerialMetaErrorTest extends BaseTestCore {
	
	/**
	 * JSON_PARSE
	 * @throws Exception
	 */
	@Test
	public void jsonParse_Test() throws Exception {
		
		String schema = 
				"{                                                              \n" +
			    "   'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                  \n" +		    	    
			    "     {  'name':'state1'                                       \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" + // missing comma
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
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new SyntaxError.Location(schema, 10, 4), JSON_SYNTAX_ERROR, "Unexpected character (''' (code 39)): was expecting comma to separate Object entries");
		assertMessageEqual(expected, actual);
	}
	
	/**
	 * NO_STATES_CLAUSE + NO_TESTS_CLAUSE
	 * @throws Exception
	 */
	@Test
	public void noStatesClause_NoTestsClause_Test() throws Exception {
		
		String schema = 
				"{                                                              \n" +			    	   
			    "   'meta':{                                                    \n" +		    	    
			    "      'name':'schema_name',                                    \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  }                                                            \n" +
			    "}                                                              \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertTrue(response.hasMessages());
		assertEquals(2, response.getMessages().size());

		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/"), PROPERTY_MISSING, "states");
		assertMessageEqual(expected, actual);
		actual = response.getMessages().get(1);
		expected = new ParserMessageImpl(new Location("/"), PROPERTY_MISSING, "variations");
		assertMessageEqual(expected, actual);
	}

	/**
	 * NO_META_CLAUSE
	 * @throws Exception
	 */
	@Test
	public void metaMissingTest() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/"), PROPERTY_MISSING, "meta");
		assertMessageEqual(expected, actual);
	}

	/**
	 * META_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void metaNotObjectTest() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta': 'blah',                                             \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/meta/"), PROPERTY_NOT_OBJECT, "meta");
		assertMessageEqual(expected, actual);
	}

	/**
	 * META_NAME_INVALID 
	 * @throws Exception
	 */
	@Test
	public void metaNameInvalid1Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':{},                                              \n" +
			    "      'comment':'schema comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/meta/name"), NAME_INVALID);
		assertMessageEqual(expected, actual);
	}

	/**
	 * META_NAME_INVALID
	 * @throws Exception
	 */
	@Test
	public void metaNameInvalid2Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'bad&name',                                      \n" +
			    "      'comment':'schema$comment'                               \n" +
			    "  },                                                           \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/meta/name"), NAME_INVALID);
		assertMessageEqual(expected, actual);
	}

	/**
	 * META_COMMENT_INVALID
	 * @throws Exception
	 */
	@Test
	public void metaCommentInvalidTest() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'_schema_name2',                                 \n" +
			    "      'comment':{}                                            \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/meta/comment"), PROPERTY_NOT_STRING, "comment");
		assertMessageEqual(expected, actual);
	}

	/**
	 * META_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void metaUnsupportedProperty1Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'name':'_schema_name2',                                 \n" +
			    "      'coment':'a comment *&^'                               \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "        'onStates':[                                           \n" +
			    "           {                                                  \n" +
			    "              'stateRef':'state1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);

		assertFalse(response.hasMessages(Severity.FATAL));
		assertFalse(response.hasMessages(Severity.ERROR));
		assertTrue(response.hasMessages(Severity.WARN));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/meta/coment"), UNSUPPORTED_PROPERTY, "coment");
		assertMessageEqual(expected, actual);
	}

	/**
	 * META_UNSUPPORTED_PROPERTY + META_NAME_MISSING
	 * @throws Exception
	 */
	@Test
	public void metaUnsupportedProperty2Test() throws Exception {
		
		String schema = 
				"{                                                             \n" +
			    "  'meta':{                                                    \n" +		    	    
			    "      'namee':'_schema_name2',                                \n" +
			    "      'comment':'a comment *&^'                               \n" +
			    "  },                                                          \n" +
			    "   'states':[                                                 \n" +
			    "     { 'name':'state1' }                                      \n" +
			    "  ],                                                          \n" +
				"  'variations':[                                              \n" +
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
			    "                    'experienceRef': 'B'                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		SchemaParser parser = getSchemaParser();
		ParserResponse response = parser.parse(schema);
		
		assertFalse(response.hasMessages(Severity.FATAL));
		assertTrue(response.hasMessages(Severity.ERROR));
		assertEquals(1, response.getMessages().size());
		ParserMessage actual = response.getMessages().get(0);
		ParserMessage expected = new ParserMessageImpl(new Location("/meta/"), NAME_MISSING);
		assertMessageEqual(expected, actual);
	}

}
