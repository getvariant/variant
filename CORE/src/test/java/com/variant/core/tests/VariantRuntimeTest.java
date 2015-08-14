package com.variant.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantEventExperience;
import com.variant.core.VariantInternalException;
import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
import com.variant.core.ext.SessionKeyResolverSample.UserDataSample;
import com.variant.core.runtime.VariantRuntimeTestFacade;
import com.variant.core.runtime.ViewServeEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;
import com.variant.core.session.TargetingPersister;
import com.variant.core.session.TargetingPersisterFromString.UserDataFromString;
import com.variant.core.util.VariantCollectionsUtils;
import com.variant.core.util.VariantJunitLogger;


/**
 * @author Igor
 */
public class VariantRuntimeTest extends BaseTest {

	/**
	 * 
	 */
	@org.junit.Test
	public void pathResolution() throws Exception {
		
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

		//
		// Test targetability
		//
		
		thrown = false;
		try{
			VariantRuntimeTestFacade.isTargetable(
				test5, 
				VariantCollectionsUtils.list(
						experience("test1.A"),
						experience("test3.B"),
						experience("test5.A"),
						experience("test6.A")));
		}
		catch (VariantInternalException e) {
			thrown = true;
			assertEquals("Input test [test5] is already targeted", e.getMessage());
		}
		assertTrue(thrown);
		
		thrown = false;
		try{
			VariantRuntimeTestFacade.isTargetable(
				test6, 
				VariantCollectionsUtils.list(
						experience("test1.C"), 
						experience("test2.A"), 
						experience("test3.C"),
						experience("test5.B"),
						experience("test4.A")));
		}
		catch (VariantInternalException e) {
			thrown = true;
			assertEquals("Input set [test1.C,test2.A,test3.C,test5.B,test4.A] is already unresolvable", e.getMessage());
		}
		assertTrue(thrown);

		assertTrue(VariantRuntimeTestFacade.isTargetable(
				test1, 
				VariantCollectionsUtils.list(experience("test2.A"))));

		assertFalse(VariantRuntimeTestFacade.isTargetable(
				test1, 
				VariantCollectionsUtils.list(experience("test2.B"))));

		assertFalse(VariantRuntimeTestFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test2.A"), 
						experience("test3.C"),
						experience("test5.A"),
						experience("test4.A"))));

		assertFalse(VariantRuntimeTestFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test6.C"), 
						experience("test2.B"), 
						experience("test3.C"),
						experience("test5.A"),
						experience("test4.A"))));

		assertTrue(VariantRuntimeTestFacade.isTargetable(
				test2,
				VariantCollectionsUtils.list(
						experience("test6.C"), 
						experience("test3.C"),
						experience("test5.A"),
						experience("test4.A"))));

		assertTrue(VariantRuntimeTestFacade.isTargetable(
				test3,
				VariantCollectionsUtils.list(
						experience("test6.C"), 
						experience("test2.B"), 
						experience("test5.A"),
						experience("test4.A"))));

		assertTrue(VariantRuntimeTestFacade.isTargetable(
				test6,
				VariantCollectionsUtils.list(
						experience("test2.B"), 
						experience("test3.C"),
						experience("test5.A"),
						experience("test4.A"))));
		
		assertFalse(VariantRuntimeTestFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test6.C"), 
						experience("test2.B"), 
						experience("test3.C"),
						experience("test5.A"))));

		assertTrue(VariantRuntimeTestFacade.isTargetable(
				test6,
				VariantCollectionsUtils.list(
						experience("test1.A"), 
						experience("test2.B"), 
						experience("test3.A"),
						experience("test5.B"),
						experience("test4.C"))));

		assertTrue(VariantRuntimeTestFacade.isTargetable(
				test2,
				VariantCollectionsUtils.list(
						experience("test6.C"), 
						experience("test1.A"), 
						experience("test3.A"),
						experience("test5.B"),
						experience("test4.C"))));

		assertTrue(VariantRuntimeTestFacade.isTargetable(
				test4,
				VariantCollectionsUtils.list(
						experience("test6.C"), 
						experience("test1.A"), 
						experience("test2.B"), 
						experience("test3.A"),
						experience("test5.B"))));

		assertFalse(VariantRuntimeTestFacade.isTargetable(
				test5,
				VariantCollectionsUtils.list(
						experience("test6.C"), 
						experience("test1.B"), 
						experience("test2.A"), 
						experience("test3.A"),
						experience("test4.C"))));

		assertFalse(VariantRuntimeTestFacade.isTargetable(
				test3,
				VariantCollectionsUtils.list(
						experience("test6.C"), 
						experience("test2.B"), 
						experience("test5.B"),
						experience("test4.C"))));

		
	}
	
	
	/**
	 * Targeting with OFF tests.
	 * @throws Exception
	 */
	@org.junit.Test
	public void offTestsTest() throws Exception {
		
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
			    	    "     }                                                                   \n" +
			            "  ],                                                                     \n" +
			    	    //=========================================================================//
			    	    
				        "  'tests':[                                                              \n" +
			    	    "     {                                                                   \n" +
			    	    "        'name':'test1',                                                  \n" +
			    	    "        'isOn':true,                                                     \n" +
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
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'viewRef':'view2',                                         \n" +
			    	    "              'isInvariant':true                                         \n" +
			    	    "           },                                                             \n" +
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
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    //--------------------------------------------------------------------------//	
			    	    "     {                                                                   \n" +
			    	    "        'name':'test2',                                                  \n" +
			    	    "        'isOn': false,                                                   \n" +
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
			    	    "              'viewRef':'view3',                                         \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'path':'/path/to/view3/test2.B'                      \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'path':'/path/to/view3/test2.C'                      \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'viewRef':'view2',                                         \n" +
			    	    "              'isInvariant':false,                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'path':'/path/to/view2/test2.B'                      \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'path':'/path/to/view2/test2.C'                      \n" +
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
			    	    "        'name':'test3',                                                  \n" +  
			    	    "        'covariantTestRefs':['test1'],                                   \n" +  
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
			    	    "              'weight':20                                                \n" +
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
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
			    	    "                    'path':'/path/to/view1/test1.B+test3.B'              \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
			    	    "                    'path':'/path/to/view1/test1.C+test3.B'              \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
			    	    "                    'path':'/path/to/view1/test1.B+test3.C'              \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
			    	    "                    'path':'/path/to/view1/test1.C+test3.C'              \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";


		ParserResponse response = Variant.parseSchema(SCHEMA);
		if (response.hasErrors()) printErrors(response);
		assertFalse(response.hasErrors());

		Schema schema = Variant.getSchema();
		View view1 = schema.getView("view1");
		VariantJunitLogger logger = (VariantJunitLogger) Variant.getLogger();		
		VariantSession ssn = Variant.getSession(new UserDataSample("key1"));
		long timestamp = System.currentTimeMillis();
		String persisterString = timestamp + ".test2.B";
		ssn.initTargetingPersister(new UserDataFromString(persisterString));
		TargetingPersister tp = ssn.getTargetingPersister();
		assertEquals(1, tp.getAll().size());
		VariantViewRequest req = Variant.startViewRequest(ssn, view1);

		// test2 is off, but TP has a variant experience for it, which will be substituted for the purposes of lookup with control.
		assertEquals("Session [key1] recognized persisted experience [test2.B] but substituted control experience [test2.A] because test is OFF",
				logger.get(-4).getMessage());
		// test1 is disjoint with test2, so has to default to control.
		assertEquals("Session [key1] targeted for test [test1] with control experience [A]",
				logger.get(-3).getMessage());
		// test3 is covariant with test1, so it is targeted unconstrained.
		assertMatches("Session \\[key1\\] targeted for test \\[test3\\] with experience \\[[A,B,C]\\]",
				logger.get(-2).getMessage());
		// test3 is covariant with test1, so it is targeted unconstrained.
		assertMatches("Session \\[key1\\] resolved view \\[view1\\] as \\[.*\\] for experience vector \\[test2\\.B\\,test1\\.A\\,test3\\.[A,B,C]\\]",
				logger.get(-1).getMessage());
		assertEquals(3, tp.getAll().size());
		// We didn't touch test2.B entry in the TP, even though we used control for resolution.
		assertEquals(experience("test2.B"), tp.get(schema.getTest("test2")));
		System.out.println(req.resolvedViewPath());
		assertMatches("/path/to/view1(/test3\\.[B,C])?", req.resolvedViewPath());
		assertEquals(ssn, req.getSession());
		assertEquals(view1, req.getView());
		
		// View Serve Event.
		ViewServeEvent event = req.getViewServeEvent();
		assertEquals(2, event.getEventExperiences().size());
		int index = 0;
		for (VariantEventExperience ee: event.getEventExperiences()) {
			if (index == 0) {
				assertEquals(experience("test1.A"), ee.getExperience());
			}
			else if (index == 1) {
				assertEqualsMulti(ee.getExperience(), experience("test3.A"), experience("test3.B"), experience("test3.C"));
			}
			else {
				assertTrue(false);
			}
			index++;
		}
	}
	
}

