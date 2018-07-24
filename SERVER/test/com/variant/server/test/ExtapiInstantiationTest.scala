package com.variant.server.test;

import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.core.schema.Test
import org.scalatest.Assertions._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.core.impl.ServerError._
import com.variant.server.api.ServerException
import com.variant.core.schema.parser.ParserMessageImpl
import com.variant.core.schema.parser.error.SemanticError
import com.variant.server.test.hooks.StateParsedHook
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.test.hooks.TestParsedHook
import com.variant.server.schema.SchemaDeployer.fromString
import com.variant.server.test.hooks.StateParsedHook2Constructors
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.EmbeddedServerSpec

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class ExtapiInstantiationTest extends EmbeddedServerSpec {

   val schemaName = "ExtapiInstantiationTest"
   
  /*
   * 
   */
	"Stateless Hook" should {
	   
	   ////////////////
	   "initialize from sole nullary constructor" in {
	      
   	    val schema = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   		   'name':'nullaryOnlyNoInit',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHookNullaryOnly'
            // no init property ok
   	   },
         {                                                              
   		   'name':'nullaryOnlyNullInit',
   			'class':'com.variant.server.test.hooks.StateParsedHookNullaryOnly',    
            'init': null // explicit null ok
   	   }                                                         
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
   ],                                                                   
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
				   'variants':[                                            
				      {'experienceRef':'B'}
			      ]                                                       
	         }                                                          
	      ]                                                             
	   }
   ]                                                                   
}"""

      val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 0

   	server.schemata.get(schemaName).isDefined mustBe true
	}
	   
   	////////////////
	   "initialize from sole single-arg constructor" in {
	      
   	    val schema = s"""
{                                                     
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   		   'name':'singleArgNoInit',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHookSingleArgOnly'
            // no init property ok
   	   },
         {                                                              
   		   'name':'singleArgNullInit',
   			'class':'com.variant.server.test.hooks.StateParsedHookSingleArgOnly',    
            'init': null // explicit null ok
   	   }                                                         
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
   ],                                                                   
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
				   'variants':[                                            
				      {'experienceRef':'B'}
			      ]                                                       
	         }                                                          
	      ]                                                             
	   }
   ]                                                                   
}"""

      val schemaDeployer = SchemaDeployer.fromString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      response.getMessages.size mustBe 0

   	server.schemata.get(schemaName).isDefined mustBe true
	   }
	   
	   ////////////////
      "initialize from nullary constructor if both available" in {
	      
   	    val schema = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   		   'name':'bothNoInit',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHook2Constructors'
            // no init property ok
   	   },
         {                                                              
   		   'name':'bothOnlyNullInit',
   			'class':'com.variant.server.test.hooks.StateParsedHook2Constructors',    
            'init': null // explicit null ok
   	   }                                                         
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
   ],                                                                   
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
				   'variants':[                                            
				      {'experienceRef':'B'}
			      ]                                                       
	         }                                                          
	      ]                                                             
	   }
   ]                                                                   
}"""

         val schemaDeployer = SchemaDeployer.fromString(schema)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         response.getMessages.size mustBe 2

         server.schemata.get(schemaName).isDefined mustBe true
         
         var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (StateParsedHook2Constructors.MSG_NULLARY)

         msg = response.getMessages.get(1)
   		msg.getSeverity mustBe INFO
         msg.getText must include (StateParsedHook2Constructors.MSG_NULLARY)
	   }

      	   ////////////////
      "fail if empty object and only nullary construcor" in {
	      
   	    val schema = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   		   'name':'nullaryOnlyEmptyInit',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHookNullaryOnly',
            'init':{}
   	   }
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
   ],                                                                   
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
				   'variants':[                                            
				      {'experienceRef':'B'}
			      ]                                                       
	         }                                                          
	      ]                                                             
	   }
   ]                                                                   
}"""
         val schemaDeployer = SchemaDeployer.fromString(schema)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         response.getMessages.size mustBe 1

         server.schemata.get(schemaName).isDefined mustBe false
         
         var msg = response.getMessages.get(0)
         msg.getSeverity mustBe ERROR
         msg.getText mustBe ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.StateParsedHookNullaryOnly")
         msg.getLocation mustBe null  // This should not be null -- bug 99
	   }

	}
   
  /*
   * 
   */
	"Stateful Hook" should {
	   
	   ////////////////
	   "initialize from one-arg constructors" in {
	      
   	    val schema = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   		   'name':'soleOneArgConstructor',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHookSingleArgOnly',
            'init':{}            
   	   },
         {                                                              
   		   'name':'bothConstructors',
   			'class':'com.variant.server.test.hooks.StateParsedHook2Constructors',    
            'init': {}
   	   }                                                         
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
   ],                                                                   
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
				   'variants':[                                            
				      {'experienceRef':'B'}
			      ]                                                       
	         }                                                          
	      ]                                                             
	   }
   ]                                                                   
}"""

         val schemaDeployer = SchemaDeployer.fromString(schema)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         response.getMessages.size mustBe 1

         server.schemata.get(schemaName).isDefined mustBe true
   	   
         var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (StateParsedHook2Constructors.MSG_SINGLE_ARG)

      }
	   
	   ////////////////
	   "fail if no one-arg constructor" in {
	      
   	    val schema = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   		   'name':'soleOneArgConstructor',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHookNullaryOnly',
            'init':{'foo':'bar'}            
   	   }
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
   ],                                                                   
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
				   'variants':[                                            
				      {'experienceRef':'B'}
			      ]                                                       
	         }                                                          
	      ]                                                             
	   }
   ]                                                                   
}"""

         val schemaDeployer = SchemaDeployer.fromString(schema)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         response.getMessages.size mustBe 1

         server.schemata.get(schemaName).isDefined mustBe false
   	   
         var msg = response.getMessages.get(0)
         msg.getSeverity mustBe ERROR
         msg.getText mustBe ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.StateParsedHookNullaryOnly")
         msg.getLocation mustBe null  // This should not be null -- bug 99

      }

	}
}
