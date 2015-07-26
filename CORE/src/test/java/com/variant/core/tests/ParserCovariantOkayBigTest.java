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
 * test3 + +   - -
 * test4 + + - + -
 * test5 + - +   -
 * blank: not instrumented, -: invariant, +: has variants.
 * 
 * T1, T2, T3(T1), T4(T2,T3), T5(T1,T2,T3,T4)
 * 
 * @author Igor
 */
public class ParserCovariantOkayBigTest extends BaseTest {

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
		
		ParserResponse response = Variant.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

		Schema schema = Variant.getSchema();
		final Test test1 = schema.getTest("test1");
		final Test test2 = schema.getTest("test2");
		final Test test3 = schema.getTest("test3");
		final Test test4 = schema.getTest("test4");
		final Test test5 = schema.getTest("test5");

		// 
		// Declared test covariance.
		//
		assertEquals(0, test1.getCovariantTests().size());
		assertEquals(0, test2.getCovariantTests().size());
		assertEquals(VariantListUtils.list(test1), test3.getCovariantTests());
		assertEquals(VariantListUtils.list(test2, test3), test4.getCovariantTests());
		assertEquals(VariantListUtils.list(test1, test2, test3, test4), test5.getCovariantTests());

		// 
		// Runtime test covariance.
		//
		assertEquals(VariantListUtils.list(test3, test5), VariantRuntime.getCovariantTests(test1));
		assertEquals(VariantListUtils.list(test4, test5), VariantRuntime.getCovariantTests(test2));
		assertEquals(VariantListUtils.list(test1, test4, test5), VariantRuntime.getCovariantTests(test3));
		assertEquals(VariantListUtils.list(test2, test3, test5), VariantRuntime.getCovariantTests(test4));
		assertEquals(VariantListUtils.list(test1, test2, test3, test4), VariantRuntime.getCovariantTests(test5));

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
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/view2/test2.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/view2/test2.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// view3
		onView = onViews.get(2);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view3"), onView.getView());
		variants = onView.getVariants();
		assertEquals(2, variants.size());
		
		variant = variants.get(0);
		assertEquals(variant.getExperience(), test2.getExperience("B"));
		assertEquals("/path/to/view3/test2.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test2.getExperience("C"));
		assertEquals("/path/to/view3/test2.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

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
		assertEquals(4, onViews.size());
		
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
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/view2/test3.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.B+test3.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.C+test3.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.B+test3.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.C+test3.C", variant.getPath());
		
		// view4
		onView = onViews.get(2);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view4"), onView.getView());
		assertEquals(0, onView.getVariants().size());

		// view5
		onView = onViews.get(3);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view5"), onView.getView());
		assertEquals(0, onView.getVariants().size());

		// 
		// test4 OnView objects
		//
		onViews = test4.getOnViews();
		assertEquals(5, onViews.size());
		
		// view1
		onView = onViews.get(0);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view1"), onView.getView());
		variants = onView.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals("/path/to/view1/test4.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals("/path/to/view1/test4.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// view2
		onView = onViews.get(1);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view2"), onView.getView());
		variants = onView.getVariants();
		assertEquals(18, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals("/path/to/view2/test4.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals("/path/to/view2/test4.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test4.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test4.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test4.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test4.C", variant.getPath());

		variant = variants.get(6);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test3.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test3.B+test4.B", variant.getPath());

		variant = variants.get(7);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test3.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test3.C+test4.B", variant.getPath());

		variant = variants.get(8);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test3.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test3.B+test4.C", variant.getPath());

		variant = variants.get(9);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test3.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test3.C+test4.C", variant.getPath());

		variant = variants.get(10);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("B"), test3.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test3.B+test4.B", variant.getPath());
		
		variant = variants.get(11);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("B"), test3.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test3.C+test4.B", variant.getPath());

		variant = variants.get(12);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("C"), test3.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test3.B+test4.B", variant.getPath());

		variant = variants.get(13);		
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("C"), test3.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test3.C+test4.B", variant.getPath());
		
		variant = variants.get(14);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("B"), test3.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test3.B+test4.C", variant.getPath());
		
		variant = variants.get(15);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("B"), test3.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test3.C+test4.C", variant.getPath());

		variant = variants.get(16);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("C"), test3.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test3.B+test4.C", variant.getPath());

		variant = variants.get(17);		
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("C"), test3.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test3.C+test4.C", variant.getPath());		
		
		// view3
		onView = onViews.get(2);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view3"), onView.getView());
		assertEquals(0, onView.getVariants().size());
		
		// view4
		onView = onViews.get(3);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view4"), onView.getView());
		variants = onView.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals("/path/to/view4/test4.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals("/path/to/view4/test4.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view4/test2.B+test4.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view4/test2.C+test4.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view4/test2.B+test4.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view4/test2.C+test4.C", variant.getPath());
				
		// view5
		onView = onViews.get(4);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view5"), onView.getView());
		assertEquals(0, onView.getVariants().size());

		// 
		// test5 OnView objects
		//
		onViews = test5.getOnViews();
		assertEquals(4, onViews.size());
		
		// view1
		onView = onViews.get(0);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view1"), onView.getView());
		variants = onView.getVariants();
		assertEquals(18, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals("/path/to/view1/test5.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals("/path/to/view1/test5.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test3.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.B+test5.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test3.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.C+test5.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test3.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.B+test5.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test3.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.C+test5.C", variant.getPath());

		variant = variants.get(6);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test5.B", variant.getPath());

		variant = variants.get(7);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test5.B", variant.getPath());

		variant = variants.get(8);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test5.C", variant.getPath());

		variant = variants.get(9);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test5.C", variant.getPath());

		variant = variants.get(10);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test3.getExperience("B"), test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.B+test4.B+test5.B", variant.getPath());
		
		variant = variants.get(11);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test3.getExperience("B"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.B+test4.C+test5.B", variant.getPath());

		variant = variants.get(12);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test3.getExperience("C"), test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.C+test4.B+test5.B", variant.getPath());

		variant = variants.get(13);		
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test3.getExperience("C"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.C+test4.C+test5.B", variant.getPath());
		
		variant = variants.get(14);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test3.getExperience("B"), test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.B+test4.B+test5.C", variant.getPath());
		
		variant = variants.get(15);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test3.getExperience("B"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.B+test4.C+test5.C", variant.getPath());

		variant = variants.get(16);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test3.getExperience("C"), test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.C+test4.B+test5.C", variant.getPath());

		variant = variants.get(17);		
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test3.getExperience("C"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test3.C+test4.C+test5.C", variant.getPath());		

		// view2
		onView = onViews.get(1);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view2"), onView.getView());
		assertEquals(0, onView.getVariants().size());

		// view3
		onView = onViews.get(2);
		assertFalse(onView.isInvariant());
		assertEquals(schema.getView("view3"), onView.getView());
		variants = onView.getVariants();
		assertEquals(18, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals("/path/to/view3/test5.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals("/path/to/view3/test5.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test1.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test5.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test1.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test5.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test1.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test5.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test1.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test5.C", variant.getPath());

		variant = variants.get(6);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.B+test5.B", variant.getPath());

		variant = variants.get(7);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.C+test5.B", variant.getPath());

		variant = variants.get(8);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.B+test5.C", variant.getPath());

		variant = variants.get(9);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.C+test5.C", variant.getPath());

		variant = variants.get(10);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test1.getExperience("B"), test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test2.B+test5.B", variant.getPath());
		
		variant = variants.get(11);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test1.getExperience("C"), test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test2.B+test5.B", variant.getPath());

		variant = variants.get(12);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test1.getExperience("B"), test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test2.C+test5.B", variant.getPath());

		variant = variants.get(13);		
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantListUtils.list(test1.getExperience("C"), test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test2.C+test5.B", variant.getPath());
		
		variant = variants.get(14);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test1.getExperience("B"), test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test2.B+test5.C", variant.getPath());
		
		variant = variants.get(15);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test1.getExperience("C"), test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test2.B+test5.C", variant.getPath());

		variant = variants.get(16);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test1.getExperience("B"), test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test2.C+test5.C", variant.getPath());

		variant = variants.get(17);		
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantListUtils.list(test1.getExperience("C"), test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test2.C+test5.C", variant.getPath());		
		
		
		// view2
		onView = onViews.get(3);
		assertTrue(onView.isInvariant());
		assertEquals(schema.getView("view5"), onView.getView());
		assertEquals(0, onView.getVariants().size());

	}

}

