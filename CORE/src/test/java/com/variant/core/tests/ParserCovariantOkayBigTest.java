package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.variant.core.ParserResponse;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.View;
import com.variant.core.util.VariantCollectionsUtils;


/**
 * @author Igor
 */
public class ParserCovariantOkayBigTest extends BaseTest {

	/**
	 * 
	 */
	@org.junit.Test
	public void test() throws Exception {
		
		ParserResponse response = engine.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

		Schema schema = engine.getSchema();
		final Test test1 = schema.getTest("test1");
		final Test test2 = schema.getTest("test2");
		final Test test3 = schema.getTest("test3");
		final Test test4 = schema.getTest("test4");
		final Test test5 = schema.getTest("test5");
		final Test test6 = schema.getTest("test6");

		final View view1 = schema.getView("view1");
		final View view2 = schema.getView("view2");
		final View view3 = schema.getView("view3");
		final View view4 = schema.getView("view4");
		final View view5 = schema.getView("view5");

		// 
		// Declared test covariance.
		//
		assertEquals(0, test1.getCovariantTests().size());
		assertEquals(0, test2.getCovariantTests().size());
		assertEquals(VariantCollectionsUtils.list(test2), test3.getCovariantTests());
		assertEquals(VariantCollectionsUtils.list(test1), test4.getCovariantTests());
		assertEquals(VariantCollectionsUtils.list(test2, test4), test5.getCovariantTests());
		assertEquals(VariantCollectionsUtils.list(test1, test2, test4, test5), test6.getCovariantTests());

		//
		// idleDaysToLive
		//
		assertEquals(0, test1.getIdleDaysToLive());
		assertEquals(0, test2.getIdleDaysToLive());
		assertEquals(1, test3.getIdleDaysToLive());
		assertEquals(0, test4.getIdleDaysToLive());
		assertEquals(0, test5.getIdleDaysToLive());
		assertEquals(0, test6.getIdleDaysToLive());
		//
		// Test disjointness. 
		//

		// test1
		for (Test t: schema.getTests()) {
			boolean exceptionThrown = false;
			try {
				assertFalse(test1.isDisjointWith(t));
			}
			catch (IllegalArgumentException iae) {
				exceptionThrown = true;
			}
			
			if (test1.equals(t)) assertTrue(exceptionThrown);
			else assertFalse(exceptionThrown);
		}
		
		// test2
		for (Test t: schema.getTests()) {
			boolean exceptionThrown = false;
			try {
				assertFalse(test2.isDisjointWith(t));
			}
			catch (IllegalArgumentException iae) {
				exceptionThrown = true;
			}
			
			if (test2.equals(t)) assertTrue(exceptionThrown);
			else assertFalse(exceptionThrown);
		}

		// test3
		for (Test t: schema.getTests()) {
			boolean exceptionThrown = false;
			try {
				if (t.equals(test6)) assertTrue(test3.isDisjointWith(t));
				else assertFalse(test3.isDisjointWith(t));
			}
			catch (IllegalArgumentException iae) {
				exceptionThrown = true;
			}
			
			if (test3.equals(t)) assertTrue(exceptionThrown);
			else assertFalse(exceptionThrown);
		}

		// test4
		for (Test t: schema.getTests()) {
			boolean exceptionThrown = false;
			try {
				assertFalse(test4.isDisjointWith(t));
			}
			catch (IllegalArgumentException iae) {
				exceptionThrown = true;
			}
			
			if (test4.equals(t)) assertTrue(exceptionThrown);
			else assertFalse(exceptionThrown);
		}

		// test5
		for (Test t: schema.getTests()) {
			boolean exceptionThrown = false;
			try {
				assertFalse(test5.isDisjointWith(t));
			}
			catch (IllegalArgumentException iae) {
				exceptionThrown = true;
			}
			
			if (test5.equals(t)) assertTrue(exceptionThrown);
			else assertFalse(exceptionThrown);
		}

		// 
		// test1 OnView objects
		//
		List<Test.OnView> onViews = test1.getOnViews();
		assertEquals(4, onViews.size());
		
		// view2
		Test.OnView onView = onViews.get(0);
		assertFalse(onView.isNonvariant());
		assertEquals(view2, onView.getView());
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
		assertFalse(onView.isNonvariant());
		assertEquals(view3, onView.getView());
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
		assertTrue(onView.isNonvariant());
		assertEquals(view4, onView.getView());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// view5
		onView = onViews.get(3);
		assertTrue(onView.isNonvariant());
		assertEquals(view5, onView.getView());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// 
		// test2 OnView objects
		//
		onViews = test2.getOnViews();
		assertEquals(4, onViews.size());
		
		// view1
		onView = onViews.get(0);
		assertTrue(onView.isNonvariant());
		assertTrue(onView.getVariants().isEmpty());
		assertEquals(view1, onView.getView());

		// view2
		onView = onViews.get(1);
		assertFalse(onView.isNonvariant());
		assertEquals(view2, onView.getView());
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
		assertFalse(onView.isNonvariant());
		assertEquals(view3, onView.getView());
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
		assertFalse(onView.isNonvariant());
		assertEquals(view4, onView.getView());
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
		assertTrue(onView.isNonvariant());
		assertEquals(view1, onView.getView());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// view2
		onView = onViews.get(1);
		assertFalse(onView.isNonvariant());
		assertEquals(view2, onView.getView());
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
		assertEquals(test2.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test2.B+test3.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test2.C+test3.B", variant.getPath());

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
		assertTrue(onView.isNonvariant());
		assertEquals(view3, onView.getView());
		variants = onView.getVariants();
		assertEquals(0, variants.size());

		// view4
		onView = onViews.get(3);
		assertFalse(onView.isNonvariant());
		assertEquals(view4, onView.getView());
		variants = onView.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals("/path/to/view4/test3.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals("/path/to/view4/test3.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(2);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view4/test2.B+test3.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test3.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view4/test2.C+test3.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view4/test2.B+test3.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test3.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test2.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view4/test2.C+test3.C", variant.getPath());

		
		// 
		// test4 OnView objects
		//
		onViews = test4.getOnViews();
		assertEquals(4, onViews.size());
		
		// view1
		onView = onViews.get(0);
		assertFalse(onView.isNonvariant());
		assertEquals(view1, onView.getView());
		variants = onView.getVariants();
		assertEquals(2, variants.size());

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
		assertFalse(onView.isNonvariant());
		assertEquals(view2, onView.getView());
		variants = onView.getVariants();
		assertEquals(6, variants.size());

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
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.B+test4.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test4.getExperience("B"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.C+test4.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("B"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.B+test4.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test4.getExperience("C"));
		assertEquals(1, variant.getCovariantExperiences().size());
		assertEquals(test1.getExperience("C"), variant.getCovariantExperiences().get(0));
		assertEquals("/path/to/view2/test1.C+test4.C", variant.getPath());
		
		// view4
		onView = onViews.get(2);
		assertTrue(onView.isNonvariant());
		assertEquals(view4, onView.getView());
		assertEquals(0, onView.getVariants().size());

		// view5
		onView = onViews.get(3);
		assertTrue(onView.isNonvariant());
		assertEquals(view5, onView.getView());
		assertEquals(0, onView.getVariants().size());

		// 
		// test5 OnView objects
		//
		onViews = test5.getOnViews();
		assertEquals(5, onViews.size());
		
		// view1
		onView = onViews.get(0);
		assertFalse(onView.isNonvariant());
		assertEquals(view1, onView.getView());
		variants = onView.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals("/path/to/view1/test5.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(1);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals("/path/to/view1/test5.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		// view2
		onView = onViews.get(1);
		assertFalse(onView.isNonvariant());
		assertEquals(view2, onView.getView());
		variants = onView.getVariants();
		assertEquals(18, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals("/path/to/view2/test5.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals("/path/to/view2/test5.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test5.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test5.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test5.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test5.C", variant.getPath());

		variant = variants.get(6);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test4.B+test5.B", variant.getPath());

		variant = variants.get(7);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test4.C+test5.B", variant.getPath());

		variant = variants.get(8);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test4.B+test5.C", variant.getPath());

		variant = variants.get(9);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test4.C+test5.C", variant.getPath());

		variant = variants.get(10);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B"), test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test4.B+test5.B", variant.getPath());
		
		variant = variants.get(11);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test4.C+test5.B", variant.getPath());

		variant = variants.get(12);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C"), test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test4.B+test5.B", variant.getPath());

		variant = variants.get(13);		
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test4.C+test5.B", variant.getPath());
		
		variant = variants.get(14);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B"), test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test4.B+test5.C", variant.getPath());
		
		variant = variants.get(15);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test4.C+test5.C", variant.getPath());

		variant = variants.get(16);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C"), test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test4.B+test5.C", variant.getPath());

		variant = variants.get(17);		
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.C+test4.C+test5.C", variant.getPath());		
		
		// view3
		onView = onViews.get(2);
		assertTrue(onView.isNonvariant());
		assertEquals(view3, onView.getView());
		assertEquals(0, onView.getVariants().size());
		
		// view4
		onView = onViews.get(3);
		assertFalse(onView.isNonvariant());
		assertEquals(view4, onView.getView());
		variants = onView.getVariants();
		assertEquals(6, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals("/path/to/view4/test5.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals("/path/to/view4/test5.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view4/test2.B+test5.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test5.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view4/test2.C+test5.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view4/test2.B+test5.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test5.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view4/test2.C+test5.C", variant.getPath());
				
		// view5
		onView = onViews.get(4);
		assertTrue(onView.isNonvariant());
		assertEquals(view5, onView.getView());
		assertEquals(0, onView.getVariants().size());

		// 
		// test6 OnView objects
		//
		onViews = test6.getOnViews();
		assertEquals(4, onViews.size());
		
		// view1
		onView = onViews.get(0);
		assertFalse(onView.isNonvariant());
		assertEquals(view1, onView.getView());
		variants = onView.getVariants();
		assertEquals(18, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals("/path/to/view1/test6.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals("/path/to/view1/test6.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test6.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test6.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test6.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test6.C", variant.getPath());

		variant = variants.get(6);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test5.B+test6.B", variant.getPath());

		variant = variants.get(7);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test5.C+test6.B", variant.getPath());

		variant = variants.get(8);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test5.B+test6.C", variant.getPath());

		variant = variants.get(9);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test5.C+test6.C", variant.getPath());

		variant = variants.get(10);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B"), test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test5.B+test6.B", variant.getPath());
		
		variant = variants.get(11);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B"), test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test5.C+test6.B", variant.getPath());

		variant = variants.get(12);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C"), test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test5.B+test6.B", variant.getPath());

		variant = variants.get(13);		
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C"), test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test5.C+test6.B", variant.getPath());
		
		variant = variants.get(14);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B"), test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test5.B+test6.C", variant.getPath());
		
		variant = variants.get(15);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B"), test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test5.C+test6.C", variant.getPath());

		variant = variants.get(16);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C"), test5.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test5.B+test6.C", variant.getPath());

		variant = variants.get(17);		
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C"), test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test5.C+test6.C", variant.getPath());		

		// view2
		onView = onViews.get(1);
		assertTrue(onView.isNonvariant());
		assertEquals(view2, onView.getView());
		assertEquals(0, onView.getVariants().size());

		// view3
		onView = onViews.get(2);
		assertFalse(onView.isNonvariant());
		assertEquals(view3, onView.getView());
		variants = onView.getVariants();
		assertEquals(18, variants.size());

		variant = variants.get(0);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals("/path/to/view3/test6.B", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());
		
		variant = variants.get(1);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals("/path/to/view3/test6.C", variant.getPath());
		assertEquals(0, variant.getCovariantExperiences().size());

		variant = variants.get(2);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test6.B", variant.getPath());

		variant = variants.get(3);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test6.B", variant.getPath());

		variant = variants.get(4);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test6.C", variant.getPath());

		variant = variants.get(5);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test6.C", variant.getPath());

		variant = variants.get(6);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.B+test6.B", variant.getPath());

		variant = variants.get(7);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.C+test6.B", variant.getPath());

		variant = variants.get(8);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.B+test6.C", variant.getPath());

		variant = variants.get(9);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.C+test6.C", variant.getPath());

		variant = variants.get(10);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("B"), test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test2.B+test6.B", variant.getPath());
		
		variant = variants.get(11);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C"), test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test2.B+test6.B", variant.getPath());

		variant = variants.get(12);
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("B"), test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test2.C+test6.B", variant.getPath());

		variant = variants.get(13);		
		assertEquals(variant.getExperience(), test6.getExperience("B"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C"), test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test2.C+test6.B", variant.getPath());
		
		variant = variants.get(14);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("B"), test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test2.B+test6.C", variant.getPath());
		
		variant = variants.get(15);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C"), test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test2.B+test6.C", variant.getPath());

		variant = variants.get(16);
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("B"), test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B+test2.C+test6.C", variant.getPath());

		variant = variants.get(17);		
		assertEquals(variant.getExperience(), test6.getExperience("C"));
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C"), test2.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test2.C+test6.C", variant.getPath());		
		
		
		// view2
		onView = onViews.get(3);
		assertTrue(onView.isNonvariant());
		assertEquals(view5, onView.getView());
		assertEquals(0, onView.getVariants().size());

	}

}

