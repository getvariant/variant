package com.variant.server.test

import com.variant.core.error.ServerError
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.boot.VariantServerImpl
import com.variant.server.impl.SessionImpl
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.EmbeddedServerSpec

class RuntimeExceptionTest extends EmbeddedServerSpec {

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
	    	    
   'variations':[
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
   			   'stateRef':'state3'
   		   }
         ] 
      }
   //--------------------------------------------------------------------------//	
	]
}
"""

   "Runtime" should {

      "throw WEIGHT_MISSING" in {

         val schemaDeployer = SchemaDeployer.fromString(schemaSrc)
         server.asInstanceOf[VariantServerImpl].useSchemaDeployer(schemaDeployer)
         val response = schemaDeployer.parserResponses(0)
         server.schemata.get(schemaName).isDefined mustBe true
         val schema = server.schemata.get(schemaName).get.liveGen.get
         val ssn = SessionImpl.empty(newSid(), schema)

         intercept[ServerExceptionRemote] {
            ssn.targetForState(schema.getState("state1").get)
         } mustBe ServerExceptionRemote(ServerError.EXPERIENCE_WEIGHT_MISSING, "test1", "A")

      }
   }
}
