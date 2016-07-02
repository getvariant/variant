package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;
import com.variant.core.impl.VariantRuntimeTestFacade;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.util.VariantCollectionsUtils;


/**
 * @author Igor
 */
public class CoreRuntimeTest extends BaseTestCore {

	private VariantCore core = rebootApi();

	/**
	 * 
	 */
	@org.junit.Test
	public void pathResolution() throws Exception {
		
		VariantRuntimeTestFacade runtimeFacade = new VariantRuntimeTestFacade(core.getRuntime());
		
		ParserResponse response = core.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = core.getSchema();
		final Test test1 = schema.getTest("test1");
		final Test test2 = schema.getTest("test2");
		final Test test3 = schema.getTest("test3");
		final Test test4 = schema.getTest("test4");
		final Test test5 = schema.getTest("test5");
		final Test test6 = schema.getTest("test6");

		final State state1 = schema.getState("state1");
		final State state2 = schema.getState("state2");
		final State state3 = schema.getState("state3");
		//final State state4 = schema.getState("state4");
		//final State state5 = schema.getState("state5");

		//
		// View resolutions
		//
		
		// state1

		Map<String,String> params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema)   // control, not instrumented
				)
		);
		assertEquals("/path/to/state1", params.get("path"));
		
		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test1.B", schema)   // not instrumented
				)
		);
		assertEquals("/path/to/state1", params.get("path"));

		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.A", schema)   // nonvariant.
				)
		);
		assertEquals("/path/to/state1", params.get("path"));

		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.B", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state1", params.get("path"));

		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.B", schema),  // nonvariant
						experience("test3.A", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state1", params.get("path"));
		
		boolean thrown = false;
		try {
			runtimeFacade.resolveState(
					state1, 
					VariantCollectionsUtils.list(
							experience("test1.A", schema),  // not instrumented
							experience("test2.B", schema),  // nonvariant
							experience("test3.A", schema),  // nonvariant
							experience("test1.B", schema)   // dupe test
					)
			);
		}
		catch (VariantInternalException e) {
			assertEquals("Duplicate test [test1] in input", e.getMessage());
			thrown = true;
		}
		assertTrue(thrown);
		
		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // not instrumented
						experience("test2.B", schema),  // nonvariant
						experience("test3.A", schema),  // nonvariant
						experience("test4.B", schema)   // variant
				)
		);
		assertEquals("/path/to/state1/test4.B", params.get("path"));
		
		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test1.A", schema),  // control
						experience("test3.A", schema)   // control, nonvariant
				)
		);
		assertEquals("/path/to/state1/test4.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test1.A", schema),  // control
						experience("test6.C", schema),  // variant
						experience("test3.A", schema)   // control, nonvariant
				)
		);
		assertEquals("/path/to/state1/test4.B+test6.C", params.get("path"));

		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test1.C", schema),  // nonvariant
						experience("test6.B", schema),  // variant
						experience("test3.A", schema)   // control, nonvariant
				)
		);
		assertEquals("/path/to/state1/test4.B+test6.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.A", schema),  // control
						experience("test2.B", schema),  // nonvariant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state1/test5.C", params.get("path"));

		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state1/test4.C+test5.C", params.get("path"));

		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test6.A", schema),  // control
						experience("test4.C", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // variant
				)
		);
		assertEquals("/path/to/state1/test4.C+test5.C", params.get("path"));

		params = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant
						experience("test6.B", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state1/test4.C+test5.C+test6.B", params.get("path"));

		
		// state2

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema)   // control
				)
		);
		assertEquals("/path/to/state2", params.get("path"));

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.B", schema)   // variant
				)
		);
		assertEquals("/path/to/state2/test1.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.B", schema)   // variant
				)
		);
		assertEquals("/path/to/state2/test2.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.A", schema)   // control
				)
		);
		assertEquals("/path/to/state2", params.get("path"));

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.B", schema),  // variant
						experience("test3.A", schema)   // control
				)
		);
		assertEquals("/path/to/state2/test2.B", params.get("path"));

		thrown = false;
		try {
			runtimeFacade.resolveState(
					state2, 
					VariantCollectionsUtils.list(
							experience("test1.A", schema),  // control
							experience("test2.B", schema),  // nonvariant
							experience("test3.A", schema),  // control, nonvariant
							experience("test3.A", schema)   // dupe test
					)
			);
		}
		catch (VariantInternalException e) {
			assertEquals("Duplicate test [test3] in input", e.getMessage());
			thrown = true;
		}
		assertTrue(thrown);
		
		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.B", schema),  // variant
						experience("test3.A", schema),  // control
						experience("test4.B", schema)   // variant, unsupported
				)
		);
		assertNull(params);
		
		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant, unsupported
						experience("test2.B", schema),  // variant
						experience("test1.A", schema),  // control
						experience("test3.A", schema)   // control
				)
		);
		assertNull(params);

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant
						experience("test2.A", schema),  // control
						experience("test1.C", schema),  // variant
						experience("test6.C", schema),  // nonvariant
						experience("test3.A", schema)   // control
				)
		);
		assertEquals("/path/to/state2/test1.C+test4.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant
						experience("test2.A", schema),  // control
						experience("test1.C", schema),  // variant
						experience("test6.B", schema),  // nonvariant
						experience("test3.A", schema)   // control
				)
		);
		assertEquals("/path/to/state2/test1.C+test4.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.A", schema),  // control
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // variant, unsupported.
				)
		);
		assertNull(params);

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // variant
						experience("test3.A", schema)   // control
				)
		);
		assertEquals("/path/to/state2/test2.B+test4.C+test5.C", params.get("path"));

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test6.A", schema),  // control
						experience("test4.C", schema),  // variant
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // variant, unsupported.
				)
		);
		assertNull(params);

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant
						experience("test6.B", schema),  // nonvariant
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // variant
						experience("test3.A", schema)   // control
				)
		);
		assertEquals("/path/to/state2/test2.B+test4.C+test5.C", params.get("path"));

		// state3

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test2.A", schema)   // control
				)
		);
		assertEquals("/path/to/state3", params.get("path"));

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema)   // not instrumented
				)
		);
		assertEquals("/path/to/state3", params.get("path"));

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state3", params.get("path"));

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema)   // variant
				)
		);
		assertEquals("/path/to/state3/test2.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test1.B", schema),  // variant
						experience("test2.A", schema)   // control
				)
		);
		assertEquals("/path/to/state3/test1.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.A", schema)   // control
				)
		);
		assertEquals("/path/to/state2", params.get("path"));

		params = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.B", schema),  // variant
						experience("test3.A", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state2/test2.B", params.get("path"));

		thrown = false;
		try {
			runtimeFacade.resolveState(
					state3, 
					VariantCollectionsUtils.list(
							experience("test1.A", schema),  // control
							experience("test2.B", schema),  // variant
							experience("test3.A", schema),  // nonvariant
							experience("test2.A", schema)   // dupe test
					)
			);
		}
		catch (VariantInternalException e) {
			assertEquals("Duplicate test [test2] in input", e.getMessage());
			thrown = true;
		}
		assertTrue(thrown);
		
		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test1.C", schema),  // variant
						experience("test2.B", schema),  // variant, unsupported
						experience("test3.A", schema),  // nonvariant
						experience("test4.B", schema)   // uninstrumented
				)
		);
		assertNull(params);
		
		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // uninstrumented
						experience("test2.B", schema),  // variant
						experience("test1.A", schema),  // control
						experience("test3.C", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state3/test2.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // uninstrumented
						experience("test2.A", schema),  // control
						experience("test1.C", schema),  // variant
						experience("test6.C", schema),  // variant
						experience("test3.A", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state3/test1.C+test6.C", params.get("path"));

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // uninstrumented
						experience("test2.B", schema),  // variant
						experience("test1.C", schema),  // variant
						experience("test6.B", schema),  // variant
						experience("test3.A", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state3/test1.C+test2.B+test6.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test4.A", schema),  // uninstrumented
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // nonvariant
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state3/test2.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // uninstrumented
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // nonvariant
						experience("test1.A", schema)   // control
				)
		);
		assertEquals("/path/to/state3/test2.B", params.get("path"));

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test6.A", schema),  // control
						experience("test4.C", schema),  // uninstrumented
						experience("test2.B", schema),  // variant, unsupported
						experience("test5.C", schema),  // nonvariant
						experience("test1.B", schema)   // variant
				)
		);
		assertNull(params);

		params = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // uninstrumented
						experience("test6.B", schema),  // variant
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // uninstrumented
						experience("test3.A", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state3/test2.B+test6.B", params.get("path"));
		
		//
		// View resolutions
		//

		Collection<Experience> subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A", schema)));
		assertTrue(subVector.isEmpty());
		
		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.B", schema)));
		assertTrue(subVector.isEmpty());

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A", schema), 
								experience("test2.B", schema)));
		assertTrue(subVector.isEmpty());

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.B", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A", schema), 
								experience("test2.B", schema), 
								experience("test3.A", schema)));
		assertTrue(subVector.isEmpty());

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A", schema), 
								experience("test2.B", schema), 
								experience("test3.C", schema)));
		assertTrue(subVector.isEmpty());

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.B", schema), 
								experience("test3.C", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.A", schema), 
								experience("test2.B", schema), 
								experience("test3.C", schema),
								experience("test4.A", schema)));
		assertTrue(subVector.isEmpty());

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.B", schema), 
								experience("test3.C", schema),
								experience("test4.A", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.B", schema), 
								experience("test3.C", schema),
								experience("test4.B", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema), experience("test4.B", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.B", schema), 
								experience("test3.C", schema),
								experience("test5.B", schema),
								experience("test4.B", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema), experience("test3.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.A", schema), 
								experience("test3.C", schema),
								experience("test5.B", schema),
								experience("test4.B", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema), experience("test3.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.A", schema), 
								experience("test3.C", schema),
								experience("test5.B", schema),
								experience("test4.A", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema), experience("test3.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.A", schema), 
								experience("test3.C", schema),
								experience("test5.A", schema),
								experience("test4.A", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C", schema), 
								experience("test1.C", schema), 
								experience("test2.B", schema), 
								experience("test3.C", schema),
								experience("test5.A", schema),
								experience("test4.A", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C", schema), 
								experience("test1.C", schema), 
								experience("test2.B", schema), 
								experience("test3.C", schema),
								experience("test5.A", schema),
								experience("test4.C", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema), experience("test4.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C", schema), 
								experience("test1.C", schema), 
								experience("test2.B", schema), 
								experience("test3.C", schema),
								experience("test5.B", schema),
								experience("test4.C", schema)));
		assertEquals(VariantCollectionsUtils.list(experience("test1.C", schema), experience("test3.C", schema)), subVector);

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C", schema), 
								experience("test1.A", schema), 
								experience("test2.B", schema), 
								experience("test3.A", schema),
								experience("test5.B", schema),
								experience("test4.C", schema)));
		assertTrue(subVector.isEmpty());

		subVector = 
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test6.C", schema), 
								experience("test2.B", schema), 
								experience("test5.B", schema),
								experience("test4.C", schema)));
		assertTrue(subVector.isEmpty());

		//
		// Test targetability
		//
		
		thrown = false;
		try{
			runtimeFacade.isTargetable(
				test5, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),
						experience("test3.B", schema),
						experience("test5.A", schema),
						experience("test6.A", schema)));
		}
		catch (VariantInternalException e) {
			thrown = true;
			assertEquals("Input test [test5] is already targeted", e.getMessage());
		}
		assertTrue(thrown);
		
		thrown = false;
		try{
			runtimeFacade.isTargetable(
				test6, 
				VariantCollectionsUtils.list(
						experience("test1.C", schema), 
						experience("test2.A", schema), 
						experience("test3.C", schema),
						experience("test5.B", schema),
						experience("test4.A", schema)));
		}
		catch (VariantInternalException e) {
			thrown = true;
			assertEquals("Input set [test1.C,test2.A,test3.C,test5.B,test4.A] is already unresolvable", e.getMessage());
		}
		assertTrue(thrown);

		assertTrue(runtimeFacade.isTargetable(
				test1, 
				VariantCollectionsUtils.list(experience("test2.A", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test1, 
				VariantCollectionsUtils.list(experience("test2.B", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test2.A", schema), 
						experience("test3.C", schema),
						experience("test5.A", schema),
						experience("test4.A", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test2.B", schema), 
						experience("test3.C", schema),
						experience("test5.A", schema),
						experience("test4.A", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test2,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test3.C", schema),
						experience("test5.A", schema),
						experience("test4.A", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test3,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test2.B", schema), 
						experience("test5.A", schema),
						experience("test4.A", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test6,
				VariantCollectionsUtils.list(
						experience("test2.B", schema), 
						experience("test3.C", schema),
						experience("test5.A", schema),
						experience("test4.A", schema))));
		
		assertFalse(runtimeFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test2.B", schema), 
						experience("test3.C", schema),
						experience("test5.A", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test6,
				VariantCollectionsUtils.list(
						experience("test1.A", schema), 
						experience("test2.B", schema), 
						experience("test3.A", schema),
						experience("test5.B", schema),
						experience("test4.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test2,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test1.A", schema), 
						experience("test3.A", schema),
						experience("test5.B", schema),
						experience("test4.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test4,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test1.A", schema), 
						experience("test2.B", schema), 
						experience("test3.A", schema),
						experience("test5.B", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test5,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test1.B", schema), 
						experience("test2.A", schema), 
						experience("test3.A", schema),
						experience("test4.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test3,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test2.B", schema), 
						experience("test5.B", schema),
						experience("test4.C", schema))));

		
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
			    	   
			    	    "   'states':[                                                             \n" +
			    	    "     {  'name':'state1',                                                  \n" +
			    	    "        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state1'                                          \n" +
			    	    "        }                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {  'NAME':'state2',                                                  \n" +
			    	    "        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state2'                                          \n" +
			    	    "        }                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {  'nAmE':'state3',                                                  \n" +
			    	    "        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state3'                                          \n" +
			    	    "        }                                                                \n" +
			    	    "     },                                                                  \n" +
			    	    "     {  'name':'state4',                                                  \n" +
			    	    "        'parameters':{                                                    \n" +
			    	    "           'path':'/path/to/state4'                                          \n" +
			    	    "        }                                                                \n" +
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
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                         \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.B'                      \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.C'                      \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                         \n" +
			    	    "              'isNonvariant':true                                         \n" +
			    	    "           },                                                             \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                         \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state3/test1.B'                      \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state3/test1.C'                      \n" +
			    	    "                    }                                                    \n" +
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
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state3',                                         \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state3/test2.B'                      \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state3/test2.C'                      \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state2',                                         \n" +
			    	    "              'isNonvariant':false,                                       \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state2/test2.B'                      \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state2/test2.C'                      \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           },                                                            \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state4',                                         \n" +
			    	    "              'isNonvariant':true                                         \n" +
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
			    	    "        'onStates':[                                                      \n" +
			    	    "           {                                                             \n" +
			    	    "              'stateRef':'state1',                                         \n" +
			    	    "              'variants':[                                               \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test3.B'                      \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test3.C'                      \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.B+test3.B'              \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'B',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.C+test3.B'              \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'B'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.B+test3.C'              \n" +
			    	    "                    }                                                    \n" +
			    	    "                 },                                                      \n" +
			    	    "                 {                                                       \n" +
			    	    "                    'experienceRef':'C',                                 \n" +
			    	    "                    'covariantExperienceRefs': [                         \n" +
			    	    "                       {                                                 \n" +
			    	    "                          'testRef': 'test1',                            \n" +
			    	    "                          'experienceRef': 'C'                           \n" +
			    	    "                       }                                                 \n" +
			    	    "                     ],                                                  \n" +
			    	    "                    'parameters':{                                       \n" +
			    	    "                       'path':'/path/to/state1/test1.C+test3.C'              \n" +
			    	    "                    }                                                    \n" +
			    	    "                 }                                                       \n" +
			    	    "              ]                                                          \n" +
			    	    "           }                                                             \n" +
			    	    "        ]                                                                \n" +
			    	    "     }                                                                   \n" +
			    	    "  ]                                                                      \n" +
			    	    "}                                                                         ";


		ParserResponse response = core.parseSchema(SCHEMA);
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		Schema schema = core.getSchema();
		State state1 = schema.getState("state1");
		VariantCoreSession ssn = core.getSession("foo-key");
		setTargetingStabile(ssn, "test2.B");
		VariantCoreStateRequest req = ssn.targetForState(state1);
		SessionScopedTargetingStabile stabile = ((CoreSessionImpl)ssn).getTargetingStabile();

		// test2 is off, but stabile has a variant experience for it, which will be substituted for the purposes of lookup with control.
		// test1 is disjoint with test2, so has to default to control.
		// test3 is covariant with test1, so it is targeted unconstrained.
		assertEquals(3, stabile.getAll().size());
		// We didn't touch test2.B entry in the TP, even though we used control for resolution.
		assertEquals("test2.B", stabile.get("test2").getTestName() + "." + stabile.get("test2").getExperienceName());
		String path = req.getResolvedParameterMap().get("path");
		assertMatches("/path/to/state1(/test3\\.[B,C])?", path);
		assertEquals(ssn, req.getSession());
		assertEquals(state1, req.getState());
		
		// View Serve Event.
		assertNotNull(req.getStateVisitedEvent());
	}
	
}

