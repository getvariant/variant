package com.variant.server.test;

import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.error.UserError.Severity._
import com.variant.core.error.ServerError._
import com.variant.core.schema.Variation
import com.variant.server.api.StateRequest.Status._
import org.scalatest.Assertions._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.server.api.ServerException
import com.variant.core.schema.parser.ParserMessageImpl
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.impl.SessionImpl
import com.variant.server.test.hooks.TestTargetingHookSimple
import com.variant.server.test.hooks.TestQualificationHookSimple
import com.variant.server.schema.SchemaDeployer.fromString
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.impl.StateRequestImpl

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class HookScopeTest extends EmbeddedServerSpec {

   val schemaName = "HookScopeTest"
  /*
   * 
   */
	"Schema scoped hooks" should {
	   
	   ////////////////
	   "be posted in ordinal order" in {
	      
   	    val schemaSrc = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
   		    'init':{'value':'h1'},                                       
   			 'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
   	   },                                                             
         {                                                              
   		    'init':{'value':'h2'},                                       
   			 'class':'com.variant.server.test.hooks.TestQualificationHookSimple'
          },                                                             
         {                                                              
   		    'init':{'value':'h3'},                                       
   			 'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
   	   },                                                             
         {                                                              
   		    'init':{'value':'h4'},                                       
   			 'class':'com.variant.server.test.hooks.TestQualificationHookSimple'
          }                                                              
      ]                                                                
   },                                                                   
	'states':[ 
	   {'name':'state1'},
	   {'name':'state2'}                                                 
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
			    {'stateRef':'state1'},
			    {'stateRef':'state2'}                                                      
	      ]                                                             
	   }, 
	   {                                                                
		   'name':'test2',
         'isOn':false,
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
				   'stateRef':'state1' 
	         }                                                          
	      ]                                                             
	   },                                                              
	   {                                                                
		 'name':'test3',
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
				   'stateRef':'state2'
				}                                                          
	      ]                                                             
	   }                                                               
   ]                                                                   
}"""

      val schemaDeployer = SchemaDeployer.fromString(schemaSrc)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)

   		server.schemata.get(schemaName).isDefined mustBe true
   		
   		val schema = server.schemata.get(schemaName).get.liveGen.get
         val state1 = schema.getState("state1").get
         val state2 = schema.getState("state2").get
         val test1 = schema.getVariation("test1").get
         val ssn = SessionImpl.empty(newSid(), schema)
   		ssn.getAttributes.size mustBe 0

   	   val req = ssn.targetForState(state1)
   		// test1 and test2 are instrumented on state1, but test2 is off
   	   println (ssn.getAttributes.get(TestQualificationHookSimple.ATTR_NAME))
   	   println (ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME))
     		
   	   ssn.getAttributes.get(TestQualificationHookSimple.ATTR_NAME) mustBe "h2.test1 h4.test1"
     		ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe "h1.test1.state1 h3.test1.state1"
    
     		// commit before targeting again.
   		req.asInstanceOf[StateRequestImpl].setStatus(Committed);	

   	   ssn.getAttributes.clear()
   	   
   		// test1 and test3 are instrumented on state2, but they are disjoint, so test3 should always
   	   // raise qualification event, but only raise targeting event if test1 got targeted to control.
   		ssn.targetForState(state2)
         ssn.getAttributes.get(TestQualificationHookSimple.ATTR_NAME) mustBe "h2.test3 h4.test3"
         
   		ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe {
   	   	if (req.getLiveExperience(test1).get == test1.getControlExperience)
   	   		"h1.test3.state2 h3.test3.state2"
   	   	else
   	   		null
   	   }
   		
	   }
   }

  /*
   * 
   */
	"Variation qualification hook" should {
	   
	   ////////////////
	   "throw state scope violation error if defined at state scope" in {
	      
   	    val schemaSrc = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName'
   },                                                                   
	'states':[ 
    {
      'name':'state1',
      'hooks':[
         {
   			'class':'com.variant.server.test.hooks.TestQualificationHookSimple',
         	'init':{'value':'should not work'}
   	   }
      ]
    },
	  {
      'name':'state2'
    }                                                 
   ],                                                                   
	'variations':[
	   {                                                                
		   'name':'test1',
        'hooks':[
           {                                                              
     			 'class':'com.variant.server.test.hooks.TestQualificationHookSimple',
             'init':{'value':'should work'}
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
   		//response.getMessages.foreach(println(_))
   		response.getMessages.size mustBe 1

   		// Confirm parse time hooks were posted. Note that compile time hooks fire for off tests.
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (HOOK_STATE_SCOPE_VIOLATION.asMessage("/states[0]/hooks[0]/", "com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent"))
   		
	   }
   }

	/*
   * 
   */
	"Test targeting hook" should {
	   
	   ////////////////
	   "never violate scoping rules" in {
	      
   	    val schemaSrc = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
        {    
   		  'init':{'value':'meta'},                                       
   		  'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
   	  }                                                              
      ]                                                                
   },                                                                   
	'states':[ 
	   {'name':'state1',
      'hooks':[
        {                                                              
   		  'init':{'value':'state'},                                       
   		  'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
   	     }                                                              
      ]                                                                

},
	   {'name':'state2'}                                                 
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
      'hooks':[
        {                                                              
      		  'init':{'value':'variation'},                                       
   			  'class':'com.variant.server.test.hooks.TestTargetingHookSimple'
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
   		ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe null
   		ssn.targetForState(state1)
   		// Only test1 is instrumented on state1. All 3 should fire in the right order: test -> state -> meta
   		ssn.getAttributes.get(TestTargetingHookSimple.ATTR_NAME) mustBe "variation.test1.state1 state.test1.state1 meta.test1.state1" 
   		   		
	   }
   }
}
