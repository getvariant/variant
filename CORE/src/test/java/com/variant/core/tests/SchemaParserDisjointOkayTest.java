package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.variant.core.Variant;
import com.variant.core.schema.Schema;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.ParserResponse;


public class SchemaParserDisjointOkayTest extends BaseTest {
	
	// Happy path schema is used by other tests too.
	public static final String SCHEMA = 

	"{                                                                                \n" +
    	    //==========================================================================//
    	   
    	    "   'vIeWs':[                                                             \n" +
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
    	    "     },                                                                  \n" +
    	    "     {  'name':'view6',                                                  \n" +
    	    "        'path':'/path/to/view6'                                          \n" +
    	    "     },                                                                  \n" +
    	    "     {  'name':'view7',                                                  \n" +
    	    "        'path':'/path/to/view7'                                          \n" +
    	    "     },                                                                  \n" +
    	    "     {  'name':'view8',                                                  \n" +
    	    "        'path':'/path/to/view8'                                          \n" +
    	    "     },                                                                  \n" +
    	    "     {  'name':'view9',                                                  \n" +
    	    "        'path':'/path/to/view9'                                          \n" +
    	    "     },                                                                  \n" +
    	    "     {  'name':'view10',                                                 \n" +
    	    "        'path':'/path/to/view10'                                         \n" +
    	    "     },                                                                  \n" +
    	    "     {  'name':'View1',                                                  \n" +
    	    "        'path':'/path/to/View1'                                          \n" +
    	    "     }                                                                   \n" +
            "  ],                                                                     \n" +
            
    	    //=========================================================================//
    	    
	        "  'TeSts':[                                                              \n" +
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
    	    "              'viewRef':'view1',                                         \n" +
    	    "              'variants':[                                               \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'B',                                 \n" +
    	    "                    'path':'/path/to/view1/test1.B'                      \n" +
    	    "                 },                                                      \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'C',                                 \n" +
    	    "                    'path':'/path/to/view1/test1.C'                      \n" +
    	    "                 }                                                       \n" +
    	    "              ]                                                          \n" +
    	    "           }                                                             \n" +
    	    "        ]                                                                \n" +
    	    "     },                                                                  \n" +
    	    //--------------------------------------------------------------------------//	
    	    "     {                                                                   \n" +
    	    "        'name':'test2',                                                  \n" +
    	    "        'experiences':[                                                  \n" +
    	    "           {                                                             \n" +
    	    "              'name':'C',                                                \n" +
    	    "              'weight':0.5,                                              \n" +
    	    "              'isControl':true                                           \n" +
    	    "           },                                                            \n" +
    	    "           {                                                             \n" +
    	    "              'name':'D',                                                \n" +
    	    "              'weight':0.6                                               \n" +
    	    "           }                                                             \n" +
    	    "        ],                                                               \n" +
    	    "        'onViews':[                                                      \n" +
    	    "           {                                                             \n" +
    	    "              'viewRef':'view3',                                         \n" +
    	    "              'variants':[                                               \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'D',                                 \n" +
    	    "                    'path':'/path/to/view3/test2.D'                      \n" +
    	    "                 }                                                       \n" +
    	    "              ]                                                          \n" +
    	    "           },                                                            \n" +
    	    "           {                                                             \n" +
    	    "              'viewRef':'view2',                                         \n" +
    	    "              'isInvariant':false,                                       \n" +
    	    "              'variants':[                                               \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'D',                                 \n" +
    	    "                    'path':'/path/to/view2/test2.D'                      \n" +
    	    "                 }                                                       \n" +
    	    "              ]                                                          \n" +
    	    "           },                                                            \n" +
    	    "           {                                                             \n" +
    	    "              'viewRef':'view4',                                         \n" +
    	    "              'isInvariant':true                                         \n" +
    	    "           }                                                             \n" +
    	    "        ]                                                                \n" +
    	    "     },                                                                  \n" +
    	    //--------------------------------------------------------------------------//	
    	    "     {                                                                   \n" +
    	    "        'name':'Test1',                                                  \n" +
    	    "        'experiences':[                                                  \n" +
    	    "           {                                                             \n" +
    	    "              'name':'A',                                                \n" +
    	    "              'weight':10,                                               \n" +
    	    "              'isControl':false                                          \n" +
    	    "           },                                                            \n" +
    	    "           {                                                             \n" +
    	    "              'isControl':true,                                          \n" +
    	    "              'name':'B',                                                \n" +
    	    "              'weight':20                                                \n" +
    	    "           }                                                             \n" +
    	    "        ],                                                               \n" +
    	    "        'onViews':[                                                      \n" +
    	    "           {                                                             \n" +
    	    "              'viewRef':'view1',                                         \n" +
    	    "              'variants':[                                               \n" +
    	    "                 {                                                       \n" +
    	    "                    'experienceRef':'A',                                 \n" +
    	    "                    'path':'/path/to/view1/Test1.A'                      \n" +
    	    "                 }                                                       \n" +
    	    "              ]                                                          \n" +
    	    "           }                                                             \n" +
    	    "        ]                                                                \n" +
    	    "     }                                                                   \n" +
    	    "  ]                                                                      \n" +
    	    "}                                                                         ";

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
	 * Happy path.
	 */
	@SuppressWarnings("serial")
	@Test
	public void happyPathTest() throws Exception {
		
		ParserResponse response = Variant.parseSchema(SCHEMA);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

		//
		// Views.
		//
		
		String[][] expectedViews = {
				{"view1", "/path/to/view1"},
				{"view2", "/path/to/view2"},
				{"view3", "/path/to/view3"},
				{"view4", "/path/to/view4"},
				{"view5", "/path/to/view5"},
				{"view6", "/path/to/view6"},
				{"view7", "/path/to/view7"},
				{"view8", "/path/to/view8"},
				{"view9", "/path/to/view9"},
				{"view10", "/path/to/view10"},
				{"View1",  "/path/to/View1"}
						
		};

		final Schema schema = Variant.getSchema();
		
		// Verify views returned as a list.
		List<View> actualViews = schema.getViews();
		assertEquals(expectedViews.length, actualViews.size());
		verifyView(expectedViews[0], actualViews.get(0));
		verifyView(expectedViews[1], actualViews.get(1));
		verifyView(expectedViews[2], actualViews.get(2));
		verifyView(expectedViews[3], actualViews.get(3));
		verifyView(expectedViews[4], actualViews.get(4));
		verifyView(expectedViews[5], actualViews.get(5));
		verifyView(expectedViews[6], actualViews.get(6));
		verifyView(expectedViews[7], actualViews.get(7));
		verifyView(expectedViews[8], actualViews.get(8));
		verifyView(expectedViews[9], actualViews.get(9));
		verifyView(expectedViews[10], actualViews.get(10));
		
		// Verify views returned individually by name.
		for (String[] expectedView: expectedViews) {	
			verifyView(expectedView, schema.getView(expectedView[0]));
		}

		// Verify non-existent views.
		assertNull(schema.getView("non-existent'"));		

		for (String[] expectedView: expectedViews) {
			assertNull(schema.getView(expectedView[0].toUpperCase()));
		}
		
		// Instrumented tests.
		View view = schema.getView("view1");
		ArrayList<com.variant.core.schema.Test> expectedInstrumentedTests = new ArrayList<com.variant.core.schema.Test>() {{
			add(schema.getTest("test1"));
			add(schema.getTest("Test1"));
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());
		assertFalse(view.isInvariantIn(schema.getTest("test1")));
		assertFalse(view.isInvariantIn(schema.getTest("Test1")));
		try {
			assertFalse(view.isInvariantIn(schema.getTest("non-existent")));
		}
		catch (NullPointerException npe ) { /* expected */ }

		view = schema.getView("view2");
		expectedInstrumentedTests = new ArrayList<com.variant.core.schema.Test>() {{
			add(schema.getTest("test2"));
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());
		assertFalse(view.isInvariantIn(schema.getTest("test2")));
		
		view = schema.getView("view3");
		expectedInstrumentedTests = new ArrayList<com.variant.core.schema.Test>() {{
			add(schema.getTest("test2"));
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());
		assertFalse(view.isInvariantIn(schema.getTest("test2")));

		view = schema.getView("view4");
		expectedInstrumentedTests = new ArrayList<com.variant.core.schema.Test>() {{
			add(schema.getTest("test2"));
		}};
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());
		assertTrue(view.isInvariantIn(schema.getTest("test2")));
		
		view = schema.getView("view5");
		expectedInstrumentedTests = new ArrayList<com.variant.core.schema.Test>();
		assertEquals(expectedInstrumentedTests, view.getInstrumentedTests());

		//
		// Tests.
		//

		List<com.variant.core.schema.Test> actualTests = schema.getTests();
		
		assertEquals(3, actualTests.size());
		verifyTest1(actualTests.get(0), schema);
		verifyTest1(schema.getTest("test1"), schema);
		verifyTest2(actualTests.get(1), schema);
		verifyTest2(schema.getTest("test2"), schema);
		verifyTest3(actualTests.get(2), schema);
		verifyTest3(schema.getTest("Test1"), schema);
		
		assertNull(schema.getTest("Test2"));
	
	}
	
	/**
	 * 
	 * @param expectedView
	 * @param actualView
	 */
	private static void verifyView(String[] expectedView, View actualView) {
		assertNotNull(actualView);
		assertEquals(expectedView[0], actualView.getName());
		assertEquals(expectedView[1], actualView.getPath());		
	}
	
	/**
	 * 
	 * @param test
	 */
	private static void verifyTest1(com.variant.core.schema.Test test, Schema config) {
		
		assertNotNull(test);
		assertEquals("test1", test.getName());
		
		// Experiences
		List<com.variant.core.schema.Test.Experience> actualExperiences = test.getExperiences();
		assertEquals(3, actualExperiences.size());
		com.variant.core.schema.Test.Experience exp = actualExperiences.get(0);
		assertEquals("A", exp.getName());
		assertEquals(10, exp.getWeight(), 0.000001);
		assertTrue(exp.isControl());
		assertEquals(exp, test.getControlExperience());
		assertEquals(test, exp.getTest());
		exp = actualExperiences.get(1);
		assertEquals("B", exp.getName());
		assertEquals(20, exp.getWeight(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getTest());
		exp = actualExperiences.get(2);
		assertEquals("C", exp.getName());
		assertEquals(30, exp.getWeight(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getTest());
		
		// OnViews
		List<com.variant.core.schema.Test.OnView> actualOnViews = test.getOnViews();
		assertEquals(1, actualOnViews.size());

		com.variant.core.schema.Test.OnView tov = actualOnViews.get(0);
		assertEquals(test, tov.getTest());
		assertEquals(config.getView("view1"), tov.getView());
		assertFalse(tov.isInvariant());
		List<com.variant.core.schema.Test.OnView.Variant> actualVariants =  tov.getVariants();
		assertEquals(2, actualVariants.size());
		com.variant.core.schema.Test.OnView.Variant variant = actualVariants.get(0);
		assertEquals(test.getExperience("B"), variant.getLocalExperience());
		assertEquals(1, variant.getExperiences().size());
		assertEquals("/path/to/view1/test1.B", variant.getPath());
		variant = actualVariants.get(1);
		assertEquals(test.getExperience("C"), variant.getLocalExperience());
		assertEquals(1, variant.getExperiences().size());
		assertEquals("/path/to/view1/test1.C", variant.getPath());
		
		
	}
	
	/**
	 * 
	 * @param test
	 */
	private static void verifyTest2(com.variant.core.schema.Test test, Schema config) {

		assertNotNull(test);
		assertEquals("test2", test.getName());
		
		// Experiences
		List<com.variant.core.schema.Test.Experience> actualExperiences = test.getExperiences();
		assertEquals(2, actualExperiences.size());
		com.variant.core.schema.Test.Experience exp = actualExperiences.get(0);
		assertEquals("C", exp.getName());
		assertEquals(0.5, exp.getWeight(), 0.000001);
		assertTrue(exp.isControl());
		assertEquals(exp, test.getControlExperience());
		assertEquals(test, exp.getTest());
		exp = actualExperiences.get(1);
		assertEquals("D", exp.getName());
		assertEquals(0.6, exp.getWeight(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getTest());
		
		// OnViews
		List<com.variant.core.schema.Test.OnView> actualOnViews = test.getOnViews();
		assertEquals(3, actualOnViews.size());

		com.variant.core.schema.Test.OnView tov = actualOnViews.get(0);
		assertEquals(test, tov.getTest());
		assertEquals(config.getView("view3"), tov.getView());
		assertFalse(tov.isInvariant());
		List<com.variant.core.schema.Test.OnView.Variant> actualVariants =  tov.getVariants();
		assertEquals(1, actualVariants.size());
		com.variant.core.schema.Test.OnView.Variant variant = actualVariants.get(0);
		assertEquals(test.getExperience("D"), variant.getLocalExperience());
		assertEquals(1, variant.getExperiences().size());
		assertEquals("/path/to/view3/test2.D", variant.getPath());

		tov = actualOnViews.get(1);
		assertEquals(test, tov.getTest());
		assertEquals(config.getView("view2"), tov.getView());
		assertFalse(tov.isInvariant());
		actualVariants =  tov.getVariants();
		assertEquals(1, actualVariants.size());
		variant = actualVariants.get(0);
		assertEquals(test.getExperience("D"), variant.getLocalExperience());
		assertEquals(1, variant.getExperiences().size());
		assertEquals("/path/to/view2/test2.D", variant.getPath());
		
		tov = actualOnViews.get(2);
		assertEquals(test, tov.getTest());
		assertEquals(config.getView("view4"), tov.getView());
		assertTrue(tov.isInvariant());
		assertTrue(tov.getVariants().isEmpty());

	}

	/**
	 * 
	 * @param test
	 */
	private static void verifyTest3(com.variant.core.schema.Test test, Schema config) {
		
		assertNotNull(test);
		assertEquals("Test1", test.getName());
		
		// Experiences
		List<com.variant.core.schema.Test.Experience> actualExperiences = test.getExperiences();
		assertEquals(2, actualExperiences.size());
		com.variant.core.schema.Test.Experience exp = actualExperiences.get(0);
		assertEquals("A", exp.getName());
		assertEquals(10, exp.getWeight(), 0.000001);
		assertFalse(exp.isControl());
		assertEquals(test, exp.getTest());
		exp = actualExperiences.get(1);
		assertEquals("B", exp.getName());
		assertEquals(20, exp.getWeight(), 0.000001);
		assertTrue(exp.isControl());
		assertEquals(exp, test.getControlExperience());
		assertEquals(test, exp.getTest());
		
		// OnViews
		List<com.variant.core.schema.Test.OnView> actualOnViews = test.getOnViews();
		assertEquals(1, actualOnViews.size());

		com.variant.core.schema.Test.OnView tov = actualOnViews.get(0);
		assertEquals(test, tov.getTest());
		assertEquals(config.getView("view1"), tov.getView());
		assertFalse(tov.isInvariant());
		List<com.variant.core.schema.Test.OnView.Variant> actualVariants =  tov.getVariants();
		assertEquals(1, actualVariants.size());
		com.variant.core.schema.Test.OnView.Variant variant = actualVariants.get(0);
		assertEquals(test.getExperience("A"), variant.getLocalExperience());
		assertEquals(1, variant.getExperiences().size());
		assertEquals("/path/to/view1/Test1.A", variant.getPath());

	}

	
}

