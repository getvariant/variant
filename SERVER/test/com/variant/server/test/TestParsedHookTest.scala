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
import com.variant.server.test.hooks.TestParsedHook

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class TestParsedHookTest extends BaseSpecWithServer {
	   
	"TestParsedHook" should {
	   
	   ////////////////
	   "be posted when a test is parsed" in {
	      
   	    val schema = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'allTestsOffTest',
      'hooks':[
         {                                                              
   		   'name':'testParsed',                                       
   			'class':'com.variant.server.test.hooks.TestParsedHook'     
   	   },
         {                                                              
   		   'name':'stateParsed',                                       
   			'class':'com.variant.server.test.hooks.StateParsedHook'     
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

   		val response = server.installSchemaDeployer(SchemaDeployer.fromString(schema)).get
   		response.getMessages.size mustBe 9
   		//response.getMessages.foreach(println(_))
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
}
