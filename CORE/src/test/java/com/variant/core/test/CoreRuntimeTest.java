package com.variant.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Random;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.impl.util.VariantCollectionsUtils;
import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.TestQualificationHook;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;
import com.variant.core.impl.VariantRuntimeTestFacade;
import com.variant.core.schema.ParserResponse;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.xdm.Schema;
import com.variant.core.xdm.State;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;
import com.variant.core.xdm.impl.StateVariantImpl;


/**
 * @author Igor
 */
public class CoreRuntimeTest extends BaseTestCore {

	private VariantCore core = rebootApi();
	private Random rand = new Random();
	
	/**
	 * 
	 */
	@org.junit.Test
	public void pathResolution() throws Exception {
		
		final VariantRuntimeTestFacade runtimeFacade = new VariantRuntimeTestFacade(core.getRuntime());
		
		ParserResponse response = core.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());

		final Schema schema = core.getSchema();
		final Test test1 = schema.getTest("test1");
		final Test test2 = schema.getTest("test2");
		final Test test3 = schema.getTest("test3");
		//final Test test4 = schema.getTest("test4");
		final Test test5 = schema.getTest("test5");
		final Test test6 = schema.getTest("test6");

		final State state1 = schema.getState("state1");
		final State state2 = schema.getState("state2");
		final State state3 = schema.getState("state3");
		//final State state4 = schema.getState("state4");
		//final State state5 = schema.getState("state5");

		//
		// State resolutions
		//

		// state1
		
		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.resolveState(
						state1, 
						VariantCollectionsUtils.list(
								experience("test1.B", schema)   // not instrumented
						)
				);
			}
		}.assertThrown("Uninstrumented test [test1.B] in input vector");

		Pair<Boolean, StateVariantImpl> resolution = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema)   // nonvariant.
				)
		);
		assertTrue(resolution.arg1());
		assertNull(resolution.arg2());

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.resolveState(
						state1, 
						VariantCollectionsUtils.list(
							experience("test1.A", schema),  // control
							experience("test2.B", schema)   // nonvariant
						)
				);
			}
		}.assertThrown("Uninstrumented test [test1.A] in input vector");

		resolution = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema),  // nonvariant
						experience("test3.C", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());
		assertNull(resolution.arg2());

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.resolveState(
						state1,	 
						VariantCollectionsUtils.list(
							experience("test2.B", schema),  // nonvariant
							experience("test3.C", schema),  // nonvariant
							experience("test2.C", schema)   // dupe test
						)
				);
			}
		}.assertThrown("Duplicate test [test2] in input vector");	
		
		
		resolution = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema),  // nonvariant
						experience("test3.C", schema),  // nonvariant
						experience("test4.B", schema)   // variant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state1/test4.B", resolution.arg2().getParameter("path"));
		
		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.resolveState(
						state1, 
						VariantCollectionsUtils.list(
							experience("test4.B", schema),  // variant
							experience("test2.B", schema),  // nonvariant
							experience("test1.C", schema),  // not instrumented
							experience("test3.C", schema)   // nonvariant
						)
				);
			}
		}.assertThrown("Uninstrumented test [test1.C] in input vector");

		resolution = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test6.C", schema),  // variant
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state1/test4.B+test6.C", resolution.arg2().getParameter("path"));

		resolution = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test6.B", schema),  // variant
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state1/test4.B+test6.B", resolution.arg2().getParameter("path"));

		resolution = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state1/test4.C+test5.C", resolution.arg2().getParameter("path"));

		resolution = runtimeFacade.resolveState(
				state1, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant
						experience("test6.B", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());		
		assertEquals("/path/to/state1/test4.C+test5.C+test6.B", resolution.arg2().getParameter("path"));

		
		// state2

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema)   // control
				)
		);
		assertTrue(resolution.arg1());
		assertNull(resolution.arg2());

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.A", schema),  // control
						experience("test2.B", schema)   // variant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state2/test2.B", resolution.arg2().getParameter("path"));
		assertEquals("/path/to/state2/test2.B", resolution.arg2().getParameter("PATH"));

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.B", schema)   // variant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state2/test1.B", resolution.arg2().getParameter("path"));

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test6.C", schema),  // nonvariant
						experience("test2.B", schema)   // variant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state2/test2.B", resolution.arg2().getParameter("path"));

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.resolveState(
					state2, 
					VariantCollectionsUtils.list(
							experience("test2.B", schema),  // variant
							experience("test6.C", schema),  // nonvariant
							experience("test2.C", schema)   // dupe test
					)
				);
			}
		}.assertThrown("Duplicate test [test2] in input vector");
		
		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant
						experience("test1.C", schema),  // variant
						experience("test6.C", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state2/test1.C+test4.B", resolution.arg2().getParameter("path"));

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test1.B", schema),  // variant
						experience("test2.B", schema),  // variant
						experience("test6.B", schema),  // invariant
						experience("test4.B", schema)   // variant, unsupported
				)
		);
		assertFalse(resolution.arg1());
		
		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.B", schema),  // variant, unsupported
						experience("test2.B", schema)   // variant
				)
		);
		assertFalse(resolution.arg1());

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // variant, unsupported.
				)
		);
		assertFalse(resolution.arg1());

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant
						experience("test5.C", schema)   // variant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state2/test4.C+test5.C", resolution.arg2().getParameter("path"));

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant
						experience("test2.B", schema),  // variant
						experience("test5.C", schema)   // variant, unsupported
				)
		);
		assertFalse(resolution.arg1());

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant unsupported
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // variant
						experience("test3.B", schema)   // variant
				)
		);
		assertFalse(resolution.arg1());

		resolution = runtimeFacade.resolveState(
				state2, 
				VariantCollectionsUtils.list(
						experience("test4.C", schema),  // variant unsupported
						experience("test6.B", schema),  // nonvariant
						experience("test2.B", schema),  // variant 
						experience("test5.C", schema)   // variant
				)
		);
		assertFalse(resolution.arg1());

		// state3

		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test2.A", schema)   // control
				)
		);
		assertTrue(resolution.arg1());
		assertNull(resolution.arg2());
		
		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema) 
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state3/test2.B", resolution.arg2().getParameter("path"));

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.resolveState(
						state3, 
						VariantCollectionsUtils.list(
								experience("test4.B", schema)   // not instrumented
						)
				);
			}
		}.assertThrown("Uninstrumented test [test4.B] in input vector");

		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());
		assertNull(resolution.arg2());

		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema)   // variant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state3/test2.B", resolution.arg2().getParameter("path"));

		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test3.B", schema),  // nonvariant
						experience("test5.C", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());
		assertNull(resolution.arg2());

		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema),  // variant
						experience("test3.C", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state3/test2.B", resolution.arg2().getParameter("path"));

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
					runtimeFacade.resolveState(
							state3, 
							VariantCollectionsUtils.list(
									experience("test2.B", schema),  // variant
									experience("test3.A", schema),  // nonvariant
									experience("test2.C", schema)   // dupe test
							)
					);
			}
		}.assertThrown("Duplicate test [test2] in input vector");

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.resolveState(
						state3, 
						VariantCollectionsUtils.list(
								experience("test1.C", schema),  // variant
								experience("test2.B", schema),  // variant, unsupported
								experience("test3.B", schema),  // nonvariant
								experience("test4.B", schema)   // uninstrumented
						)
				);
			}
		}.assertThrown("Uninstrumented test [test4.B] in input vector");
		
		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test1.C", schema),  // variant
						experience("test2.B", schema),  // variant, unsupported
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertFalse(resolution.arg1());
		
		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema),  // variant
						experience("test3.C", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state3/test2.B", resolution.arg2().getParameter("path"));

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.resolveState(
						state3, 
						VariantCollectionsUtils.list(
								experience("test4.B", schema),  // uninstrumented
								experience("test1.C", schema),  // variant
								experience("test6.C", schema),  // variant
								experience("test3.B", schema)   // nonvariant
						)
				);
			}
		}.assertThrown("Uninstrumented test [test4.B] in input vector");
		

		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema),  // variant unsupported
						experience("test1.C", schema),  // variant
						experience("test6.B", schema),  // variant
						experience("test3.C", schema)   // nonvariant
				)
		);
		assertFalse(resolution.arg1());

		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test2.B", schema),  // variant
						experience("test5.C", schema),  // nonvariant
						experience("test3.B", schema)   // nonvariant
				)
		);
		assertEquals("/path/to/state3/test2.B", resolution.arg2().getParameter("path"));

		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test6.C", schema),  // control
						experience("test2.B", schema),  // variant, unsupported
						experience("test5.C", schema),  // nonvariant
						experience("test1.B", schema)   // variant
				)
		);
		assertFalse(resolution.arg1());

		resolution = runtimeFacade.resolveState(
				state3, 
				VariantCollectionsUtils.list(
						experience("test6.B", schema),  // variant
						experience("test2.B", schema),  // variant
						experience("test5.C", schema)   // nonvariant
				)
		);
		assertTrue(resolution.arg1());
		assertEquals("/path/to/state3/test2.B+test6.B", resolution.arg2().getParameter("path"));
		
		//
		// Coordinates resolvability
		//

		assertTrue(
				runtimeFacade.isResolvable(VariantCollectionsUtils.list(
						experience("test1.A", schema))));
		
		assertTrue(
				runtimeFacade.isResolvable(VariantCollectionsUtils.list(
						experience("test1.B", schema))));

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.B", schema)),
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test2.B", schema)));
			}
		}.assertThrown("Input vector [test1.C,test2.B] must be resolvable, but is not");

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema)),
						VariantCollectionsUtils.list(
								experience("test1.B", schema), 
								experience("test2.B", schema)));
			}
		}.assertThrown("Experience [test1.B] in second argument contradicts experience [test1.C] in first argument");

		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test2.B", schema), 
								experience("test3.C", schema)),
						VariantCollectionsUtils.list(
								experience("test1.A", schema), 
								experience("test3.B", schema)));
			}
		}.assertThrown("Experience [test3.B] in second argument contradicts experience [test3.C] in first argument");

		Collection<Experience> subVector = runtimeFacade.minUnresolvableSubvector(
				VariantCollectionsUtils.list(
						experience("test1.A", schema), 
						experience("test2.B", schema)),
				VariantCollectionsUtils.list(
						experience("test3.C", schema), 
						experience("test4.B", schema)));
		assertEqualAsSets(VariantCollectionsUtils.list(experience("test4.B", schema)), subVector);
		
		subVector = runtimeFacade.minUnresolvableSubvector(
				VariantCollectionsUtils.list(
						experience("test2.B", schema)),
				VariantCollectionsUtils.list(
						experience("test3.A", schema), 
						experience("test4.B", schema)));
		assertEqualAsSets(VariantCollectionsUtils.list(experience("test4.B", schema)), subVector);

		subVector =
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test2.B", schema)),
						VariantCollectionsUtils.list(
								experience("test1.B", schema))
						);
		assertEqualAsSets(VariantCollectionsUtils.list(experience("test1.B", schema)), subVector);

		subVector =
				runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test2.B", schema), 
								experience("test3.C", schema)),	
						VariantCollectionsUtils.list(
								experience("test1.B", schema))
				);
		assertEqualAsSets(VariantCollectionsUtils.list(experience("test1.B", schema)), subVector);

		subVector = runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test3.C", schema)),
						VariantCollectionsUtils.list(
								experience("test2.B", schema))
				);
		assertTrue(subVector.isEmpty());

		subVector = runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test3.C", schema)),
						VariantCollectionsUtils.list(
								experience("test2.B", schema),
								experience("test5.C", schema))
				);
		assertEqualAsSets(VariantCollectionsUtils.list(experience("test5.C", schema)), subVector);

		subVector = runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test2.C", schema)),
						VariantCollectionsUtils.list(
								experience("test1.C", schema), 
								experience("test3.C", schema)));
		assertEqualAsSets(VariantCollectionsUtils.list(experience("test1.C", schema)), subVector);

		subVector = runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test1.C", schema)),
						VariantCollectionsUtils.list(
								experience("test4.B", schema), 
								experience("test5.C", schema)));
		assertEqualAsSets(VariantCollectionsUtils.list(experience("test5.C", schema)), subVector);

		subVector = runtimeFacade.minUnresolvableSubvector(
						VariantCollectionsUtils.list(
								experience("test2.C", schema)),
						VariantCollectionsUtils.list(
								experience("test4.B", schema), 
								experience("test5.C", schema)));
		assertEqualAsSets(VariantCollectionsUtils.list(experience("test4.B", schema)), subVector);

		subVector = runtimeFacade.minUnresolvableSubvector(
				VariantCollectionsUtils.list(
						experience("test2.C", schema)),
				VariantCollectionsUtils.list(
						experience("test3.B", schema), 
						experience("test5.C", schema)));
		assertEqualAsSets(VariantCollectionsUtils.list(experience("test5.C", schema)), subVector);

		subVector = runtimeFacade.minUnresolvableSubvector(
				VariantCollectionsUtils.list(
						experience("test1.C", schema),
						experience("test4.C", schema)),
				VariantCollectionsUtils.list(
						experience("test3.B", schema), 
						experience("test5.C", schema)));
		assertEqualAsSets(
				VariantCollectionsUtils.list(
						experience("test3.B", schema), 
						experience("test5.C", schema)), 
				subVector);

		subVector = runtimeFacade.minUnresolvableSubvector(
				VariantCollectionsUtils.list(
						experience("test2.C", schema),
						experience("test3.C", schema)),
				VariantCollectionsUtils.list(
						experience("test1.C", schema)));
		assertEqualAsSets(
				VariantCollectionsUtils.list(
						experience("test1.C", schema)), 
				subVector);
		
		subVector = runtimeFacade.minUnresolvableSubvector(
				VariantCollectionsUtils.list(
						experience("test2.C", schema),
						experience("test3.C", schema)),
				VariantCollectionsUtils.list(
						experience("test1.C", schema), 
						experience("test4.C", schema)));
		assertEqualAsSets(
				VariantCollectionsUtils.list(
						experience("test1.C", schema), 
						experience("test4.C", schema)), 
				subVector);
		
		subVector = runtimeFacade.minUnresolvableSubvector(
				VariantCollectionsUtils.list(
						experience("test6.C", schema),
						experience("test1.B", schema)),
				VariantCollectionsUtils.list(
						experience("test5.C", schema), 
						experience("test4.C", schema)));
		assertEqualAsSets(
				VariantCollectionsUtils.list(
						experience("test5.C", schema)), 
				subVector);
		
		subVector = runtimeFacade.minUnresolvableSubvector(
				VariantCollectionsUtils.list(
						experience("test5.C", schema),
						experience("test4.B", schema)),
				VariantCollectionsUtils.list(
						experience("test1.C", schema), 
						experience("test2.C", schema)));
		assertEqualAsSets(
				VariantCollectionsUtils.list(
						experience("test1.C", schema), 
						experience("test2.C", schema)),
				subVector);
		
		//
		// Test targetability
		//
		
		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.isTargetable(
						test5, 
						VariantCollectionsUtils.list(
								experience("test5.A", schema),
								experience("test2.A", schema)));
			}
		}.assertThrown("Input test [test5] is already targeted");


		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.isTargetable(
					test6, 
					VariantCollectionsUtils.list(
							experience("test1.C", schema), 
							experience("test3.C", schema),
							experience("test5.B", schema)));
			}
		}.assertThrown("Input vector [test1.C,test3.C,test5.B] is already unresolvable");

		assertFalse(runtimeFacade.isTargetable(
				test1, 
				VariantCollectionsUtils.list(experience("test2.B", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test5.B", schema),
						experience("test4.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test6.B", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test4.C", schema),
						experience("test6.B", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema),
						experience("test5.B", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test1,
				VariantCollectionsUtils.list(
						experience("test4.C", schema),
						experience("test5.C", schema),
						experience("test6.B", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test2,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test5.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test2,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test3.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test2,
				VariantCollectionsUtils.list(
						experience("test5.C", schema), 
						experience("test4.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test2,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test2,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		// Targetable because 6 and 3 are logically disjoint, i.e.
		// even though some states are instrumebnted by both, none is
		// a variantful instrumentation.
		assertTrue(runtimeFacade.isTargetable(
				test3,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test2.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test5,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test5,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test5,
				VariantCollectionsUtils.list(
						experience("test2.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test6,
				VariantCollectionsUtils.list(
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test6,
				VariantCollectionsUtils.list(
						experience("test5.C", schema), 
						experience("test2.C", schema))));

		// See comment about test3 and test6 above
		assertTrue(runtimeFacade.isTargetable(
				test6,
				VariantCollectionsUtils.list(
						experience("test3.C", schema))));

		// Ditto
		assertTrue(runtimeFacade.isTargetable(
				test6,
				VariantCollectionsUtils.list(
						experience("test3.C", schema), 
						experience("test2.C", schema))));

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
		String sessionId = VariantStringUtils.random64BitString(rand);
		VariantCoreSession ssn = core.getSession(sessionId, true).getBody();
		setTargetingStabile(ssn, "test2.B");
		VariantCoreStateRequest req = ssn.targetForState(state1);
		SessionScopedTargetingStabile stabile = ((CoreSessionImpl)ssn).getTargetingStabile();

		// test2 is off, but stabile has a variant experience for it, which will be substituted for the purposes of lookup with control.
		// test3 is covariant with test1, so it is targeted unconstrained.
		assertEquals(3, stabile.getAll().size());
		// We didn't touch test2.B entry in the TP, even though we used control for resolution.
		assertEquals("test2.B", stabile.get("test2").getTestName() + "." + stabile.get("test2").getExperienceName());
		String path = req.getResolvedParameter("path");
		assertMatches("/path/to/state1(/test[1,3]\\.[B,C])?(\\+test[1,3]\\.[B,C])?", path);
		assertEquals(ssn, req.getSession());
		assertEquals(state1, req.getState());
		
		// View Serve Event.
		assertNotNull(req.getStateVisitedEvent());
	}

	/**
	 * 
	 */
	@org.junit.Test
	public void disqualifyAllTestsTest() {
				
		ParserResponse response = core.parseSchema(openResourceAsInputStream("/schema/ParserCovariantOkayBigTest.json"));
		if (response.hasMessages()) printMessages(response);
		assertFalse(response.hasMessages());
		
		core.clearHookListeners();
		core.addHookListener(new DisqualAllHookListener());
		
		String sessionId = VariantStringUtils.random64BitString(rand);
		VariantCoreSession ssnIn = core.getSession(sessionId, true).getBody();
		assertTrue(ssnIn.getTraversedStates().isEmpty());
		assertTrue(ssnIn.getTraversedTests().isEmpty());
		assertNull(ssnIn.getStateRequest());
	    	      	    	      
		for (String stateName: new String[] {"state1", "state2", "state3", "state4", "state5"}) {
	    	         
			VariantCoreStateRequest req = ssnIn.targetForState(core.getSchema().getState(stateName));
			CoreSessionImpl ssn = (CoreSessionImpl) req.getSession();
			assertEquals(ssn, ssnIn);
			assertTrue(ssn.getTraversedStates().isEmpty());
			assertTrue(ssn.getTraversedTests().isEmpty());
			assertEquals(req, ssn.getStateRequest());
			req.commit();
			assertTrue(ssn.getTraversedStates().isEmpty());
			assertTrue(ssn.getTraversedTests().isEmpty());
			assertEquals(req, ssn.getStateRequest());
		}
	}
	    
	private static class DisqualAllHookListener implements HookListener<TestQualificationHook> {

		@Override
		public Class<TestQualificationHook> getHookClass() {
			return TestQualificationHook.class;
		}

		@Override
		public void post(TestQualificationHook hook) {
			hook.setQualified(false);
		}
		
	}
}

