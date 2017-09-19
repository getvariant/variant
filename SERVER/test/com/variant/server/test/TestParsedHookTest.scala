package com.variant.server.test;

import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.core.schema.Test
import org.scalatest.Assertions._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.core.CommonError._
import com.variant.server.api.ServerException
import com.variant.core.schema.parser.ParserMessageImpl
import com.variant.core.schema.parser.ParserError
import com.variant.server.test.hooks.StateParsedHook
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.test.hooks.TestParsedHook
import com.variant.server.schema.SchemaDeployerString

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class TestParsedHookTest extends BaseSpecWithServer {

  /*
   * 
   */
	"Schema scoped TestParsedHook" should {
	   
	   ////////////////
	   "be posted for all tests" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'testParsedHookTest',
      'hooks':[
         {                                                              
   		     'name':'testParsed',                                       
   			   'class':'com.variant.server.test.hooks.TestParsedHook',
           'init':{'hookName':'testParsed', 'clipChain':true}
   	     },
        {                                                              
   		     'name':'stateParsed',                                       
   			   'class':'com.variant.server.test.hooks.StateParsedHook',    
           'init':{'hookName':'stateParsed'}
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
	   }                                                               
   ]                                                                   
}"""

      val schemaDeployer = SchemaDeployerString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponse
   		response.getMessages.size mustBe 9
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 3
   		response.getMessages(WARN).size() mustBe 6
   		response.getMessages(INFO).size() mustBe 9
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed", "state1")))
   		msg = response.getMessages.get(1)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed", "state1")))
   		msg = response.getMessages.get(2)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed", "state1")))
   		msg = response.getMessages.get(3)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsed", "test1")))
   		msg = response.getMessages.get(4)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(TestParsedHook.WARN_MESSAGE_FORMAT, "testParsed", "test1")))
   		msg = response.getMessages.get(5)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(TestParsedHook.ERROR_MESSAGE_FORMAT, "testParsed", "test1")))
   		msg = response.getMessages.get(6)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsed", "test2")))
   		msg = response.getMessages.get(7)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(TestParsedHook.WARN_MESSAGE_FORMAT, "testParsed", "test2")))
   		msg = response.getMessages.get(8)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(TestParsedHook.ERROR_MESSAGE_FORMAT, "testParsed", "test2")))

   		server.schema.isDefined mustBe false
	   }
   }

	/*
   * 
   */
	"Test scoped TestParsedHook" should {
	   
	   ////////////////
	   "be posted for enclosing test only and before any schema scoped hooks" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'testParsedHookTest',
      'hooks':[
         {                                                              
   		     'name':'testParsedS1',                                       
   			   'class':'com.variant.server.test.hooks.TestParsedHook',
           'init':{'hookName':'testParsedS1', 'clipChain':true}
   	     },
        {                                                              
   		     'name':'testParsedS2',                                       
   			   'class':'com.variant.server.test.hooks.TestParsedHook',    
           'init':{'hookName':'testParsedS2', 'clipChain':true}
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
	      ],
        'hooks':[
           {                                                              
     		     'name':'testParsedT11',                                       
     			   'class':'com.variant.server.test.hooks.TestParsedHook',
             'init':{'hookName':'testParsedT11'}
     	     },
          {                                                              
     		     'name':'testParsedT12',                                       
     			   'class':'com.variant.server.test.hooks.TestParsedHook',    
             'init':{'hookName':'testParsedT12', 'clipChain':true}
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
	      ],
        'hooks':[
           {                                                              
     		     'name':'testParsedT21',                                       
     			   'class':'com.variant.server.test.hooks.TestParsedHook',
             'init':{'hookName':'testParsedT21'}
     	     },
          {                                                              
     		     'name':'testParsedT22',                                       
     			   'class':'com.variant.server.test.hooks.TestParsedHook',    
             'init':{'hookName':'testParsedT22'}
     	     }                                                              
        ]                                                                                                                             
	   }                                                               
   ]                                                                   
}"""

      val schemaDeployer = SchemaDeployerString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponse
   		
   		response.getMessages.size mustBe 15
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 5
   		response.getMessages(WARN).size() mustBe 10
   		response.getMessages(INFO).size() mustBe 15
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsedT11", "test1")))
   		msg = response.getMessages.get(1)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(TestParsedHook.WARN_MESSAGE_FORMAT, "testParsedT11", "test1")))
   		msg = response.getMessages.get(2)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(TestParsedHook.ERROR_MESSAGE_FORMAT, "testParsedT11", "test1")))
   		msg = response.getMessages.get(3)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsedT12", "test1")))
   		msg = response.getMessages.get(4)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(TestParsedHook.WARN_MESSAGE_FORMAT, "testParsedT12", "test1")))
   		msg = response.getMessages.get(5)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(TestParsedHook.ERROR_MESSAGE_FORMAT, "testParsedT12", "test1")))
   		msg = response.getMessages.get(6)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsedT21", "test2")))
   		msg = response.getMessages.get(7)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(TestParsedHook.WARN_MESSAGE_FORMAT, "testParsedT21", "test2")))
   		msg = response.getMessages.get(8)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(TestParsedHook.ERROR_MESSAGE_FORMAT, "testParsedT21", "test2")))
   		msg = response.getMessages.get(9)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsedT22", "test2")))
   		msg = response.getMessages.get(10)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(TestParsedHook.WARN_MESSAGE_FORMAT, "testParsedT22", "test2")))
   		msg = response.getMessages.get(11)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(TestParsedHook.ERROR_MESSAGE_FORMAT, "testParsedT22", "test2")))
   		msg = response.getMessages.get(12)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(TestParsedHook.INFO_MESSAGE_FORMAT, "testParsedS1", "test2")))
   		msg = response.getMessages.get(13)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(TestParsedHook.WARN_MESSAGE_FORMAT, "testParsedS1", "test2")))
   		msg = response.getMessages.get(14)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(TestParsedHook.ERROR_MESSAGE_FORMAT, "testParsedS1", "test2")))

   		server.schema.isDefined mustBe false
	   }
   }

	/*
   * 
   */
	"No TestParsedHook" should {
	   
	   ////////////////
	   "be posted for tests which had parser errors" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'stateScopedHooks',                                                          
      'hooks':[                                                         
         {                                                              
   		     'name':'testParsed',                                       
   			   'class':'com.variant.server.test.hooks.TestParsedHook',
           'init':{'hookName':'TestParsedSchema'}     
   	     }
       ]                                                              
   },                                                                   
	'states':[
	    {
        'name':'state1'                                                            
      }
   ],                                                                   
	'tests':[
	   {                                                                
		   'name':'invalid name',  //  Bad name
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
				    	   {                                                    
				    	      'experienceRef':'B',                              
							   'parameters':{                                    
				    	      'path':'/path/to/state1/test1.B'               
				         }                                                 
				     }                                                    
			     ]                                                       
	        }                                                          
	     ]                                                             
	  },
	   {                                                                
		   'name':'test1',
	      'experiences':[                                               
            {                                                          
				   'name':'A',                                             
				   'weight':10                                          
				   //'isControl':true    //No control experience                                        
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
				    	   {                                                    
				    	      'experienceRef':'B',               
							      'parameters':{                                    
				    	      'path':'/path/to/state1/test1.B'               
				         }                                                 
				     }                                                    
			     ]                                                       
	        }                                                          
	     ]                                                             
    },
	   {                                                                
		   'name':'test2',
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
   		     'name':'testParsed',
   			   'class':'com.variant.server.test.hooks.NookNoInterface',  // Will faill
           'init':{'hookName':'stateParsed'}     
   	     },
         {                                                              
   		     'name':'testParsed',
   			   'class':'com.variant.server.test.hooks.TestParsedHook',  // OK, but should not fire
           'init':{'hookName':'stateParsed'}     
   	     }
       ],
			'onStates':[                                                   
				  {                                                          
				     'stateRef':'state1',                                     
				    	'variants':[                                            
				    	   {                                                    
				    	      'experienceRef':'B',                              
							   'parameters':{                                    
				    	      'path':'/path/to/state1/test1.B'               
				         }                                                 
				     }                                                    
			     ]                                                       
	        }                                                          
	     ]                                                             
    }

  ]                                                                   
}"""

      val schemaDeployer = SchemaDeployerString(schema)
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponse
   		response.getMessages.foreach(println(_))
   		response.getMessages.size mustBe 3
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 3
   		response.getMessages(WARN).size() mustBe 3
   		response.getMessages(INFO).size() mustBe 3
   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.TEST_NAME_INVALID.asMessage())
   		msg = response.getMessages.get(1)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.CONTROL_EXPERIENCE_MISSING.asMessage("test1"))
   		msg = response.getMessages.get(2)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_NAME_DUPE.asMessage("testParsed"))

   		server.schema.isDefined mustBe false
	   }
   }

}
