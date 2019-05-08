package com.variant.server.test;

import com.variant.core.error.UserError.Severity._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.EmbeddedServerSpec

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class HookInstantiationExceptionTest extends EmbeddedServerSpec {
	   
   /**
    * 
    */
	"Any hook" should {
	   
	   ////////////////////
	   "emit HOOK_CLASS_NO_INTERFACE if class doesn't implement Userook" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                         
   			'class':'com.variant.server.test.hooks.HookNoInterface'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'variations':[                                                           
	   {                                                                
		   'name':'test1',
	      'experiences':[                                               
            {                                                          
				   'name':'A',                                             
				   'weight':10,                                            
				   'isControl':true                                        
	         },                                                         
		      {                                                          
		         'name':'B',                                             
				   'weight':20                                             
				}                                                          
	      ],                                                            
			'onStates':[                                                   
			   {                                                          
				   'stateRef':'state1',                                     
				   'variants':[{'experienceRef':'B'} ]                                                       
	         }                                                          
	      ]                                                             
	   }                                                               
   ]                                                                   
}"""

   	     val schemaDeployer = SchemaDeployer.fromString(schema)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         response.getMessages.size mustBe 1
     		 response.getMessages(FATAL) mustBe empty
     		 response.getMessages(ERROR).size() mustBe 1
     		 var msg = response.getMessages.get(0)
     	   msg.getSeverity mustBe ERROR
     	   msg.getText mustBe HOOK_CLASS_NO_INTERFACE.asMessage("com.variant.server.test.hooks.HookNoInterface", "com.variant.core.lifecycle.LifecycleHook")
      }
   }
	
	/**
    * 
    */
	"StateParsedHook" should {
	   
	   ////////////////////
	   "emit OBJECT_INSTANTIATION_ERROR for a non-existent hook class" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                                                                                   
   			'class':'com.foo.bar'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'variations':[                                                           
	   {                                                                
		   'name':'test1',
	      'experiences':[                                               
            {                                                          
				   'name':'A',                                             
				   'weight':10,                                            
				   'isControl':true                                        
	         },                                                         
		      {                                                          
		         'name':'B',                                             
				   'weight':20                                             
				}                                                          
	      ],                                                            
			'onStates':[                                                   
			   {                                                          
				   'stateRef':'state1',                                     
				   'variants':[{'experienceRef':'B'} ]                                                       
	         }                                                          
	      ]                                                             
	   }                                                               
   ]                                                                   
}"""

   	  val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe OBJECT_INSTANTIATION_ERROR.asMessage("com.foo.bar", "java.lang.ClassNotFoundException")
   }

	   ////////////////////
	   "emit OBJECT_CONSTRUCTOR_ERROR for an existing hook class with non-public constructor" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                                                                                                   
   			'class':'com.variant.server.test.hooks.HookPrivateConstructor'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'variations':[                                                           
	   {                                                                
		   'name':'test1',
	      'experiences':[                                               
            {                                                          
				   'name':'A',                                             
				   'weight':10,                                            
				   'isControl':true                                        
	         },                                                         
		      {                                                          
		         'name':'B',                                             
				   'weight':20                                             
				}                                                          
	      ],                                                            
			'onStates':[                                                   
			   {                                                          
				   'stateRef':'state1',                                     
				   'variants':[{'experienceRef':'B'} ]                                                       
	         }                                                          
	      ]                                                             
	   }                                                               
   ]                                                                   
}"""

     val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookPrivateConstructor", "java.lang.IllegalAccessException")
      }
	   
	   ////////////////////
	   "emit OBJECT_CONSTRUCTION_ERROR for an existing hook class with wrong signature constructor" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                                                              
   			'class':'com.variant.server.test.hooks.HookWrongSignatureConstructor'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'variations':[                                                           
	   {                                                                
		   'name':'test1',
	      'experiences':[                                               
            {                                                          
				   'name':'A',                                             
				   'weight':10,                                            
				   'isControl':true                                        
	         },                                                         
		      {                                                          
		         'name':'B',                                             
				   'weight':20                                             
				}                                                          
	      ],                                                            
			'onStates':[                                                   
			   {                                                          
				   'stateRef':'state1',                                     
				   'variants':[{'experienceRef':'B'} ]                                                       
	         }                                                          
	      ]                                                             
	   }                                                               
   ]                                                                   
}"""

      val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookWrongSignatureConstructor")
      }
	   
	   ////////////////////
	   "emit HOOK_STATE_SCOPE_VIOLATION when qualification hoook is defined at state level" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest'
   },                                                                   
	'states':[
		{
			'name':'state1',
         'hooks':[                                                         
            {                                                            
      			'class':'com.variant.server.test.hooks.TestQualificationHookSimple',
      			'init':{'value':'should crash'}
      	   }                                                              
         ]
		}
	],                                                                   
	'variations':[                                                           
	   {                                                                
		   'name':'test1',
	      'experiences':[                                               
            {                                                          
				   'name':'A',                                             
				   'weight':10,                                            
				   'isControl':true                                        
	         },                                                         
		      {                                                          
		         'name':'B',                                             
				   'weight':20                                             
				}                                                          
	      ],                                                            
			'onStates':[                                                   
			   {                                                          
				   'stateRef':'state1',                                     
				   'variants':[{'experienceRef':'B'} ]                                                       
	         }                                                          
	      ]                                                             
	   }                                                               
   ]                                                                   
}"""

      val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe (HOOK_STATE_SCOPE_VIOLATION.asMessage("/states[0]/hooks[0]/", "com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent"))
      }
	}
		
}
