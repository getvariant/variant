package com.variant.server.test.api





import com.variant.core.error.UserError.Severity._
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.boot.ServerErrorLocal._
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.impl.SessionImpl
import com.variant.server.test.hooks.Hook2Constructors

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class HookInstantiationTest extends EmbeddedServerSpec {

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
   			'class':'com.variant.server.test.hooks.HookNullaryConstructor'
            // no init property ok
   	   },
         {                                                              
   			'class':'com.variant.server.test.hooks.HookNullaryConstructor',    
            'init': null // explicit null ok
   	   }                                                         
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
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
   			'class':'com.variant.server.test.hooks.HookArgConstructor'
            // no init property ok
   	   },
         {                                                              
   			'class':'com.variant.server.test.hooks.HookArgConstructor',    
            'init': null // explicit null ok
   	   },
         {                                                              
   			'class':'com.variant.server.test.hooks.HookArgConstructor',    
            'init': {'foo':'bar'}
   	   }                                                         
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
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
	      
   	    val schemaSrc = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   			'class':'com.variant.server.test.hooks.Hook2Constructors'
            // no init property ok
   	   },
         {                                                              
   			'class':'com.variant.server.test.hooks.Hook2Constructors',    
            'init': null // explicit null ok
   	   }                                                         
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
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
				   'variants':[                                            
				      {'experienceRef':'B'}
			      ]                                                       
	         }                                                          
	      ]                                                             
	   }
   ]                                                                   
}"""

         val schemaDeployer = SchemaDeployer.fromString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         response.getMessages.size mustBe 0
         server.schemata.get(schemaName).isDefined mustBe true
         
         // Confirm runtime hooks were posted.
   		val schema = server.schemata.get(schemaName).get.liveGen.get
         val state1 = schema.getState("state1").get
         val test = schema.getVariation("test1").get
         val ssn = SessionImpl.empty(newSid(), schema)         
   		ssn.getAttributes.size mustBe 0

   		// This should add two attrs with random keys but same values
   	   val req = ssn.targetForState(state1)
   	   ssn.getAttributes.size mustBe 2
   		ssn.getAttributes.values.forEach(_ mustBe Hook2Constructors.MSG_NULLARY)

	   }

      	   ////////////////
      "fail if empty object and only nullary construcor" in {
	      
   	    val schema = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   			'class':'com.variant.server.test.hooks.HookNullaryConstructor',
            'init':{}
   	   }
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
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
         msg.getText mustBe ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookNullaryConstructor")
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
   			'class':'com.variant.server.test.hooks.HookNullaryConstructor',
            'init':{}   // Won't work
   	   },
         {                                                              
   			'class':'com.variant.server.test.hooks.Hook2Constructors',    
            'init': {}
   	   }                                                         
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
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
   		msg.getText mustBe ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookNullaryConstructor")
         msg.getLocation mustBe null  // This should not be null -- bug 99

      }
	   
	   ////////////////
	   "fail if no one-arg constructor" in {
	      
   	    val schema = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   			'class':'com.variant.server.test.hooks.HookNullaryConstructor',
            'init':{'foo':'bar'}            
   	   }
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'}                                                 
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

         response.getMessages.size mustBe 1

         server.schemata.get(schemaName).isDefined mustBe false

         var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText mustBe ServerErrorLocal.OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookNullaryConstructor")
         msg.getLocation mustBe null  // This should not be null -- bug 99

      }

	}
}
