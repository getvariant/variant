package com.variant.server.test;

import com.variant.core.lce.LifecycleEvent
import com.variant.core.lce.StateParsedLifecycleEvent
import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.core.lce.TestParsedLifecycleEvent
import com.variant.core.schema.Test
import org.scalatest.Assertions._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.server.api.ServerException
import com.variant.core.schema.parser.ParserMessageImpl
import com.variant.server.test.hooks.StateParsedHook
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.schema.SchemaDeployer.fromString
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.BaseSpecWithServer

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class HookInstantiationExceptionTest extends BaseSpecWithServer {
	   
   /**
    * 
    */
	"Any hook" should {
	   
	   ////////////////////
	   "emit HOOK_CLASS_NO_INTERFACE if class doesn't implement UserHook" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                                                              
   		     'name':'stateParsed',                                       
   			   'class':'com.variant.server.test.hooks.HookNoInterface'     
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

   	     val schemaDeployer = SchemaDeployer.fromString(schema)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         response.getMessages.size mustBe 1
     		 response.getMessages(FATAL) mustBe empty
     		 response.getMessages(ERROR).size() mustBe 1
     		 var msg = response.getMessages.get(0)
     	   msg.getSeverity mustBe ERROR
     	   msg.getText mustBe ServerErrorLocal.HOOK_CLASS_NO_INTERFACE.asMessage("com.variant.server.test.hooks.HookNoInterface", "com.variant.core.UserHook")
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

   	  val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.foo.bar", "java.lang.ClassNotFoundException")
   }

	   ////////////////////
	   "emit OBJECT_CONSTRUCTOR_ERROR for an existing hook class with non-public constructor" in {
	      
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

     val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.StateParsedHookPrivateConstructor", "java.lang.IllegalAccessException")
      }
	   
	   ////////////////////
	   "emit OBJECT_CONSTRUCTION_ERROR for an existing hook class with no nullary or config constructor" in {
	      
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

      val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.StateParsedHookArgumentConstructor", "java.lang.InstantiationException")
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

      val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 1
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 1
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		///msg.getText mustBe ServerErrorLocal.HOOK_SCHEMA_DOMAIN_DEFINED_AT_TEST.asMessage("stateParsed", "com.variant.core.schema.StateParsedLifecycleEvent", "test1")
      }
	}
		
}
