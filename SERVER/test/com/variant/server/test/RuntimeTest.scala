package com.variant.server.test;

import com.variant.server.api.ServerException
import com.variant.server.boot.RuntimeTestFacade
import com.variant.server.util.JavaImplicits._
import com.variant.server.test.spec.EmbeddedServerSpec
import java.util.Optional

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class RuntimeTest extends EmbeddedServerSpec {
   
	"Runtime" should {

      val schema = server.schemata.get("monstrosity").get.liveGen.get
 	   val runtime = RuntimeTestFacade(schema)

      val state1 = schema.getState("state1").get
      val state2 = schema.getState("state2").get
   	val state3 = schema.getState("state3").get
   	val test1 = schema.getVariation("test1").get
      val test2 = schema.getVariation("test2").get
   	val test3 = schema.getVariation("test3").get
   	val test4 = schema.getVariation("test4").get
   	val test5 = schema.getVariation("test5").get
   	val test6 = schema.getVariation("test6").get

      /**
       * 
       */
	   "resolve for state1" in {
	         	    
   	   intercept[ServerException.Internal] {
             
            runtime.resolveState(state1, test1.getExperience("B").get)
             
         }.getMessage mustEqual "Uninstrumented variation [test1.B] in coordinate vector"

         intercept[ServerException.Internal] {
         
            runtime.resolveState(state1, test1.getExperience("A").get, test2.getExperience("B").get)
         
         }.getMessage mustEqual "Uninstrumented variation [test1.A] in coordinate vector"

         // Control 
         runtime.resolveState(
               state1, 
               test2.getExperience("A").get) mustBe Optional.empty

         runtime.resolveState(
               state1, 
               experience("test2.B", schema)) mustBe 
            test2.getOnState(state1).get.getVariant(
                  test2.getExperience("B").get)

         runtime.resolveState(
			   state1, 
				test3.getExperience("B").get,   
				test5.getExperience("C").get) mustBe null

         intercept[ServerException.Internal] {
            
				runtime.resolveState(
					state1,	 
					test2.getExperience("B").get,   
					test3.getExperience("C").get,   
					test2.getExperience("B").get)   // dupe test
					
			}.getMessage mustBe "Duplicate variation [test2] in coordinate vector"
         
         runtime.resolveState(
				   state1, 
				   test2.getExperience("B").get,   
					test3.getExperience("C").get,   
					test5.getExperience("B").get) mustBe null

         runtime.resolveState(
				   state1, 
				   test2.getExperience("B").get,   
					test3.getExperience("A").get,   
					test5.getExperience("B").get) mustBe 
		      test5.getOnState(state1).get.getVariant(
		            test2.getExperience("B").get, 
		            test5.getExperience("B").get)

		   intercept[ServerException.Internal] {
				runtime.resolveState(
						state1, 
						test4.getExperience("B").get, 
						test2.getExperience("B").get,   
						test1.getExperience("C").get,  // not instrumented
						test3.getExperience("C").get)
						
			}.getMessage mustBe "Uninstrumented variation [test1.C] in coordinate vector"

		   runtime.resolveState(
				   state1,
				   test2.getExperience("B").get, 
		   		test5.getExperience("B").get,   
		   		test6.getExperience("C").get, 
		   		test3.getExperience("B").get) mustBe null
            
		   runtime.resolveState(
				   state1,
				   test2.getExperience("B").get, 
		   		test5.getExperience("B").get,   
		   		test6.getExperience("C").get, 
		   		test3.getExperience("A").get) mustBe 
		   	test6.getOnState(state1).get.getVariant(
		   	      test2.getExperience("B").get, 
		   	      test5.getExperience("B").get, 
		   	      test6.getExperience("C").get)

		   		runtime.resolveState(
   				state1, 
   				test5.getExperience("B").get,  
   				test2.getExperience("B").get,   
   				test6.getExperience("B").get) mustBe
		   	test6.getOnState(state1).get.getVariant(
		   	      test2.getExperience("B").get, 
		   	      test5.getExperience("B").get, 
		   	      test6.getExperience("B").get)
   
		   runtime.resolveState(
   				state1, 
   				test5.getExperience("B").get,  
   				test2.getExperience("A").get,   
   				test6.getExperience("B").get,  
   				test3.getExperience("A").get) mustBe
		   	test6.getOnState(state1).get.getVariant(
		   	      test5.getExperience("B").get,
		   	      test6.getExperience("B").get)

		   runtime.resolveState(
   				state1, 
   				test5.getExperience("A").get,  
   				test2.getExperience("A").get,   
   				test6.getExperience("A").get,  
   				test3.getExperience("A").get) mustBe Optional.empty()

	   }
	   
      /**
       * 
       */
	   "resolve for state2" in {
   		
	      runtime.resolveState(
				   state2, 
				   test1.getExperience("A").get) mustBe Optional.empty

   		runtime.resolveState(
   				state2, 
	   			test1.getExperience("A").get,
					test2.getExperience("B").get) mustBe 
            test2.getOnState(state2).get.getVariant(
		            test2.getExperience("B").get)

       	runtime.resolveState(
   				state2, 
   		  		test1.getExperience("B").get) mustBe
            test1.getOnState(state2).get.getVariant(
		            test1.getExperience("B").get)
   
   		runtime.resolveState(
   				state2, 
   				test6.getExperience("C").get,   
   				test2.getExperience("B").get) mustBe
            test6.getOnState(state2).get.getVariant(
		            test2.getExperience("B").get,
		            test6.getExperience("C").get)
    		
         intercept[ServerException.Internal] {
 				runtime.resolveState(
   					state2, 
   					test2.getExperience("B").get,  
   					test6.getExperience("C").get,   
   					test2.getExperience("C").get)
   					
  			}.getMessage mustBe "Duplicate variation [test2] in coordinate vector"

        runtime.resolveState(
   				state2, 
   				test4.getExperience("B").get,  
   				test1.getExperience("C").get,  
   				test6.getExperience("C")get) mustBe
             test6.getOnState(state2).get.getVariant(
		            test1.getExperience("C").get,
		            test6.getExperience("C").get,
		            test4.getExperience("B").get)
      
         intercept[ServerException.Internal] {
   		   runtime.resolveState(
   				state2, 
   				test2.getExperience("B").get,  
   				test5.getExperience("C").get,  
   				test3.getExperience("B").get)
  			}.getMessage mustBe "Uninstrumented variation [test3.B] in coordinate vector"
      
   		runtime.resolveState(
   				state2, 
   				test4.getExperience("C").get,
   				test2.getExperience("B").get,  
   				test5.getExperience("C").get,  
   				test1.getExperience("B").get) mustBe null
   
   		runtime.resolveState(
   				state2, 
   				test1.getExperience("C").get, 
   				test6.getExperience("B").get,   
   				test2.getExperience("B").get,   
   				test5.getExperience("C").get) mustBe null
   				
   		runtime.resolveState(
   				state2, 
   				test1.getExperience("A").get, 
   				test6.getExperience("B").get,   
   				test2.getExperience("B").get,   
   				test5.getExperience("C").get) mustBe 
   			test6.getOnState(state2).get.getVariant(
		            test2.getExperience("B").get,
		            test6.getExperience("B").get,
		            test5.getExperience("C").get)
		            
   		runtime.resolveState(
   				state2, 
   				test1.getExperience("A").get, 
   				test6.getExperience("A").get,   
   				test4.getExperience("A").get, 
   				test2.getExperience("A").get,   
   				test5.getExperience("A").get) mustBe Optional.empty


	   }
/*	   
	   "resolve for state3" in {

         val schema = server.schemata.get("monstrosity").get.liveGen.get
 	      val runtime = RuntimeTestFacade(schema)

      	val state1 = schema.getState("state1").get
      	val state2 = schema.getState("state2").get
   	   val state3 = schema.getState("state3").get
   	   val test1 = schema.getVariation("test1").get
      	val test2 = schema.getVariation("test2").get
   	   val test3 = schema.getVariation("test3").get
   	   val test4 = schema.getVariation("test4").get
   	   val test5 = schema.getVariation("test5").get
   	   val test6 = schema.getVariation("test6").get

   	   var resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.A", schema)   // control
   				)
   		);
   		resolvedVariant mustBe None
   		
   		resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema) 
   				)
   		);
   		resolvedVariant mustBe Some
   		resolvedVariant.get.getParameters().get("path") mustBe "/path/to/state3/test2.B"
      
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
               new ServerException.Internal("Uninstrumented variation [test4.B] in coordinate vector").getMessage)
         )

         resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test3.B", schema)    
   				)
   		);
   		resolvedVariant mustBe None
   
   		resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema)   
   				)
   		);
   		resolvedVariant mustBe Some
   		resolvedVariant.get.getParameters().get("path") mustBe "/path/to/state3/test2.B"
   
   		resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test3.B", schema),   
   						experience("test5.C", schema)    
   				)
   		);
   		resolvedVariant mustBe None
   
   		resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema),  
   						experience("test3.C", schema)    
   				)
   		);
   		resolvedVariant mustBe Some
   		resolvedVariant.get.getParameters().get("path") mustBe "/path/to/state3/test2.B"
   
         caughtEx = intercept[ServerException.Internal] {
   					runtime.resolveState(
   							state3, 
   							Array(
   									experience("test2.B", schema),  
   									experience("test3.A", schema),   
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
   								experience("test1.C", schema),  
   								experience("test2.B", schema),  // unsupported
   								experience("test3.B", schema),   
   								experience("test4.B", schema)   // uninstrumented
   						)
   				)
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Uninstrumented variation [test4.B] in coordinate vector").getMessage)
         )

         resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test1.C", schema),  
   						experience("test2.B", schema),  // unsupported
   						experience("test3.B", schema)    
   				)
   		);
   		resolvedVariant mustBe null
   		
   		resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema),  
   						experience("test3.C", schema)    
   				)
   		);
   		resolvedVariant mustBe Some
   		resolvedVariant.get.getParameters().get("path") mustBe "/path/to/state3/test2.B"
      		
         caughtEx = intercept[ServerException.Internal] {
   				runtime.resolveState(
   						state3, 
   						Array(
   								experience("test4.B", schema),  // uninstrumented
   								experience("test1.C", schema),  
   								experience("test6.C", schema),  
   								experience("test3.B", schema)    
   						)
   				)
  			}
		   assert(
		         caughtEx.getMessage.equals(
               new ServerException.Internal("Uninstrumented variation [test4.B] in coordinate vector").getMessage)
         )

   		resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema),  // unsupported
   						experience("test1.C", schema),  
   						experience("test6.B", schema),  
   						experience("test3.C", schema)    
   				)
   		);
   		resolvedVariant mustBe null
   
   		resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test2.B", schema),  
   						experience("test5.C", schema),   
   						experience("test3.B", schema)    
   				)
   		)
   		resolvedVariant mustBe Some
   		resolvedVariant.get.getParameters().get("path") mustBe "/path/to/state3/test2.B"
   
   		resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test6.C", schema),  // control
   						experience("test2.B", schema),  // unsupported
   						experience("test5.C", schema),   
   						experience("test1.B", schema)   
   				)
   		);
   		resolvedVariant mustBe null
   
   		resolvedVariant = runtime.resolveState(
   				state3, 
   				Array(
   						experience("test6.B", schema),  
   						experience("test2.B", schema),  
   						experience("test5.C", schema)    
   				)
   		);
   		resolvedVariant mustBe Some
   		resolvedVariant.get.getParameters().get("path") mustBe "/path/to/state3/test2.B+test6.B"   
	   }

	   "resolve these coordinate vectors" in {

         val schema = server.schemata.get("monstrosity").get.liveGen.get
 	      val runtime = RuntimeTestFacade(schema)

      	val state1 = schema.getState("state1").get
      	val state2 = schema.getState("state2").get
   	   val state3 = schema.getState("state3").get
   	   val test1 = schema.getVariation("test1").get
      	val test2 = schema.getVariation("test2").get
   	   val test3 = schema.getVariation("test3").get
   	   val test4 = schema.getVariation("test4").get
   	   val test5 = schema.getVariation("test5").get
   	   val test6 = schema.getVariation("test6").get

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

         val schema = server.schemata.get("monstrosity").get.liveGen.get
 	      val runtime = RuntimeTestFacade(schema)

      	val state1 = schema.getState("state1").get
      	val state2 = schema.getState("state2").get
   	   val state3 = schema.getState("state3").get
   	   val test1 = schema.getVariation("test1").get
      	val test2 = schema.getVariation("test2").get
   	   val test3 = schema.getVariation("test3").get
   	   val test4 = schema.getVariation("test4").get
   	   val test5 = schema.getVariation("test5").get
   	   val test6 = schema.getVariation("test6").get

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
	   * 
	   */
	}
}
