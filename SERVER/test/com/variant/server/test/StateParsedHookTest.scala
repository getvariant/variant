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
import com.variant.server.session.ServerSession
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
class StateParsedHookTest extends BaseSpecWithServer {
	   
	"StateParsedHook" should {
	   
	   ////////////////
	   "be posted when a state is parsed" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[                                                         
         {                                                              
   		   'name':'stateParsed',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHook'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'},                                                
		{'name':'state2'},                                                
		{'name':'state3'}                                                 
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

   		val response = server.installSchemaDeployer(SchemaDeployer.fromString(schema)).get
   		response.getMessages.size mustBe 9
//   		response.getMessages.foreach(println(_))
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
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed", "state2")))
   		msg = response.getMessages.get(4)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed", "state2")))
   		msg = response.getMessages.get(5)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed", "state2")))
   		msg = response.getMessages.get(6)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed", "state3")))
   		msg = response.getMessages.get(7)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT,  "stateParsed", "state3")))
   		msg = response.getMessages.get(8)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed",  "state3")))

   		server.schema.isDefined mustBe false
	   }
	
		////////////////
	   "be posted by multiple hooks" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[
         {                                                              
   		   'name':'stateParsed1',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHook'     
   	   },                                                             
         {                                                              
   		   'name':'stateParsed2',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHook'     
   	   },                                                             
         {                                                              
   		   'name':'stateParsed3',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHook'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[                                                          
	   {'name':'state1'},                                                
		{'name':'state2'},                                                
		{'name':'state3'}                                                 
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

   		val response = server.installSchemaDeployer(SchemaDeployer.fromString(schema)).get
   		//response.getMessages.foreach(println(_))
   		response.getMessages.size mustBe 27
   		response.getMessages(FATAL) mustBe empty
   		response.getMessages(ERROR).size() mustBe 9
   		response.getMessages(WARN).size() mustBe 18
   		response.getMessages(INFO).size() mustBe 27

   		var msg = response.getMessages.get(0)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed1", "state1")))
   		msg = response.getMessages.get(1)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed1", "state1")))
   		msg = response.getMessages.get(2)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed1", "state1")))
   		msg = response.getMessages.get(3)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed2", "state1")))
   		msg = response.getMessages.get(4)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed2", "state1")))
   		msg = response.getMessages.get(5)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed2", "state1")))
   		msg = response.getMessages.get(6)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed3", "state1")))
   		msg = response.getMessages.get(7)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed3", "state1")))
   		msg = response.getMessages.get(8)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed3", "state1")))

   		msg = response.getMessages.get(9)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed1", "state2")))
   		msg = response.getMessages.get(10)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed1", "state2")))
   		msg = response.getMessages.get(11)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed1", "state2")))
   		msg = response.getMessages.get(12)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed2", "state2")))
   		msg = response.getMessages.get(13)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed2", "state2")))
   		msg = response.getMessages.get(14)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed2", "state2")))
   		msg = response.getMessages.get(15)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed3", "state2")))
   		msg = response.getMessages.get(16)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed3", "state2")))
   		msg = response.getMessages.get(17)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed3", "state2")))

   		msg = response.getMessages.get(18)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed1", "state3")))
   		msg = response.getMessages.get(19)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed1", "state3")))
   		msg = response.getMessages.get(20)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed1", "state3")))
   		msg = response.getMessages.get(21)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed2", "state3")))
   		msg = response.getMessages.get(22)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed2", "state3")))
   		msg = response.getMessages.get(23)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed2", "state3")))
   		msg = response.getMessages.get(24)
   		msg.getSeverity mustBe INFO
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(INFO, String.format(StateParsedHook.INFO_MESSAGE_FORMAT, "stateParsed3", "state3")))
   		msg = response.getMessages.get(25)
   		msg.getSeverity mustBe WARN
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(WARN, String.format(StateParsedHook.WARN_MESSAGE_FORMAT, "stateParsed3", "state3")))
   		msg = response.getMessages.get(26)
   		msg.getSeverity mustBe ERROR
   		msg.getText must include (ParserError.HOOK_MESSAGE.asMessage(ERROR, String.format(StateParsedHook.ERROR_MESSAGE_FORMAT, "stateParsed3", "state3")))

   		server.schema.isDefined mustBe false
	   }
   }
}
