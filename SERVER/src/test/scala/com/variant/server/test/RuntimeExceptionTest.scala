package com.variant.server.test

import com.variant.share.error.ServerError
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.impl.SessionImpl
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.boot.VariantServer
import com.variant.server.test.spec.TempSchemataDir
import java.nio.file.Files
import java.nio.file.Paths

class RuntimeExceptionTest extends EmbeddedServerSpec with TempSchemataDir {

   // Don't copy any files into the temp schemata dir on server startup
   override lazy val schemata = Set.empty

   val schemaName = "RuntimeExceptionTest"

   val schemaSrc = s"""
{
   'meta':{
      'name':'${schemaName}'
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

   Files.write(Paths.get(s"${schemataDir}/${schemaName}.json"), schemaSrc.getBytes)
   Thread.sleep(dirWatcherLatencyMillis)

   "Runtime" should {

      "throw WEIGHT_MISSING" in {

         val lines = ServerLogTailer.last(5)
         //lines.foreach(println)
         server.schemata.get(schemaName).isDefined mustBe true
         val schema = server.schemata.get(schemaName).get.liveGen.get
         val ssn = SessionImpl.empty(newSid(), schema)

         intercept[ServerExceptionRemote] {
            ssn.targetForState(schema.getState("state1").get)
         } mustBe ServerExceptionRemote(ServerError.EXPERIENCE_WEIGHT_MISSING, "test1", "A")

      }
   }
}
