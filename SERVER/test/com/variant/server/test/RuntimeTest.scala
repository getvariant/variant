package com.variant.server.test;

import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.core.schema.Test
import org.scalatest.Assertions._
import com.variant.server.boot.ServerErrorLocal._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import com.variant.server.boot.RuntimeTestFacade
import com.variant.server.api.ServerException
import scala.collection.mutable.ArrayBuffer
import com.variant.server.schema.SchemaDeployerClasspath

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class RuntimeTest extends BaseSpecWithServer {
   
	"Runtime" should {
	   
       val schemaDeployer = SchemaDeployerClasspath("/ParserCovariantOkayBigTestNoHooks.json")
       server.useSchemaDeployer(schemaDeployer)
       val response = schemaDeployer.parserResponses(0)
       response.hasMessages() mustBe false		
       server.schemata.get("ParserCovariantOkayBigTestNoHooks").isDefined mustBe true
   	 val schema = server.schemata.get("ParserCovariantOkayBigTestNoHooks").get
 	    val runtime = RuntimeTestFacade(schema)

   	val state1 = schema.getState("state1")
   	val state2 = schema.getState("state2")
   	val state3 = schema.getState("state3")
   	val test1 = schema.getTest("test1")
   	val test2 = schema.getTest("test2")
   	val test3 = schema.getTest("test3")
   	val test4 = schema.getTest("test4")
   	val test5 = schema.getTest("test5")
   	val test6 = schema.getTest("test6")

	   "resolve for state1" in {
	         	   
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
		   resolution._2.getParameters.get("path") mustBe "/path/to/state1/test4.B"

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

		   resolution = runtime.resolveState(
				state1,
				Array(
						experience("test4.B", schema),  // variant
						experience("test2.B", schema),  // nonvariant
						experience("test6.C", schema),  // variant
						experience("test3.B", schema)   // nonvariant
				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state1/test4.B+test6.C"
            
   		resolution = runtime.resolveState(
   				state1, 
   				Array(
   						experience("test4.B", schema),  // variant
   						experience("test2.B", schema),  // nonvariant
   						experience("test6.B", schema),  // variant
   						experience("test3.B", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state1/test4.B+test6.B"
   
   		resolution = runtime.resolveState(
   				state1, 
   				Array(
   						experience("test4.C", schema),  // variant
   						experience("test2.B", schema),  // nonvariant
   						experience("test5.C", schema),  // variant
   						experience("test3.B", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state1/test4.C+test5.C"
   
   		resolution = runtime.resolveState(
   				state1, 
   				Array(
   						experience("test4.C", schema),  // variant
   						experience("test6.B", schema),  // variant
   						experience("test2.B", schema),  // nonvariant
   						experience("test5.C", schema),  // variant
   						experience("test3.B", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state1/test4.C+test5.C+test6.B"
   		
	   }
	   
	   "should resolve for state2" in {
   		
	      var resolution = runtime.resolveState(
				   state2, 
				   Array(
						experience("test1.A", schema)   // control
				   )
		   );
		   resolution._1 mustBe true
		   resolution._2 mustBe null

   		resolution = runtime.resolveState(
   				state2, 
	   			Array(
						experience("test1.A", schema),  // control
						experience("test2.B", schema)   // variant
				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state2/test2.B"
   		resolution._2.getParameters().get("PATH") mustBe null // path is case sensitive.

       	resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test1.B", schema)   // variant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state2/test1.B"
   		resolution._2.getParameters().get("PaTh") mustBe null // path is case sensitive.
   
   		resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test6.C", schema),  // nonvariant
   						experience("test2.B", schema)   // variant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state2/test2.B"
   		
         var caughtEx = intercept[ServerException.Internal] {
 				runtime.resolveState(
   					state2, 
   					Array(
   							experience("test2.B", schema),  // variant
   							experience("test6.C", schema),  // nonvariant
   							experience("test2.C", schema)   // dupe test
   					)
   			)
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Duplicate test [test2] in input vector").getMessage)
         )

        resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test4.B", schema),  // variant
   						experience("test1.C", schema),  // variant
   						experience("test6.C", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state2/test1.C+test4.B"
   
   		resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test1.B", schema),  // variant
   						experience("test2.B", schema),  // variant
   						experience("test6.B", schema),  // invariant
   						experience("test4.B", schema)   // variant, unsupported
   				)
   		);
   		resolution._1 mustBe false
   		
   		resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test4.B", schema),  // variant, unsupported
   						experience("test2.B", schema)   // variant
   				)
   		);
   		resolution._1 mustBe false
   
   		resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test2.B", schema),  // variant
   						experience("test5.C", schema),  // variant
   						experience("test3.B", schema)   // variant, unsupported.
   				)
   		);
   		resolution._1 mustBe false
   
   		resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test4.C", schema),  // variant
   						experience("test5.C", schema)   // variant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state2/test4.C+test5.C"
   
   		resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test4.C", schema),  // variant
   						experience("test2.B", schema),  // variant
   						experience("test5.C", schema)   // variant, unsupported
   				)
   		);
   		resolution._1 mustBe false
   
   		resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test4.C", schema),  // variant unsupported
   						experience("test2.B", schema),  // variant
   						experience("test5.C", schema),  // variant
   						experience("test3.B", schema)   // variant
   				)
   		);
   		resolution._1 mustBe false
   
   		resolution = runtime.resolveState(
   				state2, 
   				Array(
   						experience("test4.C", schema),  // variant unsupported
   						experience("test6.B", schema),  // nonvariant
   						experience("test2.B", schema),  // variant 
   						experience("test5.C", schema)   // variant
   				)
   		);
   		resolution._1 mustBe false
	   }
	   
	   "resolve for state3" in {

   		var resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.A", schema)   // control
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2 mustBe null
   		
   		resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema) 
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state3/test2.B"
      
         var caughtEx = intercept[ServerException.Internal] {
   				runtime.resolveState(
   						state3, 
   						Array(
   								experience("test4.B", schema)   // not instrumented
   						)
   				)
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Uninstrumented test [test4.B] in input vector").getMessage)
         )

         resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test3.B", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2 mustBe null
   
   		resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema)   // variant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state3/test2.B"
   
   		resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test3.B", schema),  // nonvariant
   						experience("test5.C", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2 mustBe null
   
   		resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema),  // variant
   						experience("test3.C", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state3/test2.B"
   
         caughtEx = intercept[ServerException.Internal] {
   					runtime.resolveState(
   							state3, 
   							Array(
   									experience("test2.B", schema),  // variant
   									experience("test3.A", schema),  // nonvariant
   									experience("test2.C", schema)   // dupe test
   							)
   					)
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Duplicate test [test2] in input vector").getMessage)
         )

         caughtEx = intercept[ServerException.Internal] {
   				runtime.resolveState(
   						state3, 
   						Array(
   								experience("test1.C", schema),  // variant
   								experience("test2.B", schema),  // variant, unsupported
   								experience("test3.B", schema),  // nonvariant
   								experience("test4.B", schema)   // uninstrumented
   						)
   				)
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Uninstrumented test [test4.B] in input vector").getMessage)
         )

         resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test1.C", schema),  // variant
   						experience("test2.B", schema),  // variant, unsupported
   						experience("test3.B", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe false
   		
   		resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema),  // variant
   						experience("test3.C", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state3/test2.B"
      		
         caughtEx = intercept[ServerException.Internal] {
   				runtime.resolveState(
   						state3, 
   						Array(
   								experience("test4.B", schema),  // uninstrumented
   								experience("test1.C", schema),  // variant
   								experience("test6.C", schema),  // variant
   								experience("test3.B", schema)   // nonvariant
   						)
   				)
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Uninstrumented test [test4.B] in input vector").getMessage)
         )

   		resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema),  // variant unsupported
   						experience("test1.C", schema),  // variant
   						experience("test6.B", schema),  // variant
   						experience("test3.C", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe false
   
   		resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema),  // variant
   						experience("test5.C", schema),  // nonvariant
   						experience("test3.B", schema)   // nonvariant
   				)
   		)
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state3/test2.B"
   
   		resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test6.C", schema),  // control
   						experience("test2.B", schema),  // variant, unsupported
   						experience("test5.C", schema),  // nonvariant
   						experience("test1.B", schema)   // variant
   				)
   		);
   		resolution._1 mustBe false
   
   		resolution = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test6.B", schema),  // variant
   						experience("test2.B", schema),  // variant
   						experience("test5.C", schema)   // nonvariant
   				)
   		);
   		resolution._1 mustBe true
   		resolution._2.getParameters().get("path") mustBe "/path/to/state3/test2.B+test6.B"   
	   }

	   "resolve these coordinate vectors" in {

         runtime.isResolvable(Array(experience("test1.A", schema))) mustBe true
		
         runtime.isResolvable(Array(experience("test1.B", schema))) mustBe true

         var caughtEx = intercept[ServerException.Internal] {

				runtime.minUnresolvableSubvector(
						Array(
								experience("test1.C", schema), 
								experience("test2.B", schema)),
						Array(
								experience("test1.C", schema), 
								experience("test2.B", schema)))
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Input vector [test1.C,test2.B] must be resolvable, but is not").getMessage)
         )
         
         caughtEx = intercept[ServerException.Internal] {

				runtime.minUnresolvableSubvector(
						Array(
								experience("test1.C", schema)),
						Array(
								experience("test1.B", schema), 
								experience("test2.B", schema)))
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Experience [test1.B] in second argument contradicts experience [test1.C] in first argument").getMessage)
         )

         caughtEx = intercept[ServerException.Internal] {

				runtime.minUnresolvableSubvector(
						Array(
								experience("test2.B", schema), 
								experience("test3.C", schema)),
						Array(
								experience("test1.A", schema), 
								experience("test3.B", schema)))
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Experience [test3.B] in second argument contradicts experience [test3.C] in first argument").getMessage)
         )

         var subVector = runtime.minUnresolvableSubvector(
				Array(
						experience("test1.A", schema), 
						experience("test2.B", schema)),
				Array(
						experience("test3.C", schema), 
						experience("test4.B", schema)))
		   subVector.toSet mustBe Array(experience("test4.B", schema)).toSet
		   
		   subVector = runtime.minUnresolvableSubvector(
				Array(
						experience("test2.B", schema)),
				Array(
						experience("test3.A", schema), 
						experience("test4.B", schema)))
   		subVector.toSet mustBe Array(experience("test4.B", schema)).toSet
   
   		subVector = runtime.minUnresolvableSubvector(
   						Array(
   								experience("test2.B", schema)),
   						Array(
   								experience("test1.B", schema)))
   		subVector.toSet mustBe Array(experience("test1.B", schema)).toSet
   
   		subVector = runtime.minUnresolvableSubvector(
   						Array(
   								experience("test2.B", schema), 
   								experience("test3.C", schema)),	
   						Array(
   								experience("test1.B", schema)))
   		subVector.toSet mustBe Array(experience("test1.B", schema)).toSet
   
   		subVector = runtime.minUnresolvableSubvector(
   						Array(
   								experience("test3.C", schema)),
   						Array(
   								experience("test2.B", schema)))
   		subVector mustBe empty
   
   		subVector = runtime.minUnresolvableSubvector(
   						Array(
   								experience("test3.C", schema)),
   						Array(
   								experience("test2.B", schema),
   								experience("test5.C", schema))
   				);
   		subVector.toSet mustBe Array(experience("test5.C", schema)).toSet
   
   		subVector = runtime.minUnresolvableSubvector(
   						Array(
   								experience("test2.C", schema)),
   						Array(
   								experience("test1.C", schema), 
   								experience("test3.C", schema)));
   		subVector.toSet mustBe Array(experience("test1.C", schema)).toSet
   
   		subVector = runtime.minUnresolvableSubvector(
   						Array(
   								experience("test1.C", schema)),
   						Array(
   								experience("test4.B", schema), 
   								experience("test5.C", schema)));
   		subVector.toSet mustBe Array(experience("test5.C", schema)).toSet
   
   		subVector = runtime.minUnresolvableSubvector(
   						Array(
   								experience("test2.C", schema)),
   						Array(
   								experience("test4.B", schema), 
   								experience("test5.C", schema)));
   		subVector.toSet mustBe Array(experience("test4.B", schema)).toSet
   
   		subVector = runtime.minUnresolvableSubvector(
   				Array(
   						experience("test2.C", schema)),
   				Array(
   						experience("test3.B", schema), 
   						experience("test5.C", schema)));
   		subVector.toSet mustBe Array(experience("test5.C", schema)).toSet
   
   		subVector = runtime.minUnresolvableSubvector(
   				Array(
   						experience("test1.C", schema),
   						experience("test4.C", schema)),
   				Array(
   						experience("test3.B", schema), 
   						experience("test5.C", schema)));
   		subVector.toSet mustBe 
   				Array(
   						experience("test3.B", schema), 
   						experience("test5.C", schema)).toSet
   
   		subVector = runtime.minUnresolvableSubvector(
   				Array(
   						experience("test2.C", schema),
   						experience("test3.C", schema)),
   				Array(
   						experience("test1.C", schema)));
   		subVector.toSet mustBe 
   				Array(
   						experience("test1.C", schema)).toSet
   		
   		subVector = runtime.minUnresolvableSubvector(
   				Array(
   						experience("test2.C", schema),
   						experience("test3.C", schema)),
   				Array(
   						experience("test1.C", schema), 
   						experience("test4.C", schema)));
   		subVector.toSet mustBe 
   				Array(
   						experience("test1.C", schema), 
   						experience("test4.C", schema)).toSet
   		
   		subVector = runtime.minUnresolvableSubvector(
   				Array(
   						experience("test6.C", schema),
   						experience("test1.B", schema)),
   				Array(
   						experience("test5.C", schema), 
   						experience("test4.C", schema)));
   		subVector.toSet mustBe 
   				Array(
                    experience("test5.C", schema)).toSet
   		
   		subVector = runtime.minUnresolvableSubvector(
   				Array(
   						experience("test5.C", schema),
   						experience("test4.B", schema)),
   				Array(
   						experience("test1.C", schema), 
   						experience("test2.C", schema)));
   		subVector.toSet mustBe Array(
   						experience("test1.C", schema), 
   						experience("test2.C", schema)).toSet

	   }
	   
	   "test isTargetable()" in {

          var caughtEx = intercept[ServerException.Internal] {   
   				runtime.isTargetable(
   						test5,
   						state1,
   						Array(
   								experience("test5.A", schema),
   								experience("test2.A", schema)))
     			}
   		   assert(
   		         caughtEx.getMessage.equals(
                  new ServerException.Internal("Input test [test5] is already targeted").getMessage)
            )

          caughtEx = intercept[ServerException.Internal] {   
				runtime.isTargetable(
						test5,
						state1,
						Array(
								experience("test5.A", schema),
								experience("test2.A", schema)))
     			}
   		   assert(
   		         caughtEx.getMessage.equals(
                  new ServerException.Internal("Input test [test5] is already targeted").getMessage)
            )

		   caughtEx = intercept[ServerException.Internal] {   
				runtime.isTargetable(
					test6, 
					state1,
					Array(
							experience("test1.C", schema), 
							experience("test3.C", schema),
							experience("test5.B", schema)))
     			}
   		   assert(
   		         caughtEx.getMessage.equals(
                  new ServerException.Internal("Input vector [test1.C,test3.C,test5.B] is already unresolvable").getMessage)
            )

      		runtime.isTargetable(
      				test1, 
      				state1,
      				Array(experience("test2.B", schema))) mustBe false
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						experience("test5.B", schema),
      						experience("test4.C", schema))) mustBe false
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						experience("test6.B", schema))) mustBe true
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						experience("test4.C", schema),
      						experience("test6.B", schema))) mustBe true
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						experience("test6.C", schema),
      						experience("test5.B", schema))) mustBe false
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						experience("test4.C", schema),
      						experience("test5.C", schema),
      						experience("test6.B", schema))) mustBe false
      
      		runtime.isTargetable(
      				test2,
      				state1,
      				Array(
      						experience("test6.C", schema), 
      						experience("test5.C", schema))) mustBe true
      
      		runtime.isTargetable(
      				test2,
      				state1,
      				Array(
      						experience("test6.C", schema), 
      						experience("test3.C", schema))) mustBe true
      
      		runtime.isTargetable(
      				test2,
      				state1,
      				Array(
      						experience("test5.C", schema), 
      						experience("test4.C", schema))) mustBe false
      
      		runtime.isTargetable(
      				test2,
      				state1,
      				Array(
      						experience("test6.C", schema), 
      						experience("test4.C", schema), 
      						experience("test1.C", schema))) mustBe false
      
      		runtime.isTargetable(
      				test2,
      				state1,
      				Array(
      						experience("test6.C", schema), 
      						experience("test4.C", schema), 
      						experience("test1.C", schema))) mustBe false
      
      		// Targetable because 6 and 3 are logically disjoint, i.e.
      		// even though some states are instrumebnted by both, none is
      		// a variantful instrumentation.
      		runtime.isTargetable(
      				test3,
      				state1,
      				Array(
      						experience("test6.C", schema), 
      						experience("test2.C", schema))) mustBe true
      
      		runtime.isTargetable(
      				test5,
      				state1,
      				Array(
      						experience("test6.C", schema), 
      						experience("test4.C", schema), 
      						experience("test1.C", schema))) mustBe false
      
      		runtime.isTargetable(
      				test5,
      				state1,
      				Array(
      						experience("test6.C", schema), 
      						experience("test4.C", schema), 
      						experience("test1.C", schema))) mustBe false
      
      		runtime.isTargetable(
      				test5,
      				state1,
      				Array(
      						experience("test2.C", schema))) mustBe true
      
      		runtime.isTargetable(
      				test6,
      				state1,
      				Array(
      						experience("test4.C", schema), 
      						experience("test1.C", schema))) mustBe true
      
      		runtime.isTargetable(
      				test6,
      				state1,
      				Array(
      						experience("test5.C", schema), 
      						experience("test2.C", schema))) mustBe true
      
      		// See comment about test3 and test6 above
      		runtime.isTargetable(
      				test6,
      				state1,
      				Array(
      						experience("test3.C", schema))) mustBe true
      
      		// Ditto
      		runtime.isTargetable(
      				test6,
      				state1,
      				Array(
      						experience("test3.C", schema), 
      						experience("test2.C", schema))) mustBe true
                  
	   }
	}
}
