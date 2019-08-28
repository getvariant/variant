package com.variant.server.test.api

import com.variant.core.error.UserError.Severity._
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import java.io.PrintWriter
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.test.util.ServerLogTailer.Level._

/**
 * @author Igor
 *
 */
class HookInstantiationExceptionTest extends EmbeddedServerSpec with TempSchemataDir {

   // No schemata to start with
   override lazy val schemata = Set.empty

   /**
    *
    */
   "Any hook" should {

      "emit HOOK_CLASS_NO_INTERFACE if class doesn't implement Userook" in {

         val schemaSrc = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'HookNoInterface',
      'hooks':[                                                         
         {                         
   			'class':'com.variant.server.test.hooks.HookNoInterface'     
   	   }                                                              
      ]                                                                
   },                                                                   
	'states':[{'name':'state1'}],                                                                   
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
				   'variants':[{'experienceRef':'B'} ]                                                       
	         }                                                          
	      ]                                                             
	   }                                                               
   ]                                                                   
}"""

         // Write this string
         new PrintWriter(s"${schemataDir}/HookNoInterface.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe HOOK_CLASS_NO_INTERFACE.asMessage("com.variant.server.test.hooks.HookNoInterface", "com.variant.server.api.lifecycle.LifecycleHook")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage("HookNoInterface", s"${schemataDir}/HookNoInterface.schema")
      }
   }

   /**
    *
    */
   "StateParsedHook" should {

      ////////////////////
      "emit OBJECT_INSTANTIATION_ERROR for a non-existent hook class" in {

         val schemaSrc = """
{
   'meta':{
      'name':'BadHookClass',
      'hooks':[
         {
   			'class':'bad.class.name'
   	   }
      ]
   },
	'states':[{'name':'state1'}],
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
				   'variants':[{'experienceRef':'B'} ]
	         }
	      ]
	   }
   ]
}"""

         // Write this string
         new PrintWriter(s"${schemataDir}/BadHookClass.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("bad.class.name", "java.lang.ClassNotFoundException")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage("BadHookClass", s"${schemataDir}/BadHookClass.schema")
      }

      ////////////////////
      "emit OBJECT_CONSTRUCTOR_ERROR for an existing hook class with non-public constructor" in {

         val schemaSrc = """
{
   'meta':{
      'name':'HookPrivateConstructor',
      'hooks':[
         {
   			'class':'com.variant.server.test.hooks.HookPrivateConstructor'
   	   }
      ]
   },
	'states':[{'name':'state1'}],
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
				   'variants':[{'experienceRef':'B'} ]
	         }
	      ]
	   }
   ]
}"""

         // Write this string
         new PrintWriter(s"${schemataDir}/HookPrivateConstructor.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookPrivateConstructor", "java.lang.IllegalAccessException")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage("HookPrivateConstructor", s"${schemataDir}/HookPrivateConstructor.schema")
      }

      ////////////////////
      "emit OBJECT_CONSTRUCTION_ERROR for an existing hook class with wrong signature constructor" in {

         val schemaSrc = """
{
   'meta':{
      'name':'HookWrongSignatureConstructor',
      'hooks':[
         {
   			'class':'com.variant.server.test.hooks.HookWrongSignatureConstructor'
   	   }
      ]
   },
	'states':[{'name':'state1'}],
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
				   'variants':[{'experienceRef':'B'} ]
	         }
	      ]
	   }
   ]
}"""

         // Write this string
         new PrintWriter(s"${schemataDir}/HookWrongSignatureConstructor.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookWrongSignatureConstructor")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage("HookWrongSignatureConstructor", s"${schemataDir}/HookWrongSignatureConstructor.schema")
      }

      ////////////////////
      "emit HOOK_STATE_SCOPE_VIOLATION when qualification hoook is defined at state level" in {

         val schemaSrc = """
{
   'meta':{
      'name':'TestQualificationHookSimple'
   },
	'states':[
		{
			'name':'state1',
         'hooks':[
            {
      			'class':'com.variant.server.test.hooks.TestQualificationHookSimple',
      			'init':{'value':'should crash'}
      	   }
         ]
		}
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
				   'variants':[{'experienceRef':'B'} ]
	         }
	      ]
	   }
   ]
}"""

         // Write this string
         new PrintWriter(s"${schemataDir}/TestQualificationHookSimple.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe HOOK_STATE_SCOPE_VIOLATION.asMessage("/states[0]/hooks[0]/", "com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage("TestQualificationHookSimple", s"${schemataDir}/TestQualificationHookSimple.schema")
      }
   }

}
