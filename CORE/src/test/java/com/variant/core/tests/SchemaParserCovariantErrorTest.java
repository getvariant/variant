package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.error.ErrorTemplate;
import com.variant.core.error.Severity;
import com.variant.core.schema.impl.ParserError;
import com.variant.core.schema.impl.ParserResponse;
import com.variant.core.schema.impl.SchemaParser;

/**
 * Parse time exceptions
 * @author Igor
 *
 */
public class SchemaParserCovariantErrorTest extends BaseTest {
	
	/**
	 * PARSER_COVARIANT_TESTS_NOT_LIST
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantTestsNotList_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
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
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view1/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': 'test1',                         \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view1/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ErrorTemplate.PARSER_COVARIANT_TESTS_NOT_LIST, "test2").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	
	/**
	 * PARSER_COVARIANT_TESTREF_NOT_STRING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantTestrefNotString_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
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
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view1/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': [1,{}],                          \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view1/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ErrorTemplate.PARSER_COVARIANT_TESTREF_NOT_STRING, "test2").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(ErrorTemplate.PARSER_COVARIANT_TESTREF_NOT_STRING, "test2").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
	}
	
	/**
	 * PARSER_COVARIANT_TESTREF_UNDEFINED
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantTestrefUndefined_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
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
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view1/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': ['bad'],                         \n" +
			    "        'experiences':[                                       \n" +
			    "           {                                                  \n" +
			    "              'name':'A',                                     \n" +
			    "              'weight':10,                                    \n" +
			    "              'isControl':true                                \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'B',                                     \n" +
			    "              'weight':20                                     \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'name':'C',                                     \n" +
			    "              'weight':30                                     \n" +
			    "           }                                                  \n" +
			    "        ],                                                    \n" +
			    "        'onViews':[                                           \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view1',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view1/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(ErrorTemplate.PARSER_COVARIANT_TESTREF_UNDEFINED, "bad", "test2").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

}
