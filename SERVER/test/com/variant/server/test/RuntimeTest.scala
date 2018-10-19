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
   	val state4 = schema.getState("state4").get
   	
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
               test2.getExperience("B").get) mustBe 
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
         
         intercept[ServerException.Internal] {
			   runtime.resolveState(
				   state1, 
				   test2.getExperience("B").get,   
					test3.getExperience("C").get,   
					test5.getExperience("B").get)
					
			}.getMessage mustBe "Undefined experience [test3.C] in coordinate vector"

         runtime.resolveState(
				   state1, 
				   test2.getExperience("B").get,   
					test3.getExperience("B").get,   
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
		            
   		intercept[ServerException.Internal] {
  			   runtime.resolveState(
  					state2, 
   				test1.getExperience("A").get, 
   				test6.getExperience("A").get,   
   				test4.getExperience("A").get, 
   				test2.getExperience("A").get,   
   				test5.getExperience("A").get)
  			}.getMessage mustBe "Undefined experience [test2.A] in coordinate vector"

   	   runtime.resolveState(
  					state2, 
   				test1.getExperience("A").get, 
   				test6.getExperience("A").get,   
   				test4.getExperience("A").get, 
   				test5.getExperience("A").get) mustBe Optional.empty

	   }

	   "test isResolvable()" in {

   	   runtime.isResolvable(test1.getExperience("A").get) mustBe true
		
         runtime.isResolvable(test1.getExperience("B").get) mustBe true

         intercept[ServerException.Internal] {

				runtime.minUnresolvableSubvector(
						Array(
								test1.getExperience("C").get, 
								test2.getExperience("B").get),
						Array(
								test1.getExperience("C").get, 
								test2.getExperience("B").get))
  			
   	   }.getMessage mustBe "Input vector [test1.C,test2.B] must be resolvable, but is not"
         
         intercept[ServerException.Internal] {

				runtime.minUnresolvableSubvector(
						Array(
								test1.getExperience("C").get),
						Array(
								test1.getExperience("B").get, 
								test2.getExperience("B").get))
  			
   	   }.getMessage mustBe "Experience [test1.B] in second argument contradicts experience [test1.C] in first argument"

         intercept[ServerException.Internal] {

				runtime.minUnresolvableSubvector(
						Array(
								test2.getExperience("B").get, 
								test3.getExperience("C").get),
						Array(
								test1.getExperience("A").get, 
								test3.getExperience("B").get))
  			
   	   }.getMessage mustBe "Experience [test3.B] in second argument contradicts experience [test3.C] in first argument"

         runtime.minUnresolvableSubvector(
				Array(
						test1.getExperience("A").get, 
						test2.getExperience("B").get),
				Array(
						test3.getExperience("C").get, 
						test4.getExperience("B").get)).toSet mustBe 
				Array(test4.getExperience("B").get).toSet
		   
		   runtime.minUnresolvableSubvector(
				Array(
						test2.getExperience("B").get),
				Array(
						test3.getExperience("A").get, 
						test4.getExperience("B").get)).toSet mustBe 		
			   Array(test4.getExperience("B").get).toSet
   
   		runtime.minUnresolvableSubvector(
   						Array(
   								test2.getExperience("B").get),
   						Array(
   								test1.getExperience("B").get)).toSet mustBe 
   				Array(test1.getExperience("B").get).toSet
   
   		runtime.minUnresolvableSubvector(
   						Array(
   								test2.getExperience("B").get, 
   								test3.getExperience("C").get),	
   						Array(
   								test1.getExperience("B").get)).toSet mustBe 
   		      Array(test1.getExperience("B").get).toSet
   
   		runtime.minUnresolvableSubvector(
   						Array(
   								test3.getExperience("C").get),
   						Array(
   								test2.getExperience("B").get)) mustBe empty
   
   		runtime.minUnresolvableSubvector(
   						Array(
   								test3.getExperience("C").get),
   						Array(
   								test2.getExperience("B").get,
   								test5.getExperience("C").get)).toSet mustBe 
   				Array(test5.getExperience("C").get).toSet
   
   		runtime.minUnresolvableSubvector(
   						Array(
   								test2.getExperience("C").get),
   						Array(
   								test1.getExperience("C").get, 
   								test3.getExperience("C").get)).toSet mustBe 
   				Array(test1.getExperience("C").get).toSet
   
   		runtime.minUnresolvableSubvector(
   						Array(
   								test1.getExperience("C").get),
   						Array(
   								test4.getExperience("B").get, 
   								test5.getExperience("C").get)).toSet mustBe 
   				Array(test5.getExperience("C").get).toSet
   
   		runtime.minUnresolvableSubvector(
   						Array(
   								test2.getExperience("C").get),
   						Array(
   								test4.getExperience("B").get, 
   								test5.getExperience("C").get)).toSet mustBe 
   				Array(test4.getExperience("B").get).toSet
   
   		runtime.minUnresolvableSubvector(
   				Array(
   						test2.getExperience("C").get),
   				Array(
   						test3.getExperience("B").get, 
   						test5.getExperience("C").get)).toSet mustBe 
   			Array(test5.getExperience("C").get).toSet
   
   		runtime.minUnresolvableSubvector(
   				Array(
   						test1.getExperience("C").get,
   						test4.getExperience("C").get),
   				Array(
   						test3.getExperience("B").get, 
   						test5.getExperience("C").get)).toSet mustBe
   				Array(
   						test3.getExperience("B").get, 
   						test5.getExperience("C").get).toSet
   
   		runtime.minUnresolvableSubvector(
   				Array(
   						test2.getExperience("C").get,
   						test3.getExperience("C").get),
   				Array(
   						test1.getExperience("C").get)).toSet mustBe 
   				Array(
   						test1.getExperience("C").get).toSet
   		
   		runtime.minUnresolvableSubvector(
   				Array(
   						test2.getExperience("C").get,
   						test3.getExperience("C").get),
   				Array(
   						test1.getExperience("C").get, 
   						test4.getExperience("C").get)).toSet mustBe 
   				Array(
   						test1.getExperience("C").get, 
   						test4.getExperience("C").get).toSet
   		
   		runtime.minUnresolvableSubvector(
   				Array(
   						test6.getExperience("C").get,
   						test1.getExperience("B").get),
   				Array(
   						test5.getExperience("C").get, 
   						test4.getExperience("C").get)).toSet mustBe 
   				Array(
                    test5.getExperience("C").get).toSet
   		
   		runtime.minUnresolvableSubvector(
   				Array(
   						test5.getExperience("C").get,
   						test4.getExperience("B").get),
   				Array(
   						test1.getExperience("C").get, 
   						test2.getExperience("C").get)).toSet mustBe Array(
   						test1.getExperience("C").get, 
   						test2.getExperience("C").get).toSet

	   }
	   
	   "test isTargetable()" in {

   	   intercept[ServerException.Internal] {   
   				runtime.isTargetable(
   						test5,
   						state1,
   						Array(
   								test5.getExperience("A").get,
   								test2.getExperience("A").get))
     			
   	      }.getMessage mustBe "Input test [test5] is already targeted"
   	      
         intercept[ServerException.Internal] {   
				runtime.isTargetable(
						test5,
						state1,
						Array(
								test5.getExperience("A").get,
								test2.getExperience("A").get))
     			
   	      }.getMessage mustBe "Input test [test5] is already targeted"

		   intercept[ServerException.Internal] {   
				runtime.isTargetable(
					test6, 
					state1,
					Array(
							test1.getExperience("C").get, 
							test3.getExperience("C").get,
							test5.getExperience("B").get))
     			
   	      }.getMessage mustBe "Input vector [test1.C,test3.C,test5.B] is already unresolvable"

      		runtime.isTargetable(
      				test1, 
      				state1,
      				Array(test2.getExperience("B").get)) mustBe false
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						test5.getExperience("B").get,
      						test4.getExperience("C").get)) mustBe false
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						test6.getExperience("B").get)) mustBe true
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						test4.getExperience("C").get,
      						test6.getExperience("B").get)) mustBe true
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						test6.getExperience("C").get,
      						test5.getExperience("B").get)) mustBe false
      
      		runtime.isTargetable(
      				test1,
      				state1,
      				Array(
      						test4.getExperience("C").get,
      						test5.getExperience("C").get,
      						test6.getExperience("B").get)) mustBe false
      
      		runtime.isTargetable(
      				test2,
      				state1,
      				Array(
      						test6.getExperience("C").get, 
      						test5.getExperience("C").get)) mustBe true
      
      		runtime.isTargetable(
      				test2,
      				state3,
      				Array(
      						test6.getExperience("C").get, 
      						test3.getExperience("A").get)) mustBe true
      
      		runtime.isTargetable(
      				test2,
      				state3,
      				Array(
      						test6.getExperience("A").get, 
      						test3.getExperience("C").get)) mustBe true

      						runtime.isTargetable(
      				test2,
      				state1,
      				Array(
      						test5.getExperience("C").get, 
      						test4.getExperience("C").get)) mustBe false
      
      		runtime.isTargetable(
      				test2,
      				state1,
      				Array(
      						test6.getExperience("C").get, 
      						test4.getExperience("C").get, 
      						test1.getExperience("C").get)) mustBe false
      
      		runtime.isTargetable(
      				test2,
      				state1,
      				Array(
      						test6.getExperience("C").get, 
      						test4.getExperience("C").get, 
      						test1.getExperience("C").get)) mustBe false
      
      		runtime.isTargetable(
      				test3,
      				state1,
      				Array(
      						test6.getExperience("C").get, 
      						test2.getExperience("C").get)) mustBe false
      
      		runtime.isTargetable(
      				test5,
      				state1,
      				Array(
      						test6.getExperience("C").get, 
      						test2.getExperience("C").get)) mustBe true

      						runtime.isTargetable(
      				test5,
      				state1,
      				Array(
      						test6.getExperience("C").get, 
      						test4.getExperience("C").get, 
      						test1.getExperience("C").get)) mustBe false
      
      		runtime.isTargetable(
      				test5,
      				state1,
      				Array(
      						test6.getExperience("C").get, 
      						test4.getExperience("C").get, 
      						test1.getExperience("C").get)) mustBe false
      
      		runtime.isTargetable(
      				test5,
      				state1,
      				Array(
      						test2.getExperience("C").get)) mustBe true
      
      		runtime.isTargetable(
      				test6,
      				state1,
      				Array(
      						test4.getExperience("C").get, 
      						test1.getExperience("C").get)) mustBe true
      
      		runtime.isTargetable(
      				test6,
      				state1,
      				Array(
      						test5.getExperience("C").get, 
      						test2.getExperience("C").get)) mustBe true
      
      		runtime.isTargetable(
      				test4,
      				state4,
      				Array(
      						test6.getExperience("C").get, 
      						test5.getExperience("C").get, 
      						test2.getExperience("A").get)) mustBe true
                        
      		runtime.isTargetable(
      				test4,
      				state4,
      				Array(
      						test6.getExperience("C").get, 
      						test5.getExperience("C").get, 
      						test2.getExperience("C").get)) mustBe false
	   }
	}
}
