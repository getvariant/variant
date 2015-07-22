package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import static com.variant.core.error.ErrorTemplate.*;

import com.variant.core.ParserResponse;
import com.variant.core.error.Severity;
import com.variant.core.schema.impl.ParserError;
import com.variant.core.schema.impl.ParserResponseImpl;
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
		
		ParserResponseImpl response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_TESTS_NOT_LIST, "test2").getMessage(), error.getMessage());
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
		
		ParserResponseImpl response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_TESTREF_NOT_STRING, "test2").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(PARSER_COVARIANT_TESTREF_NOT_STRING, "test2").getMessage(), error.getMessage());
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
		
		ParserResponseImpl response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_TESTREF_UNDEFINED, "bad", "test2").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
	}

	/**
	 * PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST + PARSER_COVARIANT_VARIANT_MISSING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceRefsNotList_Test() throws Exception {
		
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view2',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3',                                       \n" +
			    "        'path':'/path/to/view3'                               \n" +
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
			    "              'isInvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': ['test1'],                       \n" +
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
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.B'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.B'   \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs':'notAList',     \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.C'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.C'   \n" +
	    	    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_EXPERIENCEREFS_NOT_LIST, "test2", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test1.B", "test2", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT + PARSER_COVARIANT_VARIANT_MISSING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceRefNotObject_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view2',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3',                                       \n" +
			    "        'path':'/path/to/view3'                               \n" +
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
			    "              'isInvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': ['test1'],                       \n" +
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
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.B'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [23],          \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.B'   \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.C'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.C'   \n" +
	    	    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_EXPERIENCE_REF_NOT_OBJECT, "test2", "view2", "B").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test1.C", "test2", "view2", "B").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING + PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceTestRefNotString_Test() throws Exception {
		
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view2',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3',                                       \n" +
			    "        'path':'/path/to/view3'                               \n" +
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
			    "              'isInvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': ['test1'],                       \n" +
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
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.B'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.B'   \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': {},                      \n" +
	    	    "                          'experienceRef': 34                 \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.C'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.C'   \n" +
	    	    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(config);
		
		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(3, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_EXPERIENCE_TEST_REF_NOT_STRING, "test2", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_NOT_STRING, "test2", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(2);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test1.B", "test2", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceTestRefUndefined_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view2',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3',                                       \n" +
			    "        'path':'/path/to/view3'                               \n" +
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
			    "              'isInvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': ['test1'],                       \n" +
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
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.B'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.B'   \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'bad',                   \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.C'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.C'   \n" +
	    	    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_EXPERIENCE_TEST_REF_UNDEFINED, "bad", "test2", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test1.B", "test2", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}


	/**
	 * PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceExperienceRefUndefined_Test() throws Exception {
		
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view2',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3',                                       \n" +
			    "        'path':'/path/to/view3'                               \n" +
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
			    "              'isInvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
	    	    "        'covariantTestRefs': ['test1'],                       \n" +
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
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.B'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.B'   \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'Bad'              \n" +
	    	    "                       }                                      \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.C'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                        {                                     \n" +
	    	    "                           'testRef': 'test1',                \n" +
	    	    "                           'experienceRef': 'C'               \n" +
	    	    "                        }                                     \n" +
	    	    "                    ],                                        \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.C'   \n" +
	    	    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, "test1", "Bad", "test2", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test1.B", "test2", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantVariantTestNotCovariant_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view2',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3',                                       \n" +
			    "        'path':'/path/to/view3'                               \n" +
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
			    "              'isInvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
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
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
	    	    "                    'path':'/path/to/view2/test2.B'           \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_TEST_NOT_COVARIANT, "test1", "B", "test2", "view2", "B").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_TEST_REF_INVARIANT
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceTestRefInvariant_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view2',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3',                                       \n" +
			    "        'path':'/path/to/view3'                               \n" +
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
			    "              'isInvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
                "        'covariantTestRefs':['test1'],                        \n" +
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
			    "                    'path':'/path/to/view1/test2.B'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    // Invalid because view1 is invariant in test1.
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
	    	    "                    'path':'/path/to/view1/test1.B+test2.B'   \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test2.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.B'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'B',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.C'   \n" +
	    	    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test2.C'           \n" +
			    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.C'   \n" +
	    	    "                 },                                           \n" +
	    	    "                 {                                            \n" +
	    	    "                    'experienceRef':'C',                      \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.C'   \n" +
	    	    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(config);

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_EXPERIENCE_TEST_REF_INVARIANT, "test1", "test2", "view1", "B").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}


	/**
	 * PARSER_COVARIANT_VARIANT_MISSING
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantVariantMissing_Test() throws Exception {
		
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view2',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3',                                       \n" +
			    "        'path':'/path/to/view3'                               \n" +
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
			    "              'isInvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
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
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
/*			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
*/			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test3',                                       \n" +
                "        'covariantTestRefs': ['test1', 'test2'],              \n" +
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
			    "                    'path':'/path/to/view1/test3.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view1/test2.B+test3.B'   \n" +
			    "                 },                                           \n" +
/*			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view1/test2.C+test3.B'   \n" +
			    "                 },                                           \n" +
*/			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test1.C'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view1/test2.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.C+test3.C'   \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test3.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.B+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.C+test3.B'   \n" +
			    "                 },                                           \n" +
/*			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test2.B+test3.B'   \n" +
			    "                 },                                           \n" +
*/			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test2.C+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test2.B+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test2.C+test3.B'   \n" +
			    "                 },                                           \n" +

			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test3.C'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.C+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test2.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test2.C+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test2.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test2.C+test3.C'   \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponseImpl response = SchemaParser.parse(config);
		
		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(3, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_VARIANT_MISSING, "B", "test2", "view2").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test2.C", "test3", "view1", "B").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(2);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test1.B,test2.B", "test3", "view2", "B").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_VARIANT_DUPE
	 * 
	 * @throws Exception
	 */
	//@Test
	public void covariantVariantDupe_Test() throws Exception {
		
		String config = 
				"{                                                             \n" +
			    "   'views':[                                                  \n" +
			    "     {  'name':'view1',                                       \n" +
			    "        'path':'/path/to/view1'                               \n" +
			    "     },                                                       \n" +
			    "     {  'path':'/path/to/view2',                              \n" +
			    "        'name':'view2'                                        \n" +
			    "     },                                                       \n" +
			    "     {  'name':'view3',                                       \n" +
			    "        'path':'/path/to/view3'                               \n" +
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
			    "              'isInvariant':true                              \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test2',                                       \n" +
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
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test1.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test1.C'           \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     },                                                       \n" +
			    //----------------------------------------------------------------//	
			    "     {                                                        \n" +
			    "        'name':'test3',                                       \n" +
                "        'covariantTestRefs': ['test1', 'test2'],              \n" +
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
			    "                    'path':'/path/to/view1/test3.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view1/test2.B+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view1/test2.C+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view1/test1.C'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view1/test2.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.C+test3.C'   \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view2',                              \n" +
			    "              'variants':[                                    \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
			    "                    'path':'/path/to/view2/test3.B'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.B+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.C+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test2.B+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test2.C+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test2.B+test3.B'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'B',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test2.C+test3.B'   \n" +
			    "                 },                                           \n" +

			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
			    "                    'path':'/path/to/view2/test3.C'           \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test2.C+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test2.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.B+test2.C+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'B'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test2.B+test3.C'   \n" +
			    "                 },                                           \n" +
			    "                 {                                            \n" +
			    "                    'experienceRef': 'C',                     \n" +
	    	    "                    'covariantExperienceRefs': [              \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test1',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       },                                     \n" +
	    	    "                       {                                      \n" +
	    	    "                          'testRef': 'test2',                 \n" +
	    	    "                          'experienceRef': 'C'                \n" +
	    	    "                       }                                      \n" +
	    	    "                     ],                                       \n" +
			    "                    'path':'/path/to/view2/test1.C+test2.C+test3.C'   \n" +
			    "                 }                                            \n" +
			    "              ]                                               \n" +
			    "           },                                                 \n" +
			    "           {                                                  \n" +
			    "              'viewRef':'view3',                              \n" +
			    "              'isInvariant':true                              \n" +
			    "           }                                                  \n" +
			    "        ]                                                     \n" +
			    "     }                                                        \n" +
			    //----------------------------------------------------------------//	
			    "  ]                                                           \n" +
			    "}                                                             \n";
		
		ParserResponse response = SchemaParser.parse(config);
printErrors(response);
		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(3, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_EXPERIENCE_EXPERIENCE_REF_UNDEFINED, "test1", "bad", "test2", "view2", "B").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test1", "B", "test2", "view2").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(2);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test1", "C", "test2", "view2").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

}
