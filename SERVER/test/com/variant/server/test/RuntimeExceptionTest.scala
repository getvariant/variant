package com.variant.server.test

import com.variant.server.boot.ServerErrorLocal._
import com.variant.core.RuntimeError._
import com.variant.server.api.Session
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import com.variant.server.api.ServerException
import com.variant.core.CoreException
import com.variant.server.impl.SessionImpl
import com.variant.server.schema.SchemaDeployer.fromString
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.BaseSpecWithServer

class RuntimeExceptionTest extends BaseSpecWithServer {

   val schemaName = "RuntimeExceptionTest"
   
   val schemaSrc = s"""
{
   'meta':{
      'name':'$schemaName'
   },
   'states':[
      {'name':'state1'},
      {'NAME':'state2'},
   	{'NaMe':'state3'}
   ],		            
	    	    
   'tests':[
      {
         'name':'test1',
         'isOn':true,
   		'experiences':[
   		   {         
   			   'name':'A',
   			   'isControl':true 
   			},
   			{ 
   			   'name':'B' 
   			}
         ],
   		'onStates':[
   		   {
   			   'stateRef':'state1',
   			   'variants':[
   			      {
   			    	   'experienceRef':'B'
   			      }
   			   ]
   			}
         ] 
      },
     //--------------------------------------------------------------------------//	
      {
         'name':'test2', 
   		'isOn': false,
   		'experiences':[
      	   {
      		   'name':'C',
      			'weight':0.5,
      			'isControl':true
   			},
   			{
   			   'name':'D',
   			   'weight':0.6
   			}
         ],
         'onStates':[
            {
               'stateRef':'state1',
   			   'variants':[
   			      {
   			    	   'experienceRef':'D'
   			      }
               ]
   			},
   			{ 
   			   'stateRef':'state2',
   			   'isNonvariant':false,
   			   'variants':[
   			      {    
   			    	   'experienceRef':'D'
   			    	} 
   			   ] 
   			},  
   			{
   			   'stateRef':'state3',
   			   'isNonvariant':true
   		   }
         ] 
      }
   //--------------------------------------------------------------------------//	
	]
}
"""
	
   "Runtime" should {

      "throw STATE_NOT_INSTRUMENTED_BY_TEST" in  {

         val schemaDeployer = SchemaDeployer.fromString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         server.schemata.get(schemaName).isDefined mustBe true
         val schema = server.schemata.get(schemaName).get
         val state2 = schema.getState("state2")

         try {
			   state2.isNonvariantIn(schema.getTest("non-existent"))
			   fail("Expected exception not thrown")
         }
         catch {
            case npe: NullPointerException => // Expected
            case t: Throwable => fail("Unexpected Exception %s".format(t.getMessage))
         }
         
         try {
			   state2.isNonvariantIn(schema.getTest("test1"))
			   fail("Expected exception not thrown")
   		}
	   	catch {
	   	   case uex: CoreException.User =>  uex.getMessage() mustEqual
      			new CoreException.User(STATE_NOT_INSTRUMENTED_BY_TEST, "state2", "test1").getMessage()
		   }
	   	
      }

      "throw WEIGHT_MISSING" in {

         val schemaDeployer = SchemaDeployer.fromString(schemaSrc)
         server.useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         server.schemata.get(schemaName).isDefined mustBe true
         val schema = server.schemata.get(schemaName).get
         val ssn = SessionImpl.empty(newSid(), schema)
   
   		try {
   		   ssn.targetForState(schema.getState("state1"))
			   fail("Expected exception not thrown")
   		}
         catch {
            case uex: ServerException.User =>  uex.getMessage() mustEqual
      			new ServerException.User(EXPERIENCE_WEIGHT_MISSING, "test1", "A").getMessage()
   		}
      }
   }
}
