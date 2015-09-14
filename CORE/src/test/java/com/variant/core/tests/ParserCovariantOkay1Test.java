package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantCollectionsUtils;


/**
 * All Tests:
 * Tests States
 *       1 2 3 4 5
 * test1   + + - -
 * test2 - + + +  
 * test3 + + - - -
 * blank: not instrumented, -: nonvariant, +: has variants.
 * 
 * T1, T2(T1), T3
 * 
 * @author Igor
 */
public class ParserCovariantOkay1Test extends BaseTest {
	
	/**
	 * 
	 */
	@org.junit.Test
	public void test() throws Exception {
		
		final String SCHEMA = 

		"{                                                                                \n" +
	    	    //==========================================================================//
	    	   
	    	    "   'states':[                                                            \n" +
	    	    "     {  'name':'state1',                                                 \n" +
	    	    "        'path':'/path/to/state1'                                         \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'NAME':'state2',                                                 \n" +
	    	    "        'path':'/path/to/state2'                                         \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'nAmE':'state3',                                                 \n" +
	    	    "        'path':'/path/to/state3'                                         \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'name':'state4',                                                 \n" +
	    	    "        'path':'/path/to/state4'                                         \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'name':'state5',                                                 \n" +
	    	    "        'path':'/path/to/state5'                                         \n" +
	    	    "     }                                                                   \n" +
	            "  ],                                                                     \n" +
	            
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
	    	    "              'weight':30                                                \n" +
	    	    "           }                                                             \n" +
	    	    "        ],                                                               \n" +
	    	    "        'onViews':[                                                      \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state2',                                        \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/state2/test1.B'                     \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/state2/test1.C'                     \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state3',                                        \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/state3/test1.B'                     \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/state3/test1.C'                     \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state4',                                        \n" +
	    	    "              'isNonvariant':true                                        \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state5',                                        \n" +
	    	    "              'isNonvariant':true                                        \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    //--------------------------------------------------------------------------//	
	    	    "     {                                                                   \n" +
	    	    "        'name':'test2',                                                  \n" +
	    	    "        'covariantTestRefs': ['test1'],                                  \n" +
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
	    	    "        'onViews':[                                                      \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state1',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state2',                                         \n" +
	    	    "              'variants':[                                               \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/state2/test2.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/state2/test1.B+test2.B'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/state2/test1.C+test2.B'              \n" +
	    	    "                 },                                                      \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/state2/test2.C'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/state2/test1.B+test2.C'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/state2/test1.C+test2.C'              \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state3',                                         \n" +
	    	    "              'variants':[                                               \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/state3/test2.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/state3/test1.B+test2.B'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/state3/test1.C+test2.B'              \n" +
	    	    "                 },                                                      \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/state3/test2.C'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/state3/test1.B+test2.C'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/state3/test1.C+test2.C'              \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +

	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state4',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/state4/test2.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/state4/test2.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           }                                                            \n" +
	    	    "        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    //--------------------------------------------------------------------------//	
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
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'name':'C',                                                \n" +
	    	    "              'weight':30                                                \n" +
	    	    "           }                                                             \n" +
	    	    "        ],                                                               \n" +
	    	    "        'onViews':[                                                      \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state1',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/state1/test3.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/state1/test3.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state2',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/state2/test3.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/state2/test3.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state3',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state4',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'state5',                                         \n" +
	    	    "              'isNonvariant':true                                         \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     }                                                                   \n" +
	    	    //--------------------------------------------------------------------------//	
	     	    "  ]                                                                      \n" +
	    	    "}                                                                         ";

		ParserResponse response = engine.parseSchema(SCHEMA);
		if (response.hasMessages()) printErrors(response);
		assertFalse(response.hasMessages());

		Schema schema = engine.getSchema();
		final Test test1 = schema.getTest("test1");
		final Test test2 = schema.getTest("test2");
		final Test test3 = schema.getTest("test3");

		// 
		// Declared test covariance.
		//
		assertEquals(0, test1.getCovariantTests().size());
		assertEquals(VariantCollectionsUtils.list(test1), test2.getCovariantTests());
		assertEquals(0, test3.getCovariantTests().size());

		// 
		// Runtime test covariance.
		//
		//assertEquals(VariantCollectionsUtils.list(test2), VariantRuntime.getCovariantTests(test1));
		//assertEquals(VariantCollectionsUtils.list(test1), VariantRuntime.getCovariantTests(test2));
		//assertEquals(0, VariantRuntime.getCovariantTests(test3).size());

		// 
		// test1 OnView objects
		//
		List<Test.OnState> onViews = test1.getOnStates();
		assertEquals(4, onViews.size());
		
		// state2
		Test.OnState onView = onViews.get(0);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getView("state2"), onView.getState());
		List<Test.OnState.Variant> variants = onView.getVariants();
		assertEquals(2, variants.size());

		Test.OnState.Variant variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/state2/test1.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/state2/test1.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// state3
		onView = onViews.get(1);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getView("state3"), onView.getState());
		variants = onView.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/state3/test1.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/state3/test1.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// state4
		onView = onViews.get(2);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getView("state4"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// state5
		onView = onViews.get(3);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getView("state5"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// 
		// test2 OnView objects
		//
		onViews = test2.getOnStates();
		assertEquals(4, onViews.size());
		
		// state1
		onView = onViews.get(0);
		assertTrue(onView.isNonvariant());
		assertTrue(onView.getVariants().isEmpty());
		assertEquals(schema.getView("state1"), onView.getState());

		// state2
		onView = onViews.get(1);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getView("state2"), onView.getState());
		variants = onView.getVariants();
		assertEquals(6, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state2/test2.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test2.B", variant.getPath());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test2.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state2/test2.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.B+test2.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state2/test1.C+test2.C", variant.getPath());

		// state3
		onView = onViews.get(2);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getView("state3"), onView.getState());
		variants = onView.getVariants();
		assertEquals(6, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state3/test2.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state3/test1.B+test2.B", variant.getPath());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state3/test1.C+test2.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state3/test2.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state3/test1.B+test2.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/state3/test1.C+test2.C", variant.getPath());

		// state4
		onView = onViews.get(3);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getView("state4"), onView.getState());
		variants = onView.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/state4/test2.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/state4/test2.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// 
		// test3 OnView objects
		//
		onViews = test3.getOnStates();
		assertEquals(5, onViews.size());
		
		// state1
		onView = onViews.get(0);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getView("state1"), onView.getState());
		variants = onView.getVariants();
		assertEquals(2, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/state1/test3.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/state1/test3.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// state2
		onView = onViews.get(1);
		assertFalse(onView.isNonvariant());
		assertEquals(schema.getView("state2"), onView.getState());
		variants = onView.getVariants();
		assertEquals(2, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/state2/test3.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/state2/test3.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// state3
		onView = onViews.get(2);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getView("state3"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// state4
		onView = onViews.get(3);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getView("state4"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// state5
		onView = onViews.get(4);
		assertTrue(onView.isNonvariant());
		assertEquals(schema.getView("state5"), onView.getState());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

	}

	
}

