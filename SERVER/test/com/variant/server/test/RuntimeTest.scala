package com.variant.server.test;

import com.variant.core.HookListener
import com.variant.core.schema.StateParsedHook
import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.server.schema.SchemaDeployer
import com.variant.core.schema.TestParsedHook
import com.variant.core.schema.Test
import com.variant.core.TestQualificationHook
import com.variant.server.session.ServerSession
import org.scalatest.Assertions._
import com.variant.core.TestTargetingHook
import com.variant.server.boot.ServerErrorLocal._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import com.variant.server.runtime.Runtime
import com.variant.server.runtime.RuntimeTestFacade
import com.variant.server.ServerException

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class RuntimeTest extends BaseSpecWithServer {
   
	"Runtime" should {
	   "do these things" in {
	      
   		val response = server.installSchemaDeployer(SchemaDeployer.fromClasspath("/ParserCovariantOkayBigTest.json")).get
      	response.hasMessages() mustBe false		
   		server.schema.isDefined mustBe true
   	   val schema = server.schema.get
   		val state1 = schema.getState("state1")
   	   val test1 = schema.getTest("test1")
   	   val test2 = schema.getTest("test2")
   	   val test3 = schema.getTest("test3")
   	   val test4 = schema.getTest("test4")
   	   val test5 = schema.getTest("test5")
   	   val test6 = schema.getTest("test6")

   	   val runtime = RuntimeTestFacade(server)
   	   
   	   var caughtEx = intercept[ServerException.Internal] {
             runtime.resolveState(state1, Array(experience("test1.B", schema)))
         }
         assert(
            caughtEx.getMessage.equals(
               new ServerException.Internal("Uninstrumented test [test1.B] in input vector").getMessage)
         )
         
         var resolution = runtime.resolveState(state1, Array(experience("test2.B", schema)))   // nonvariant.
         resolution mustBe (true, null)

         caughtEx = intercept[ServerException.Internal] {
             runtime.resolveState(state1, Array(experience("test1.A", schema), experience("test2.B", schema)))
         }
         assert(
            caughtEx.getMessage.equals(
               new ServerException.Internal("Uninstrumented test [test1.A] in input vector").getMessage)
         )

         resolution = runtime.resolveState(
			   state1, 
				Array(experience("test2.B", schema),  // nonvariant
						experience("test3.C", schema)   // nonvariant
				)
		   )
		   resolution mustBe (true, null)

		   caughtEx = intercept[ServerException.Internal] {
				runtime.resolveState(
					state1,	 
					Array(
						experience("test2.B", schema),  // nonvariant
						experience("test3.C", schema),  // nonvariant
						experience("test2.C", schema)   // dupe test
					)
				)
			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Duplicate test [test2] in input vector").getMessage)
         )
         
         resolution = runtime.resolveState(
				   state1, 
				   Array(
				   	experience("test2.B", schema),  // nonvariant
					   experience("test3.C", schema),  // nonvariant
						experience("test4.B", schema)   // variant
				)
		   )
		   resolution._1 mustBe true
		   resolution._2.getParameter("path") mustBe "/path/to/state1/test4.B"

         caughtEx = intercept[ServerException.Internal] {
				runtime.resolveState(
						state1, 
						Array(
							experience("test4.B", schema),  // variant
							experience("test2.B", schema),  // nonvariant
							experience("test1.C", schema),  // not instrumented
							experience("test3.C", schema)   // nonvariant
						)
				)
			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Uninstrumented test [test1.C] in input vector").getMessage)
         )

         
	   }
   }
}

/*		
		
		
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
						state1,
						VariantCollectionsUtils.list(
								experience("test5.A", schema),
								experience("test2.A", schema)));
			}
		}.assertThrown("Input test [test5] is already targeted");


		new VariantInternalExceptionInterceptor() { 
			@Override public void toRun() {
				runtimeFacade.isTargetable(
					test6, 
					state1,
					VariantCollectionsUtils.list(
							experience("test1.C", schema), 
							experience("test3.C", schema),
							experience("test5.B", schema)));
			}
		}.assertThrown("Input vector [test1.C,test3.C,test5.B] is already unresolvable");

		assertFalse(runtimeFacade.isTargetable(
				test1, 
				state1,
				VariantCollectionsUtils.list(experience("test2.B", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test1,
				state1,
				VariantCollectionsUtils.list(
						experience("test5.B", schema),
						experience("test4.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test1,
				state1,
				VariantCollectionsUtils.list(
						experience("test6.B", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test1,
				state1,
				VariantCollectionsUtils.list(
						experience("test4.C", schema),
						experience("test6.B", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test1,
				state1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema),
						experience("test5.B", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test1,
				state1,
				VariantCollectionsUtils.list(
						experience("test4.C", schema),
						experience("test5.C", schema),
						experience("test6.B", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test2,
				state1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test5.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test2,
				state1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test3.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test2,
				state1,
				VariantCollectionsUtils.list(
						experience("test5.C", schema), 
						experience("test4.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test2,
				state1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test2,
				state1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		// Targetable because 6 and 3 are logically disjoint, i.e.
		// even though some states are instrumebnted by both, none is
		// a variantful instrumentation.
		assertTrue(runtimeFacade.isTargetable(
				test3,
				state1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test2.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test5,
				state1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		assertFalse(runtimeFacade.isTargetable(
				test5,
				state1,
				VariantCollectionsUtils.list(
						experience("test6.C", schema), 
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test5,
				state1,
				VariantCollectionsUtils.list(
						experience("test2.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test6,
				state1,
				VariantCollectionsUtils.list(
						experience("test4.C", schema), 
						experience("test1.C", schema))));

		assertTrue(runtimeFacade.isTargetable(
				test6,
				state1,
				VariantCollectionsUtils.list(
						experience("test5.C", schema), 
						experience("test2.C", schema))));

		// See comment about test3 and test6 above
		assertTrue(runtimeFacade.isTargetable(
				test6,
				state1,
				VariantCollectionsUtils.list(
						experience("test3.C", schema))));

		// Ditto
		assertTrue(runtimeFacade.isTargetable(
				test6,
				state1,
				VariantCollectionsUtils.list(
						experience("test3.C", schema), 
						experience("test2.C", schema))));

*/