package com.variant.core.tests;

import static com.variant.core.error.ErrorTemplate.PARSER_COVARIANT_EXPERIENCE_DUPE;
import static com.variant.core.error.ErrorTemplate.PARSER_COVARIANT_VARIANT_DUPE;
import static com.variant.core.error.ErrorTemplate.PARSER_COVARIANT_VARIANT_MISSING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.variant.core.ParserResponse;
import com.variant.core.error.ParserError;
import com.variant.core.error.Severity;
import com.variant.core.schema.impl.SchemaParser;

/**
 * Parse time exceptions
 * @author Igor
 *
 */
public class ParserCovariantErrorMultiTest extends BaseTest {
	
	/**
	 * PARSER_COVARIANT_VARIANT_DUPE
	 * 
	 * @throws Exception
	 */
	@Test
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
			    "                 },                                           \n" +
			    // DUPE:
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
			    "                 }                                           \n" +
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

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(1, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_DUPE, "test1.B, test2.B", "test3", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

	/**
	 * PARSER_COVARIANT_EXPERIENCE_DUPE
	 * 
	 * @throws Exception
	 */
	@Test
	public void covariantExperienceDupe_Test() throws Exception {
		
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
	    	    "                          'testRef': 'test1',                 \n" +  // Should have been test2
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

		assertTrue(response.hasErrors());
		assertEquals(Severity.ERROR, response.highestErrorSeverity());
		assertEquals(2, response.getErrors().size());
		ParserError error = response.getErrors().get(0);
		assertEquals(new ParserError(PARSER_COVARIANT_EXPERIENCE_DUPE, "test1", "B", "test3", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());
		error = response.getErrors().get(1);
		assertEquals(new ParserError(PARSER_COVARIANT_VARIANT_MISSING, "test1.B,test2.B", "test3", "view2", "C").getMessage(), error.getMessage());
		assertEquals(Severity.ERROR, error.getSeverity());

	}

}
