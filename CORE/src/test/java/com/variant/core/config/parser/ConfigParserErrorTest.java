package com.variant.core.config.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Parse time exceptions
 * @author Igor
 *
 */
public class ConfigParserErrorTest extends ConfigParserBaseTest {

	/**
	 * JSON_PARSE
	 * @throws Exception
	 */
	@Test
	public void jsonParse_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +		    	    
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
		
		assertTrue(response.hasErrors());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.JSON_PARSE, "Unexpected character (''' (code 39)): was expecting comma to separate OBJECT entries").getMessage(), error.getMessage());		
		assertEquals(7, error.getLine().intValue());
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
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(2, response.getErrors().size());

		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.NO_VIEWS_CLAUSE).getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.INFO, error.getSeverity());

		error = response.getErrors().get(1);
		assertEquals(new ParserError(ParserErrorTemplate.NO_TESTS_CLAUSE).getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.INFO, error.getSeverity());

	}

	/**
	 * NO_VIEWS_CLAUSE + VIEWREF_INVALID
	 * @throws Exception
	 */
	@Test
	public void noViewsClause_ViewRefInvalid_Test() throws Exception {
		
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(2, response.getErrors().size());

		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.NO_VIEWS_CLAUSE).getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.INFO, error.getSeverity());

		error = response.getErrors().get(1);
		assertEquals(new ParserError(ParserErrorTemplate.VIEWREF_UNDEFINED, "view1", "Test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}


	/**
	 * NO_VIEWS + VIEWREF_INVALID
	 * @throws Exception
	 */
	@Test
	public void noViews_ViewRefInfalid_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
			    "   'views':[                                                  \n" +		    	    
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(2, response.getErrors().size());

		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.NO_VIEWS).getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.INFO, error.getSeverity());

		error = response.getErrors().get(1);
		assertEquals(new ParserError(ParserErrorTemplate.VIEWREF_UNDEFINED, "view1", "Test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());

	}
	/**
	 * VIEW_NAME_MISSING
	 * @throws Exception
	 */
	@Test
	public void viewNameMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1'                               \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
		
		assertTrue(response.hasErrors());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VIEW_NAME_MISSING).getMessage(), error.getMessage());
	}

	/**
	 * VIEW_NAME_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void viewNameNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':[1,2],                                         \n" + 
			    "        'path':'/path/to/view3'                               \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VIEW_NAME_NOT_STRING).getMessage(), error.getMessage());
	}

	/**
	 * VIEW_NAME_DUPE
	 * @throws Exception
	 */
	@Test
	public void viewNameDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view1',                                       \n" + 
			    "        'path':'/path/to/view2'                               \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());

		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VIEW_NAME_DUPE, "view1").getMessage(), error.getMessage());
	}

	/**
	 * VIEW_PATH_MISSING
	 * @throws Exception
	 */
	@Test
	public void viewPathMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3'                                        \n" + 
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VIEW_PATH_MISSING, "view3").getMessage(), error.getMessage());
	}

	/**
	 * NO_TESTS_CLAUSE
	 * @throws Exception
	 */
	@Test
	public void noTestsClause_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
			    "   'viEWs':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     }                                                        \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
		
		assertTrue(response.hasErrors());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.NO_TESTS_CLAUSE).getMessage(), error.getMessage());
	}

	/**
	 * NO_TESTS
	 * @throws Exception
	 */
	@Test
	public void noTests_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +			    	   
			    "   'viEWs':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
		
		assertTrue(response.hasErrors());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.NO_TESTS).getMessage(), error.getMessage());
	}

	/**
	 * UNSUPPORTED_CLAUSE, VIEW_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void unsupportedClause_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2',                                       \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ],                                                          \n" +
				"  'invalid clause': 'throw an error'                          \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.WARN, response.highestSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.UNSUPPORTED_CLAUSE, "invalid clause").getMessage(), error.getMessage());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(ParserErrorTemplate.VIEW_UNSUPPORTED_PROPERTY, "invalid property", "view2").getMessage(), error.getMessage());
	}

	/**
	 * TEST_NAME_MISSING
	 * @throws Exception
	 */
	@Test
	public void testNameMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                       \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.TEST_NAME_MISSING).getMessage(), error.getMessage());
	}

	/**
	 * TEST_NAME_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void testNameNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.TEST_NAME_NOT_STRING).getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}
	
	/**
	 * TEST_NAME_DUPE
	 * @throws Exception
	 */
	@Test
	public void testNameDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'VIEWS':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
		
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.TEST_NAME_DUPE, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * TEST_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void testUnsupportedProperty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
	
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.WARN, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.TEST_UNSUPPORTED_PROPERTY, "unsupported", "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.WARN, error.getSeverity());
	}

	/**
	 * EXPERIENCES_NOT_LIST + EXPERIENCEREF_UNDEFINED
	 * @throws Exception
	 */
	@Test
	public void experienceNotList_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'tests':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':{'foo':'bar'},                          \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCES_NOT_LIST, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCEREF_UNDEFINED, "A", "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}


	/**
	 * EXPERIENCES_LIST_EMPTY + + EXPERIENCEREF_UNDEFINED
	 * @throws Exception
	 */
	@Test
	public void experiencesListEmpty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCES_LIST_EMPTY, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCEREF_UNDEFINED, "A", "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCE_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void experiencesNotObject_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           []                                                 \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCE_NOT_OBJECT, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCE_NAME_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void experienceNameNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':234,                                     \n" +
			    "              'weight':50,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
	
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCE_NAME_NOT_STRING, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * ISCONTROL_NOT_BOOLEAN
	 * @throws Exception
	 */
	@Test
	public void isControlNotBoolean_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
	
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.ISCONTROL_NOT_BOOLEAN, "test1", "A").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * WEIGHT_NOT_NUMBER
	 * @throws Exception
	 */
	@Test
	public void weightNotNumber_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     }                                                        \n" +
			    "  ],                                                          \n" +
				"  'TESTS':[                                                   \n" +
			    "     {                                                        \n" +
			    "        'name':'test1',                                       \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':50                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':'50',                                  \n" +
			    "              'isControl':true                                \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.WEIGHT_NOT_NUMBER, "test1", "B").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCE_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void experienceUnsupportedProperty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
	
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.WARN, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCE_UNSUPPORTED_PROPERTY, "unsupported", "test1", "A").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.WARN, error.getSeverity());
	}

	/**
	 * ONVIEWS_NOT_LIST
	 * @throws Exception
	 */
	@Test
	public void onViewsNotList_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':                                            \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "                                                              \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
	
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.ONVIEWS_NOT_LIST, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * ONVIEWS_LIST_EMPTY
	 * @throws Exception
	 */
	@Test
	public void onViewsListEmpty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS': []                                         \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
	
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.ONVIEWS_LIST_EMPTY, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}
	
	/**
	 * ONVIEW_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void onViewNotObject_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[45]                                        \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
	
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.ONVIEW_NOT_OBJECT, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * VIEWREF_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void viewrefNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':3456789,                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
	
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VIEWREF_NOT_STRING, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * VIEWREF_MISSING
	 * @throws Exception
	 */
	@Test
	public void viewrefMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VIEWREF_MISSING, "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * VIEWREF_UNDEFINED
	 * @throws Exception
	 */
	@Test
	public void viewrefUndefined_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'View1',                              \n" +
			    "              'isInvariant': false,                           \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VIEWREF_UNDEFINED, "View1", "test1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * ISINVARIANT_NOT_BOOLEAN
	 * @throws Exception
	 */
	@Test
	public void isInvariantNotBoolean_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'ISInvariant': 'false',                         \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.ISINVARIANT_NOT_BOOLEAN, "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANTS_NOT_LIST
	 * @throws Exception
	 */
	@Test
	public void variantsNotList_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'ISInvariant': false,                           \n" +
			    "              'variants':                                     \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "                                                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VARIANTS_NOT_LIST, "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}


	/**
	 * VARIANTS_LIST_EMPTY
	 * @throws Exception
	 */
	@Test
	public void variantsListEmpty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewref':'view1',                              \n" +
			    "              'VARIANTS':[                                    \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VARIANTS_LIST_EMPTY, "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANTS_UNSUPPORTED_PROPERTY
	 * @throws Exception
	 */
	@Test
	public void variantsUnsupportedProperty_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewref':'view1',                              \n" +
			    "              'VARIANTS':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A',          \n" +
                "                    'unsupported': 'unsupported property'     \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VARIANTS_UNSUPPORTED_PROPERTY, "unsupported", "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANTS_ISINVARIANT_INCOMPATIBLE
	 * @throws Exception
	 */
	@Test
	public void variantsIsInvariantIncompatible_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewref':'view1',                              \n" +
			    "               'ISINVARIANT': true,                           \n" +
			    "              'VARIANTS':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'          \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VARIANTS_ISINVARIANT_INCOMPATIBLE, "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANTS_ISINVARIANT_XOR
	 * @throws Exception
	 */
	@Test
	public void variantsIsInvariantXor_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewref':'view1'                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VARIANTS_ISINVARIANT_XOR, "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * VARIANT_NOT_OBJECT
	 * @throws Exception
	 */
	@Test
	public void variantNotObject_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[45,'foo']                           \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.VARIANT_NOT_OBJECT, "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(ParserErrorTemplate.VARIANT_NOT_OBJECT, "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCEREF_MISSING
	 * @throws Exception
	 */
	@Test
	public void experienceRefMissing_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
//			    "                    'experienceRef':'A',                      \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCEREF_MISSING, "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}
	
	/**
	 * EXPERIENCEREF_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void experienceRefNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': true,                    \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCEREF_NOT_STRING, "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCEREF_UNDEFINED
	 * @throws Exception
	 */
	@Test
	public void experienceRefUndefined_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'foo',                   \n" +
			    "                    'path':'/path/to/view1/test1.A'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCEREF_UNDEFINED, "foo", "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCEREF_ISCONTROL
	 * @throws Exception
	 */
	@Test
	public void experienceIsControl_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view1/test1.B'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCEREF_ISCONTROL, "B", "test1", "view1").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

	/**
	 * EXPERIENCEREF_PATH_NOT_STRING
	 * @throws Exception
	 */
	@Test
	public void experiencePathNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'Views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view1',                              \n" +
			    "        'name':'view2'                                        \n" +
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
			    "        'ONVIEWS':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'A',                     \n" +
			    "                    'path':['foo','bar']                      \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = ConfigParser.parse(config);
printErrors(response);
		assertTrue(response.hasErrors());
		assertEquals(ParserError.Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ParserErrorTemplate.EXPERIENCEREF_PATH_NOT_STRING, "test1", "view1", "A").getMessage(), error.getMessage());
		assertEquals(ParserError.Severity.ERROR, error.getSeverity());
	}

}
