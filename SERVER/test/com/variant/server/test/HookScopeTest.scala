package com.variant.server.test;

import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.core.schema.Test
import org.scalatest.Assertions._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.core.RuntimeError._
import com.variant.server.api.ServerException
import com.variant.core.schema.parser.ParserMessageImpl
import com.variant.server.test.hooks.StateParsedHook
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.test.hooks.TestParsedHook
import com.variant.server.impl.SessionImpl
import com.variant.server.test.hooks.TestTargetingHookNil
import com.variant.server.test.hooks.TestQualificationHookNil
import com.variant.server.schema.SchemaDeployerString

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class HookScopeTest extends BaseSpecWithServer {

   val schemaName = "HookScopeTest"
  /*
   * 
   */
	"Schema scoped hook" should {
	   
	   ////////////////
	   "never violate scoping rules" in {
	      
   	    val schemaSrc = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName',
      'hooks':[
         {                                                              
            'name':'testParsed',                                       
            'class':'com.variant.server.test.hooks.TestParsedHook',
            'init':{'hookName':'testParsed', 'infoOnly':true}
   	   },
         {                                                              
   		     'name':'stateParsed',                                       
   			   'class':'com.variant.server.test.hooks.StateParsedHook',    
           'init':{'hookName':'stateParsed', 'infoOnly':true}
   	   },                                                             
         {                                                              
   		     'name':'testQualifier',                                       
   			   'class':'com.variant.server.test.hooks.TestQualificationHookNil'
   	   },                                                             
         {                                                              
   		     'name':'testTargeter',                                       
   			   'class':'com.variant.server.test.hooks.TestTargetingHookNil'
          }                                                              
      ]                                                                
   },                                                                   
	'states':[ 
	   {'name':'state1'},
	   {'name':'state2'}                                                 
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
				   'stateRef':'state1',                                     
				   'variants':[                                            
				      {'experienceRef':'B'}  
			      ]                                                       
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
				   'stateRef':'state2',                                     
				   'variants':[                                            
				      {'experienceRef':'B'}  
			      ]                                                       
	         }                                                          
	      ]                                                             
	   }                                                               
   ]                                                                   
}"""

      val schemaDeployer = SchemaDeployerString(schemaSrc)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
      //response.getMessages.foreach(println _)
   		response.getMessages.size mustBe 5
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 0
   		response.getMessages(WARN).size() mustBe 0
   		response.getMessages(INFO).size() mustBe 5
   		
   		// Confirm parse time hooks were posted. Note that compile time hooks fire for off tests.
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (HOOK_USER_MESSAGE_INFO.asMessage(String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed", "state1")))
   		msg = response.getMessages.get(1)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (HOOK_USER_MESSAGE_INFO.asMessage(String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed", "state2")))
   		msg = response.getMessages.get(2)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (HOOK_USER_MESSAGE_INFO.asMessage(String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsed", "test1")))
   		msg = response.getMessages.get(3)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (HOOK_USER_MESSAGE_INFO.asMessage(String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsed", "test2")))
   		msg = response.getMessages.get(4)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (HOOK_USER_MESSAGE_INFO.asMessage(String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsed", "test3")))

   		server.schemata.get(schemaName).isDefined mustBe true
   		
   		// Confirm runtime hooks were posted.
   		val schema = server.schemata(schemaName)
         val state1 = schema.getState("state1")
         val state2 = schema.getState("state2")
         val test = schema.getTest("test1")
         val ssn = SessionImpl.empty(newSid(), schema)
   		ssn.getAttribute(TestQualificationHookNil.ATTR_KEY) mustBe null
   		ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe null

   		ssn.targetForState(state1)
   		// Only test1 is instrumented on state1
   		ssn.getAttribute(TestQualificationHookNil.ATTR_KEY) mustBe "test1"
   		ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe "test1"
   		
   		// Test3 is instrumented on state2
   		ssn.targetForState(state2)
         ssn.getAttribute(TestQualificationHookNil.ATTR_KEY) mustBe "test1 test3"
   		ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe "test1 test3"	   
   		
	   }
   }

  /*
   * 
   */
	"State parsed hook" should {
	   
	   ////////////////
	   "throw state scope violation error if defined at test scope" in {
	      
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
   		     'name':'stateParsed',                                       
   			   'class':'com.variant.server.test.hooks.StateParsedHook',
           'init':{'hookName':'stateParsedS1'}
   	     }
      ]
    },
	  {
      'name':'state2'
    }                                                 
   ],                                                                   
	'tests':[
	   {                                                                
		   'name':'test1',
        'hooks':[
           {                                                              
     		     'name':'stateParsed',                                       
     			   'class':'com.variant.server.test.hooks.StateParsedHook',
             'init':{'hookName':'stateParsedT1'}
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

      val schemaDeployer = SchemaDeployerString(schemaSrc)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
   		//response.getMessages.foreach(println(_))
   		response.getMessages.size mustBe 4
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 2
   		response.getMessages(WARN).size() mustBe 3
   		response.getMessages(INFO).size() mustBe 4
   		
   		// Confirm parse time hooks were posted. Note that compile time hooks fire for off tests.
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (HOOK_USER_MESSAGE_INFO.asMessage(String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsedS1", "state1")))
   		msg = response.getMessages.get(1)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (HOOK_USER_MESSAGE_WARN.asMessage(String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsedS1", "state1")))
   		msg = response.getMessages.get(2)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (HOOK_USER_MESSAGE_ERROR.asMessage(String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsedS1", "state1")))
   		msg = response.getMessages.get(3)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ServerErrorLocal.HOOK_TEST_SCOPE_VIOLATION.asMessage("stateParsed", "test1", "com.variant.core.lce.StateParsedLifecycleEvent"))

   		server.schemata.get(schemaName).isDefined mustBe false
   		
	   }
   }

  /*
   * 
   */
	"Test parsed hook" should {
	   
	   ////////////////
	   "throw state scope violation error if defined at state scope" in {
	      
   	    val schemaSrc = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'$schemaName'
   },                                                                   
	'states':[ 
    {
      'name':'state1'
    },
	  {
      'name':'state2',
      'hooks':[
         {                                                              
   		     'name':'testParsed',                                       
   			   'class':'com.variant.server.test.hooks.TestParsedHook',
           'init':{'hookName':'testParsedS1'}
   	     }
      ]
    }                                                 
   ],                                                                   
	'tests':[
	   {                                                                
		   'name':'test1',
        'hooks':[
           {                                                              
     		     'name':'testParsed',                                       
     			   'class':'com.variant.server.test.hooks.TestParsedHook',
             'init':{'hookName':'testParsedT1'}
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

      val schemaDeployer = SchemaDeployerString(schemaSrc)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
   		//response.getMessages.foreach(println(_))
   		response.getMessages.size mustBe 4
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 2
   		response.getMessages(WARN).size() mustBe 3
   		response.getMessages(INFO).size() mustBe 4
   		
   		// Confirm parse time hooks were posted. Note that compile time hooks fire for off tests.
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ServerErrorLocal.HOOK_STATE_SCOPE_VIOLATION.asMessage("testParsed", "state2", "com.variant.core.lce.TestParsedLifecycleEvent"))
   		msg = response.getMessages.get(1)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (HOOK_USER_MESSAGE_INFO.asMessage(String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsedT1", "test1")))
   		msg = response.getMessages.get(2)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (HOOK_USER_MESSAGE_WARN.asMessage(String.format(TestParsedHook.WARN_MESSAGE_FORMAT, "testParsedT1", "test1")))
   		msg = response.getMessages.get(3)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (HOOK_USER_MESSAGE_ERROR.asMessage(String.format(TestParsedHook.ERROR_MESSAGE_FORMAT, "testParsedT1", "test1")))

   		server.schemata.get(schemaName).isDefined mustBe false
   		
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
   		     'name':'testTargeter',                                       
   			   'class':'com.variant.server.test.hooks.TestTargetingHookNil'
   	     }                                                              
      ]                                                                
   },                                                                   
	'states':[ 
	   {'name':'state1',
      'hooks':[
        {                                                              
   		     'name':'testTargeter',                                       
   			   'class':'com.variant.server.test.hooks.TestTargetingHookNil'
   	     }                                                              
      ]                                                                

},
	   {'name':'state2'}                                                 
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
      'hooks':[
        {                                                              
   		     'name':'testTargeter',                                       
   			   'class':'com.variant.server.test.hooks.TestTargetingHookNil'
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

         val schemaDeployer = SchemaDeployerString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
  
   	   response.getMessages.size mustBe 0

   		server.schemata.get(schemaName).isDefined mustBe true
   		
   		// Confirm runtime hooks were posted.
   		val schema = server.schemata.get(schemaName).get

         val state1 = schema.getState("state1")
         val test = schema.getTest("test1")
         val ssn = SessionImpl.empty(newSid(), schema)
   		ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe null
   		ssn.targetForState(state1)
   		// Only test1 is instrumented on state1
   		ssn.getAttribute(TestTargetingHookNil.ATTR_KEY) mustBe "test1 test1 test1"  // All three should fire!
   		   		
	   }
   }

		/*
   * 
   */
	"Test qualifying hook" should {
	   
	   ////////////////
	   "throw state scope violation error if defined at state scope" in {
	      
   	    val schemaSrc = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'_',
      'hooks': [
        {                                                              
   		     'name':'$schemaName',                                       
   			   'class':'com.variant.server.test.hooks.TestQualificationHookNil'
   	     }
      ]                                                                
   },                                                                   
	'states':[ 
	   {
       'name':'state1',
       'hooks': [
         {                                                              
   		     'name':'testQualifier',                                       
   			   'class':'com.variant.server.test.hooks.TestQualificationHookNil'
   	     }
       ]                                                                
     },
	   {'name':'state2'}                                                 
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
       'hooks': [
         {                                                              
   		     'name':'testQualifier',                                       
   			   'class':'com.variant.server.test.hooks.TestQualificationHookNil'
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

      val schemaDeployer = SchemaDeployerString(schemaSrc)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)

      //response.getMessages.foreach(println(_))
   		response.getMessages.size mustBe 1

   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ServerErrorLocal.HOOK_STATE_SCOPE_VIOLATION.asMessage("testQualifier", "state1", "com.variant.server.lce.TestQualificationLifecycleEvent"))

   		server.schemata.get(schemaName).isDefined mustBe false
   		
   		   		
	   }
   }


}
