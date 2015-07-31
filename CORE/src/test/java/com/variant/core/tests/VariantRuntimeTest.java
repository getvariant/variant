package com.variant.core.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantInternalException;
import com.variant.core.runtime.VariantRuntime;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.View;
import com.variant.core.util.VariantCollectionsUtils;


/**
 * @author Igor
 */
public class VariantRuntimeTest extends BaseTest {

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
		final Test test6 = schema.getTest("test6");

		final View view1 = schema.getView("view1");
		final View view2 = schema.getView("view2");
		final View view3 = schema.getView("view3");
		final View view4 = schema.getView("view4");
		final View view5 = schema.getView("view5");

		//
		// View variant lookups
		//
		
		// view1

		Test.OnView.Variant variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test1.getExperience("A")   // control, not instrumented
				)
		);
		assertNull(variant);
		
		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test1.getExperience("B")   // variant, not instrumented
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // control, not instrumented
						test2.getExperience("A")   // control, invariant.
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // control
						test2.getExperience("B")   // invariant
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // control
						test2.getExperience("B"),  // invariant
						test3.getExperience("A")   // control, invariant
				)
		);
		assertNull(variant);
		
		boolean thrown = false;
		try {
			VariantRuntime.findViewVariant(
					view1, 
					VariantCollectionsUtils.list(
							test1.getExperience("A"),  // not instrumented
							test2.getExperience("B"),  // invariant
							test3.getExperience("A"),  // invariant
							test1.getExperience("B")   // dupe test
					)
			);
		}
		catch (VariantInternalException e) {
			assertEquals("Duplicate test [test1] in input", e.getMessage());
			thrown = true;
		}
		assertTrue(thrown);
		
		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // not instrumented
						test2.getExperience("B"),  // invariant
						test3.getExperience("A"),  // invariant
						test4.getExperience("B")   // variant
				)
		);
		assertEquals(test4, variant.getTest());
		assertEquals(view1, variant.getOnView().getView());
		assertEquals(test4.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B", variant.getPath());
		
		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test4.getExperience("B"),  // variant
						test2.getExperience("B"),  // invariant
						test1.getExperience("A"),  // control
						test3.getExperience("A")   // control, invariant
				)
		);
		assertEquals(test4, variant.getTest());
		assertEquals(view1, variant.getOnView().getView());
		assertEquals(test4.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test4.getExperience("B"),  // variant
						test2.getExperience("B"),  // invariant
						test1.getExperience("A"),  // control
						test6.getExperience("C"),  // variant
						test3.getExperience("A")   // control, invariant
				)
		);
		assertEquals(test6, variant.getTest());
		assertEquals(view1, variant.getOnView().getView());
		assertEquals(test6.getExperience("C"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test6.C", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test4.getExperience("B"),  // variant
						test2.getExperience("B"),  // invariant
						test1.getExperience("C"),  // invariant
						test6.getExperience("B"),  // variant
						test3.getExperience("A")   // control, invariant
				)
		);
		assertEquals(test6, variant.getTest());
		assertEquals(view1, variant.getOnView().getView());
		assertEquals(test6.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.B+test6.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test4.getExperience("A"),  // control
						test2.getExperience("B"),  // invariant
						test5.getExperience("C"),  // variant
						test3.getExperience("B")   // invariant
				)
		);
		assertEquals(test5, variant.getTest());
		assertEquals(view1, variant.getOnView().getView());
		assertEquals(test5.getExperience("C"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test5.C", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test4.getExperience("C"),  // variant
						test2.getExperience("B"),  // invariant
						test5.getExperience("C"),  // variant
						test3.getExperience("B")   // invariant
				)
		);
		assertEquals(test5, variant.getTest());
		assertEquals(view1, variant.getOnView().getView());
		assertEquals(test5.getExperience("C"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test5.C", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test6.getExperience("A"),  // control
						test4.getExperience("C"),  // variant
						test2.getExperience("B"),  // invariant
						test5.getExperience("C"),  // variant
						test3.getExperience("B")   // variant
				)
		);
		assertEquals(test5, variant.getTest());
		assertEquals(view1, variant.getOnView().getView());
		assertEquals(test5.getExperience("C"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test5.C", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view1, 
				VariantCollectionsUtils.list(
						test4.getExperience("C"),  // variant
						test6.getExperience("B"),  // variant
						test2.getExperience("B"),  // invariant
						test5.getExperience("C"),  // variant
						test3.getExperience("B")   // invariant
				)
		);
		assertEquals(test6, variant.getTest());
		assertEquals(view1, variant.getOnView().getView());
		assertEquals(test6.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test4.getExperience("C"), test5.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view1/test4.C+test5.C+test6.B", variant.getPath());

		
		// view2

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test1.getExperience("A")   // control
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test1.getExperience("B")   // variant
				)
		);
		assertEquals(test1, variant.getTest());
		assertEquals(view2, variant.getOnView().getView());
		assertEquals(test1.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test1.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // control
						test2.getExperience("B")   // variant
				)
		);
		assertEquals(test2, variant.getTest());
		assertEquals(view2, variant.getOnView().getView());
		assertEquals(test2.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // control
						test2.getExperience("A")   // control
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // control
						test2.getExperience("B"),  // variant
						test3.getExperience("A")   // control
				)
		);
		assertEquals(test2, variant.getTest());
		assertEquals(view2, variant.getOnView().getView());
		assertEquals(test2.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B", variant.getPath());

		thrown = false;
		try {
			VariantRuntime.findViewVariant(
					view2, 
					VariantCollectionsUtils.list(
							test1.getExperience("A"),  // control
							test2.getExperience("B"),  // invariant
							test3.getExperience("A"),  // control, invariant
							test3.getExperience("A")   // dupe test
					)
			);
		}
		catch (VariantInternalException e) {
			assertEquals("Duplicate test [test3] in input", e.getMessage());
			thrown = true;
		}
		assertTrue(thrown);
		
		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // control
						test2.getExperience("B"),  // variant, unsupported
						test3.getExperience("A"),  // control
						test4.getExperience("B")   // variant
				)
		);
		assertNull(variant);
		
		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test4.getExperience("B"),  // variant
						test2.getExperience("B"),  // variant
						test1.getExperience("A"),  // control
						test3.getExperience("A")   // control
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test4.getExperience("B"),  // variant
						test2.getExperience("A"),  // control
						test1.getExperience("C"),  // variant
						test6.getExperience("C"),  // invariant
						test3.getExperience("A")   // control
				)
		);
		assertEquals(test4, variant.getTest());
		assertEquals(view2, variant.getOnView().getView());
		assertEquals(test4.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test1.C+test4.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test4.getExperience("B"),  // variant
						test2.getExperience("A"),  // control
						test1.getExperience("C"),  // variant
						test6.getExperience("B"),  // invariant
						test3.getExperience("A")   // control
				)
		);
		assertEquals(test4, variant.getTest());
		assertEquals(view2, variant.getOnView().getView());
		assertEquals(test4.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test1.C+test4.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test4.getExperience("A"),  // control
						test2.getExperience("B"),  // variant
						test5.getExperience("C"),  // variant
						test3.getExperience("B")   // variant, unsupported.
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test4.getExperience("C"),  // variant
						test2.getExperience("B"),  // variant
						test5.getExperience("C"),  // variant
						test3.getExperience("A")   // control
				)
		);
		assertEquals(test5, variant.getTest());
		assertEquals(view2, variant.getOnView().getView());
		assertEquals(test5.getExperience("C"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test4.C+test5.C", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test6.getExperience("A"),  // control
						test4.getExperience("C"),  // variant
						test2.getExperience("B"),  // variant
						test5.getExperience("C"),  // variant
						test3.getExperience("B")   // variant, unsupported.
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test4.getExperience("C"),  // variant
						test6.getExperience("B"),  // invariant
						test2.getExperience("B"),  // variant
						test5.getExperience("C"),  // variant
						test3.getExperience("A")   // control
				)
		);
		assertEquals(test5, variant.getTest());
		assertEquals(view2, variant.getOnView().getView());
		assertEquals(test5.getExperience("C"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B"), test4.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B+test4.C+test5.C", variant.getPath());

		// view3

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test2.getExperience("A")   // control
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test4.getExperience("B")   // not instrumented
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test3.getExperience("B")   // invariant
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test2.getExperience("B")   // variant
				)
		);
		assertEquals(test2, variant.getTest());
		assertEquals(view3, variant.getOnView().getView());
		assertEquals(test2.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test1.getExperience("B"),  // variant
						test2.getExperience("A")   // control
				)
		);
		assertEquals(test1, variant.getTest());
		assertEquals(view3, variant.getOnView().getView());
		assertEquals(test1.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // control
						test2.getExperience("A")   // control
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view2, 
				VariantCollectionsUtils.list(
						test1.getExperience("A"),  // control
						test2.getExperience("B"),  // variant
						test3.getExperience("A")   // invariant
				)
		);
		assertEquals(test2, variant.getTest());
		assertEquals(view2, variant.getOnView().getView());
		assertEquals(test2.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view2/test2.B", variant.getPath());

		thrown = false;
		try {
			VariantRuntime.findViewVariant(
					view3, 
					VariantCollectionsUtils.list(
							test1.getExperience("A"),  // control
							test2.getExperience("B"),  // variant
							test3.getExperience("A"),  // invariant
							test2.getExperience("A")   // dupe test
					)
			);
		}
		catch (VariantInternalException e) {
			assertEquals("Duplicate test [test2] in input", e.getMessage());
			thrown = true;
		}
		assertTrue(thrown);
		
		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test1.getExperience("C"),  // variant
						test2.getExperience("B"),  // variant, unsupported
						test3.getExperience("A"),  // invariant
						test4.getExperience("B")   // uninstrumented
				)
		);
		assertNull(variant);
		
		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test4.getExperience("B"),  // uninstrumented
						test2.getExperience("B"),  // variant
						test1.getExperience("A"),  // control
						test3.getExperience("C")   // invariant
				)
		);
		assertEquals(test2, variant.getTest());
		assertEquals(view3, variant.getOnView().getView());
		assertEquals(test2.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test4.getExperience("B"),  // uninstrumented
						test2.getExperience("A"),  // control
						test1.getExperience("C"),  // variant
						test6.getExperience("C"),  // variant
						test3.getExperience("A")   // invariant
				)
		);
		assertEquals(test6, variant.getTest());
		assertEquals(view3, variant.getOnView().getView());
		assertEquals(test6.getExperience("C"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test6.C", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test4.getExperience("B"),  // uninstrumented
						test2.getExperience("B"),  // variant
						test1.getExperience("C"),  // variant
						test6.getExperience("B"),  // variant
						test3.getExperience("A")   // invariant
				)
		);
		assertEquals(test6, variant.getTest());
		assertEquals(view3, variant.getOnView().getView());
		assertEquals(test6.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test1.getExperience("C"), test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test1.C+test2.B+test6.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test4.getExperience("A"),  // uninstrumented
						test2.getExperience("B"),  // variant
						test5.getExperience("C"),  // invariant
						test3.getExperience("B")   // invariant
				)
		);
		assertEquals(test2, variant.getTest());
		assertEquals(view3, variant.getOnView().getView());
		assertEquals(test2.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test4.getExperience("C"),  // uninstrumented
						test2.getExperience("B"),  // variant
						test5.getExperience("C"),  // invariant
						test1.getExperience("A")   // control
				)
		);
		assertEquals(test2, variant.getTest());
		assertEquals(view3, variant.getOnView().getView());
		assertEquals(test2.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.EMPTY_LIST, variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.B", variant.getPath());

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test6.getExperience("A"),  // control
						test4.getExperience("C"),  // uninstrumented
						test2.getExperience("B"),  // variant, unsupported
						test5.getExperience("C"),  // invariant
						test1.getExperience("B")   // variant
				)
		);
		assertNull(variant);

		variant = VariantRuntime.findViewVariant(
				view3, 
				VariantCollectionsUtils.list(
						test4.getExperience("C"),  // uninstrumented
						test6.getExperience("B"),  // variant
						test2.getExperience("B"),  // variant
						test5.getExperience("C"),  // uninstrumented
						test3.getExperience("A")   // invariant
				)
		);
		assertEquals(test6, variant.getTest());
		assertEquals(view3, variant.getOnView().getView());
		assertEquals(test6.getExperience("B"), variant.getExperience());
		assertEquals(VariantCollectionsUtils.list(test2.getExperience("B")), variant.getCovariantExperiences());
		assertEquals("/path/to/view3/test2.B+test6.B", variant.getPath());

	}


	
}

