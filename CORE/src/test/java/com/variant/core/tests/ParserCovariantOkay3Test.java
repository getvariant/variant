package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.runtime.VariantRuntime;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.util.VariantListUtils;


/**
 * All Tests:
 * Tests Views
 *       1 2 3 4 5
 * test1   + + - -
 * test2 - + + +  
 * test3 + + - - -
 * blank: not instrumented, -: invariant, +: has variants.
 * 
 * T1, T2(T1), T3(T2)
 * 
 * @author Igor
 */
public class ParserCovariantOkay3Test extends BaseTest {
	

	/**
	 * 
	 * @throws Exception
	 */
	@Before
	public void beforeEachTest() throws Exception {

		// Bootstrap the Variant container with defaults.
		Variant.Config variantConfig = new Variant.Config();
		variantConfig.getSessionServiceConfig().setKeyResolverClassName("com.variant.ext.session.SessionKeyResolverJunit");
		Variant.bootstrap(variantConfig);

	}

	/**
	 * 
	 */
	@org.junit.Test
	public void test() throws Exception {
		
		final String SCHEMA = 

		"{                                                                                \n" +
	    	    //==========================================================================//
	    	   
	    	    "   'views':[                                                             \n" +
	    	    "     {  'name':'view1',                                                  \n" +
	    	    "        'path':'/path/to/view1'                                          \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'NAME':'view2',                                                  \n" +
	    	    "        'path':'/path/to/view2'                                          \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'nAmE':'view3',                                                  \n" +
	    	    "        'path':'/path/to/view3'                                          \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'name':'view4',                                                  \n" +
	    	    "        'path':'/path/to/view4'                                          \n" +
	    	    "     },                                                                  \n" +
	    	    "     {  'name':'view5',                                                  \n" +
	    	    "        'path':'/path/to/view5'                                          \n" +
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
	    	    "              'viewRef':'view2',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view2/test1.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view2/test1.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view3',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view3/test1.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view3/test1.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view4',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view5',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
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
	    	    "              'viewRef':'view1',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view2',                                         \n" +
	    	    "              'variants':[                                               \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view2/test2.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.B'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.B'              \n" +
	    	    "                 },                                                      \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view2/test2.C'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view2/test1.B+test2.C'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view2/test1.C+test2.C'              \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view3',                                         \n" +
	    	    "              'variants':[                                               \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view3/test2.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view3/test1.B+test2.B'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view3/test1.C+test2.B'              \n" +
	    	    "                 },                                                      \n" +

	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view3/test2.C'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view3/test1.B+test2.C'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test1',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view3/test1.C+test2.C'              \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +

	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view4',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view4/test2.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view4/test2.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     },                                                                  \n" +
	    	    //--------------------------------------------------------------------------//	
	    	    "     {                                                                   \n" +
	    	    "        'name':'test3',                                                  \n" +
	    	    "        'covariantTestRefs': ['test2'],                                  \n" +
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
	    	    "              'viewRef':'view1',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view1/test3.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view1/test3.C'                      \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view2',                                         \n" +
	    	    "              'variants':[                                               \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'path':'/path/to/view2/test3.B'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test2',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view2/test2.B+test3.B'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'B',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test2',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view2/test2.C+test3.B'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'path':'/path/to/view2/test3.C'                      \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test2',                            \n" +
	    	    "                          'experienceRef': 'B'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view2/test2.B+test3.C'              \n" +
	    	    "                 },                                                      \n" +
	    	    "                 {                                                       \n" +
	    	    "                    'experienceRef':'C',                                 \n" +
	    	    "                    'covariantExperienceRefs': [                         \n" +
	    	    "                       {                                                 \n" +
	    	    "                          'testRef': 'test2',                            \n" +
	    	    "                          'experienceRef': 'C'                           \n" +
	    	    "                       }                                                 \n" +
	    	    "                     ],                                                  \n" +
	    	    "                    'path':'/path/to/view2/test2.C+test3.C'              \n" +
	    	    "                 }                                                       \n" +
	    	    "              ]                                                          \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view3',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view4',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
	    	    "           },                                                            \n" +
	    	    "           {                                                             \n" +
	    	    "              'viewRef':'view5',                                         \n" +
	    	    "              'isInvariant':true                                         \n" +
	    	    "           }                                                             \n" +
	    	    "        ]                                                                \n" +
	    	    "     }                                                                   \n" +
	    	    //--------------------------------------------------------------------------//	
	     	    "  ]                                                                      \n" +
	    	    "}                                                                         ";

		ParserResponse response = Variant.parseSchema(SCHEMA);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

		Schema schema = Variant.getSchema();
		final Test test1 = schema.getTest("test1");
		final Test test2 = schema.getTest("test2");
		final Test test3 = schema.getTest("test3");

		// 
		// Declared test covariance.
		//
		assertEquals(0, test1.getCovariantTests().size());
		assertEquals(VariantListUtils.list(test1), test2.getCovariantTests());
		assertEquals(VariantListUtils.list(test2), test3.getCovariantTests());

		// 
		// Runtime test covariance.
		//
		assertEquals(VariantListUtils.list(test2), VariantRuntime.getCovariantTests(test1));
		assertEquals(VariantListUtils.list(test1, test3), VariantRuntime.getCovariantTests(test2));
		assertEquals(VariantListUtils.list(test2), VariantRuntime.getCovariantTests(test3));

		// 
		// test1 OnView objects
		//
		List<Test.OnView> onViews = test1.getOnViews();
		assertEquals(4, onViews.size());
		
		// view2
		Test.OnView onView = onViews.get(0);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view2"), onView.getView());
		List<Test.OnView.Variant> variants = onView.getVariants();
		assertEquals(2, variants.size());

		Test.OnView.Variant variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/view2/test1.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/view2/test1.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// view3
		onView = onViews.get(1);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view3"), onView.getView());
		variants = onView.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test1.getExperience("B"));
		assertEquals("/path/to/view3/test1.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test1.getExperience("C"));
		assertEquals("/path/to/view3/test1.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// view4
		onView = onViews.get(2);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view4"), onView.getView());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// view5
		onView = onViews.get(3);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view5"), onView.getView());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// 
		// test2 OnView objects
		//
		onViews = test2.getOnViews();
		assertEquals(4, onViews.size());
		
		// view1
		onView = onViews.get(0);
		assertTrue(onView.isInvariant());
		assertTrue(onView.getVariants().isEmpty());
		assertEquals(schema.getView("view1"), onView.getView());

		// view2
		onView = onViews.get(1);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view2"), onView.getView());
		variants = onView.getVariants();
		assertEquals(6, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/view2/test2.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.B+test2.B", variant.getPath());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.C+test2.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/view2/test2.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.B+test2.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.C+test2.C", variant.getPath());

		// view3
		onView = onViews.get(2);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view3"), onView.getView());
		variants = onView.getVariants();
		assertEquals(6, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/view3/test2.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view3/test1.B+test2.B", variant.getPath());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view3/test1.C+test2.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/view3/test2.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view3/test1.B+test2.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view3/test1.C+test2.C", variant.getPath());

		// view4
		onView = onViews.get(3);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view4"), onView.getView());
		variants = onView.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/view4/test2.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/view4/test2.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// 
		// test3 OnView objects
		//
		onViews = test3.getOnViews();
		assertEquals(5, onViews.size());
		
		// view1
		onView = onViews.get(0);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view1"), onView.getView());
		variants = onView.getVariants();
		assertEquals(2, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/view1/test3.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/view1/test3.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// view2
		onView = onViews.get(1);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view2"), onView.getView());
		variants = onView.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/view2/test3.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test2.B+test3.B", variant.getPath());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test2.C+test3.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/view2/test3.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test2.B+test3.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test2.C+test3.C", variant.getPath());

		// view3
		onView = onViews.get(2);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view3"), onView.getView());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// view4
		onView = onViews.get(3);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view4"), onView.getView());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// view5
		onView = onViews.get(4);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view5"), onView.getView());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

	}

	
}

