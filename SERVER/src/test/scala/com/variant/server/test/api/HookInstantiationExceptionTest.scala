package com.variant.server.test.api

import com.variant.share.error.UserError.Severity._
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

         val schemaName = "HookNoInterface"
         val schemaSrc = s"""
{                                                                              
   'meta':{                                                             		    	    
      'name':'${schemaName}',
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
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe false

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe HOOK_CLASS_NO_INTERFACE.asMessage("com.variant.server.test.hooks.HookNoInterface", "com.variant.server.api.lifecycle.LifecycleHook")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage(schemaName, s"${schemataDir}/${schemaName}.schema")
      }
   }

   /**
    *
    */
   "StateParsedHook" should {

      ////////////////////
      "emit OBJECT_INSTANTIATION_ERROR for a non-existent hook class" in {

         val schemaName = "BadHookClass"
         val schemaSrc = s"""
{
   'meta':{
      'name':'${schemaName}',
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
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe false

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("bad.class.name", "java.lang.ClassNotFoundException")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage(schemaName, s"${schemataDir}/${schemaName}.schema")
      }

      ////////////////////
      "emit OBJECT_CONSTRUCTOR_ERROR for an existing hook class with non-public constructor" in {

         val schemaName = "HookPrivateConstructor"
         val schemaSrc = s"""
{
   'meta':{
      'name':'${schemaName}',
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
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe false

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookPrivateConstructor", "java.lang.IllegalAccessException")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage(schemaName, s"${schemataDir}/${schemaName}.schema")
      }

      ////////////////////
      "emit OBJECT_CONSTRUCTION_ERROR for an existing hook class with wrong signature constructor" in {

         val schemaName = "HookWrongSignatureConstructor"
         val schemaSrc = s"""
{
   'meta':{
      'name':'${schemaName}',
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
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe false

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookWrongSignatureConstructor")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage(schemaName, s"${schemataDir}/${schemaName}.schema")
      }

      ////////////////////
      "emit HOOK_STATE_SCOPE_VIOLATION when qualification hoook is defined at state level" in {

         val schemaName = "TestQualificationHookSimple"
         val schemaSrc = s"""
{
   'meta':{
      'name':'${schemaName}'
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
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe false

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe HOOK_STATE_SCOPE_VIOLATION.asMessage("/states[0]/hooks[0]/", "com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage(schemaName, s"${schemataDir}/${schemaName}.schema")
      }

      "fail if empty init object and only nullary construcor" in {

         val schemaName = "HookNullaryConstructor1"
         val schemaSrc = s"""
{
   'meta':{
      'name':'${schemaName}',
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
         // Write this string
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe false

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookNullaryConstructor")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage(schemaName, s"${schemataDir}/${schemaName}.schema")
      }

      ////////////////
      "fail if no one-arg constructor" in {

         val schemaName = "HookNullaryConstructor2"
         val schemaSrc = s"""
{
   'meta':{
      'name':'${schemaName}',
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

         // Write this string
         new PrintWriter(s"${schemataDir}/${schemaName}.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get(schemaName).isDefined mustBe false

         val lines = ServerLogTailer.last(2)
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_CONSTRUCTOR_ERROR.asMessage("com.variant.server.test.hooks.HookNullaryConstructor")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage(schemaName, s"${schemataDir}/${schemaName}.schema")

      }
   }
}
