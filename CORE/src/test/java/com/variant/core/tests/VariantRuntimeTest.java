package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantInternalException;
import com.variant.core.runtime.VariantRuntimeTestFacade;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
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
		// View resolutions
		//
		
		// view1

		String path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test1.A")   // control, not instrumented
				)
		);
		assertEquals("/path/to/view1", path);
		
		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test1.B")   // not instrumented
				)
		);
		assertEquals("/path/to/view1", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // control
						experience("test2.A")   // invariant.
				)
		);
		assertEquals("/path/to/view1", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // control
						experience("test2.B")   // invariant
				)
		);
		assertEquals("/path/to/view1", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // control
						experience("test2.B"),  // invariant
						experience("test3.A")   // invariant
				)
		);
		assertEquals("/path/to/view1", path);
		
		boolean thrown = false;
		try {
			VariantRuntimeTestFacade.resolveViewPath(
					view1, 
					VariantCollectionsUtils.list(
							experience("test1.A"),  // not instrumented
							experience("test2.B"),  // invariant
							experience("test3.A"),  // invariant
							experience("test1.B")   // dupe test
					)
			);
		}
		catch (VariantInternalException e) {
			assertEquals("Duplicate test [test1] in input", e.getMessage());
			thrown = true;
		}
		assertTrue(thrown);
		
		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // not instrumented
						experience("test2.B"),  // invariant
						experience("test3.A"),  // invariant
						experience("test4.B")   // variant
				)
		);
		assertEquals("/path/to/view1/test4.B", path);
		
		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test4.B"),  // variant
						experience("test2.B"),  // invariant
						experience("test1.A"),  // control
						experience("test3.A")   // control, invariant
				)
		);
		assertEquals("/path/to/view1/test4.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test4.B"),  // variant
						experience("test2.B"),  // invariant
						experience("test1.A"),  // control
						experience("test6.C"),  // variant
						experience("test3.A")   // control, invariant
				)
		);
		assertEquals("/path/to/view1/test4.B+test6.C", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test4.B"),  // variant
						experience("test2.B"),  // invariant
						experience("test1.C"),  // invariant
						experience("test6.B"),  // variant
						experience("test3.A")   // control, invariant
				)
		);
		assertEquals("/path/to/view1/test4.B+test6.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test4.A"),  // control
						experience("test2.B"),  // invariant
						experience("test5.C"),  // variant
						experience("test3.B")   // invariant
				)
		);
		assertEquals("/path/to/view1/test5.C", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test4.C"),  // variant
						experience("test2.B"),  // invariant
						experience("test5.C"),  // variant
						experience("test3.B")   // invariant
				)
		);
		assertEquals("/path/to/view1/test4.C+test5.C", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test6.A"),  // control
						experience("test4.C"),  // variant
						experience("test2.B"),  // invariant
						experience("test5.C"),  // variant
						experience("test3.B")   // variant
				)
		);
		assertEquals("/path/to/view1/test4.C+test5.C", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view1, 
				VariantCollectionsUtils.list(
						experience("test4.C"),  // variant
						experience("test6.B"),  // variant
						experience("test2.B"),  // invariant
						experience("test5.C"),  // variant
						experience("test3.B")   // invariant
				)
		);
		assertEquals("/path/to/view1/test4.C+test5.C+test6.B", path);

		
		// view2

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test1.A")   // control
				)
		);
		assertEquals("/path/to/view2", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test1.B")   // variant
				)
		);
		assertEquals("/path/to/view2/test1.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // control
						experience("test2.B")   // variant
				)
		);
		assertEquals("/path/to/view2/test2.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // control
						experience("test2.A")   // control
				)
		);
		assertEquals("/path/to/view2", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // control
						experience("test2.B"),  // variant
						experience("test3.A")   // control
				)
		);
		assertEquals("/path/to/view2/test2.B", path);

		thrown = false;
		try {
			VariantRuntimeTestFacade.resolveViewPath(
					view2, 
					VariantCollectionsUtils.list(
							experience("test1.A"),  // control
							experience("test2.B"),  // invariant
							experience("test3.A"),  // control, invariant
							experience("test3.A")   // dupe test
					)
			);
		}
		catch (VariantInternalException e) {
			assertEquals("Duplicate test [test3] in input", e.getMessage());
			thrown = true;
		}
		assertTrue(thrown);
		
		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // control
						experience("test2.B"),  // variant
						experience("test3.A"),  // control
						experience("test4.B")   // variant, unsupported
				)
		);
		assertNull(path);
		
		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test4.B"),  // variant, unsupported
						experience("test2.B"),  // variant
						experience("test1.A"),  // control
						experience("test3.A")   // control
				)
		);
		assertNull(path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test4.B"),  // variant
						experience("test2.A"),  // control
						experience("test1.C"),  // variant
						experience("test6.C"),  // invariant
						experience("test3.A")   // control
				)
		);
		assertEquals("/path/to/view2/test1.C+test4.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test4.B"),  // variant
						experience("test2.A"),  // control
						experience("test1.C"),  // variant
						experience("test6.B"),  // invariant
						experience("test3.A")   // control
				)
		);
		assertEquals("/path/to/view2/test1.C+test4.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test4.A"),  // control
						experience("test2.B"),  // variant
						experience("test5.C"),  // variant
						experience("test3.B")   // variant, unsupported.
				)
		);
		assertNull(path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test4.C"),  // variant
						experience("test2.B"),  // variant
						experience("test5.C"),  // variant
						experience("test3.A")   // control
				)
		);
		assertEquals("/path/to/view2/test2.B+test4.C+test5.C", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test6.A"),  // control
						experience("test4.C"),  // variant
						experience("test2.B"),  // variant
						experience("test5.C"),  // variant
						experience("test3.B")   // variant, unsupported.
				)
		);
		assertNull(path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test4.C"),  // variant
						experience("test6.B"),  // invariant
						experience("test2.B"),  // variant
						experience("test5.C"),  // variant
						experience("test3.A")   // control
				)
		);
		assertEquals("/path/to/view2/test2.B+test4.C+test5.C", path);

		// view3

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test2.A")   // control
				)
		);
		assertEquals("/path/to/view3", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test4.B")   // not instrumented
				)
		);
		assertEquals("/path/to/view3", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test3.B")   // invariant
				)
		);
		assertEquals("/path/to/view3", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test2.B")   // variant
				)
		);
		assertEquals("/path/to/view3/test2.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test1.B"),  // variant
						experience("test2.A")   // control
				)
		);
		assertEquals("/path/to/view3/test1.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // control
						experience("test2.A")   // control
				)
		);
		assertEquals("/path/to/view2", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view2, 
				VariantCollectionsUtils.list(
						experience("test1.A"),  // control
						experience("test2.B"),  // variant
						experience("test3.A")   // invariant
				)
		);
		assertEquals("/path/to/view2/test2.B", path);

		thrown = false;
		try {
			VariantRuntimeTestFacade.resolveViewPath(
					view3, 
					VariantCollectionsUtils.list(
							experience("test1.A"),  // control
							experience("test2.B"),  // variant
							experience("test3.A"),  // invariant
							experience("test2.A")   // dupe test
					)
			);
		}
		catch (VariantInternalException e) {
			assertEquals("Duplicate test [test2] in input", e.getMessage());
			thrown = true;
		}
		assertTrue(thrown);
		
		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test1.C"),  // variant
						experience("test2.B"),  // variant, unsupported
						experience("test3.A"),  // invariant
						experience("test4.B")   // uninstrumented
				)
		);
		assertNull(path);
		
		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test4.B"),  // uninstrumented
						experience("test2.B"),  // variant
						experience("test1.A"),  // control
						experience("test3.C")   // invariant
				)
		);
		assertEquals("/path/to/view3/test2.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test4.B"),  // uninstrumented
						experience("test2.A"),  // control
						experience("test1.C"),  // variant
						experience("test6.C"),  // variant
						experience("test3.A")   // invariant
				)
		);
		assertEquals("/path/to/view3/test1.C+test6.C", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test4.B"),  // uninstrumented
						experience("test2.B"),  // variant
						experience("test1.C"),  // variant
						experience("test6.B"),  // variant
						experience("test3.A")   // invariant
				)
		);
		assertEquals("/path/to/view3/test1.C+test2.B+test6.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test4.A"),  // uninstrumented
						experience("test2.B"),  // variant
						experience("test5.C"),  // invariant
						experience("test3.B")   // invariant
				)
		);
		assertEquals("/path/to/view3/test2.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test4.C"),  // uninstrumented
						experience("test2.B"),  // variant
						experience("test5.C"),  // invariant
						experience("test1.A")   // control
				)
		);
		assertEquals("/path/to/view3/test2.B", path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test6.A"),  // control
						experience("test4.C"),  // uninstrumented
						experience("test2.B"),  // variant, unsupported
						experience("test5.C"),  // invariant
						experience("test1.B")   // variant
				)
		);
		assertNull(path);

		path = VariantRuntimeTestFacade.resolveViewPath(
				view3, 
				VariantCollectionsUtils.list(
						experience("test4.C"),  // uninstrumented
						experience("test6.B"),  // variant
						experience("test2.B"),  // variant
						experience("test5.C"),  // uninstrumented
						experience("test3.A")   // invariant
				)
		);
		assertEquals("/path/to/view3/test2.B+test6.B", path);
		
		//
		// View resolutions
		//

		Collection<Experience> subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A")));
		assertTrue(subVector.isEmpty());
		
		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.B")));
		assertTrue(subVector.isEmpty());

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A"), 
								experience("test2.B")));
		assertTrue(subVector.isEmpty());

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C"), 
								experience("test2.B")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A"), 
								experience("test2.B"), 
								experience("test3.A")));
		assertTrue(subVector.isEmpty());

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A"), 
								experience("test2.B"), 
								experience("test3.C")));
		assertTrue(subVector.isEmpty());

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C"), 
								experience("test2.B"), 
								experience("test3.C")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A"), 
								experience("test2.B"), 
								experience("test3.C"),
								experience("test4.A")));
		assertTrue(subVector.isEmpty());

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C"), 
								experience("test2.B"), 
								experience("test3.C"),
								experience("test4.A")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C"), 
								experience("test2.B"), 
								experience("test3.C"),
								experience("test4.B")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C"), experience("test4.B")), subVector);
//
		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C"), 
								experience("test2.B"), 
								experience("test3.C"),
								experience("test5.B"),
								experience("test4.B")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C"), experience("test3.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C"), 
								experience("test2.A"), 
								experience("test3.C"),
								experience("test5.B"),
								experience("test4.B")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C"), experience("test3.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C"), 
								experience("test2.A"), 
								experience("test3.C"),
								experience("test5.B"),
								experience("test4.A")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C"), experience("test3.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C"), 
								experience("test2.A"), 
								experience("test3.C"),
								experience("test5.A"),
								experience("test4.A")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C"), 
								experience("test1.C"), 
								experience("test2.B"), 
								experience("test3.C"),
								experience("test5.A"),
								experience("test4.A")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C"), 
								experience("test1.C"), 
								experience("test2.B"), 
								experience("test3.C"),
								experience("test5.A"),
								experience("test4.C")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C"), experience("test4.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C"), 
								experience("test1.C"), 
								experience("test2.B"), 
								experience("test3.C"),
								experience("test5.B"),
								experience("test4.C")));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C"), experience("test3.C")), subVector);

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C"), 
								experience("test1.A"), 
								experience("test2.B"), 
								experience("test3.A"),
								experience("test5.B"),
								experience("test4.C")));
		assertTrue(subVector.isEmpty());

		subVector = 
				VariantRuntimeTestFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C"), 
								experience("test2.B"), 
								experience("test5.B"),
								experience("test4.C")));
		assertTrue(subVector.isEmpty());

	}
	
}

