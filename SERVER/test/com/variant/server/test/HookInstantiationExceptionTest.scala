package com.variant.server.test;

import com.variant.core.LifecycleEvent
import com.variant.core.schema.StateParsedLifecycleEvent
import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.server.schema.SchemaDeployer
import com.variant.core.schema.TestParsedLifecycleEvent
import com.variant.core.schema.Test
import org.scalatest.Assertions._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.core.CommonError._
import com.variant.server.api.ServerException
import com.variant.core.schema.parser.ParserMessageImpl
import com.variant.server.api.TestQualificationLifecycleEvent
import com.variant.server.api.TestTargetingLifecycleEvent
import com.variant.server.api.UserHook
import com.variant.core.schema.parser.ParserError
import com.variant.server.test.hooks.StateParsedHook
import com.variant.server.boot.ServerErrorLocal

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class HookInstantiationExceptionTest extends BaseSpecWithServer {
	   
   /**
    * 
    */
	"StateParsedHook" should {
	   
	   ////////////////////
	   "emit HOOK_INSTANTIATION_ERROR for a non-existent hook class" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                                                              
   		   'name':'stateParsed',                                       
   			'class':'com.foo.bar'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'tests':[                                                           
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

         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schema)).get  
         response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.HOOK_INSTANTIATION_ERROR.asMessage("com.foo.bar", "java.lang.ClassNotFoundException")
      }

	   ////////////////////
	   "emit HOOK_INSTANTIATION_ERROR for an existing hook class with non-public constructor" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                                                              
   		   'name':'stateParsed',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHookPrivateConstructor'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'tests':[                                                           
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

         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schema)).get  
         response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.HOOK_INSTANTIATION_ERROR.asMessage("com.variant.server.test.hooks.StateParsedHookPrivateConstructor", "java.lang.IllegalAccessException")
      }
	   
	   ////////////////////
	   "emit HOOK_INSTANTIATION_ERROR for an existing hook class with no no-argument constructor" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                                                              
   		   'name':'stateParsed',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHookArgumentConstructor'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'tests':[                                                           
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

         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schema)).get  
         response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.HOOK_INSTANTIATION_ERROR.asMessage("com.variant.server.test.hooks.StateParsedHookArgumentConstructor", "java.lang.InstantiationException")
      }
	   
	   ////////////////////
	   "emit HOOK_SCHEMA_DOMAIN_DEFINED_AT_TEST when defined at Test level" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest'
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'tests':[                                                           
	   {                                                                
		   'name':'test1',
         'hooks':[                                                         
            {                                                              
      		   'name':'stateParsed',                                       
      			'class':'com.variant.server.test.hooks.StateParsedHook'     
      	   }                                                              
         ],
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

         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schema)).get  
         response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.HOOK_SCHEMA_DOMAIN_DEFINED_AT_TEST.asMessage("stateParsed", "com.variant.core.schema.StateParsedLifecycleEvent", "test1")
      }
	}
	
	
	/**
    * 
    */
	"TestParsedHook" should {
	      
	   ////////////////////
	   "emit HOOK_SCHEMA_DOMAIN_DEFINED_AT_TEST when defined at test level" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest'
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'tests':[                                                           
	   {                                                                
		   'name':'test1',
         'hooks':[                                                         
            {                                                              
      		   'name':'testParsed',                                       
      			'class':'com.variant.server.test.hooks.TestParsedHook'     
      	   }                                                              
         ],
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

         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schema)).get 

         response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.HOOK_SCHEMA_DOMAIN_DEFINED_AT_TEST.asMessage("testParsed", "com.variant.core.schema.TestParsedLifecycleEvent", "test1")
      }

	}
	
	
  /**
    * 
    */
	"Test Targeting Hook" should {

      ////////////////////
	   "emit HOOK_TEST_DOMAIN_DEFINED_AT_SCHEMA when defined at schema level" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                                                              
   		   'name':'testTargeting',                                       
   			'class':'com.variant.server.test.hooks.TestTargetingHookNil'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
	'tests':[                                                           
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

         val response = server.installSchemaDeployer(SchemaDeployer.fromString(schema)).get  
         response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.HOOK_TEST_DOMAIN_DEFINED_AT_SCHEMA.asMessage("testTargeting", "com.variant.server.api.TestTargetingLifecycleEvent")
      }
   }

}
