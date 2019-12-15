package com.variant.server.test.api

import com.variant.share.error.UserError.Severity._
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.schema.SchemaDeployer
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.impl.SessionImpl
import java.io.PrintWriter
import com.variant.server.test.spec.TempSchemataDir
import com.variant.server.test.hooks.Hook2Constructors

/**
 * TODO: Need to also test annotations.
 * @author Igor
 *
 */
class HookInstantiationTest extends EmbeddedServerSpec with TempSchemataDir {

   // No schemata to start with
   override lazy val schemata = Set.empty

   /*
   *
   */
   "Lifecycle Event Hook" should {

      ////////////////
      "initialize from sole nullary constructor" in {

         val schemaSrc = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'HookNullaryConstructor',
      'hooks':[
         {                           
   			'class':'com.variant.server.test.hooks.HookNullaryConstructor'
            // no init property ok
   	   },
         {                                                              
   			'class':'com.variant.server.test.hooks.HookNullaryConstructor',    
            'init': null // explicit null ok
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
         new PrintWriter(s"${schemataDir}/HookNullaryConstructor.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get("HookNullaryConstructor").isDefined mustBe true
      }

      ////////////////
      "initialize from sole single-arg constructor" in {

         val schemaSrc = """
{
   'meta':{
      'name':'HookArgConstructor',
      'hooks':[
         {
   			'class':'com.variant.server.test.hooks.HookArgConstructor'
            // no init property ok
   	   },
         {
   			'class':'com.variant.server.test.hooks.HookArgConstructor',
            'init': null // explicit null ok
   	   },
         {
   			'class':'com.variant.server.test.hooks.HookArgConstructor',
            'init': {'foo':'bar'}
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
         new PrintWriter(s"${schemataDir}/HookArgConstructor.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get("HookArgConstructor").isDefined mustBe true
      }

      ////////////////
      "initialize from nullary constructor if both available" in {

         val schemaSrc = """
{
   'meta':{
      'name':'Hook2Constructors1',
      'hooks':[
         {
   			'class':'com.variant.server.test.hooks.Hook2Constructors'
            // no init property ok
   	   },
         {
   			'class':'com.variant.server.test.hooks.Hook2Constructors',
            'init': null // explicit null ok
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
         new PrintWriter(s"${schemataDir}/Hook2Constructors1.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get("Hook2Constructors1").isDefined mustBe true

         // Confirm runtime hooks were posted.
         val schema = server.schemata.get("Hook2Constructors1").get.liveGen.get
         val state1 = schema.getState("state1").get
         val test = schema.getVariation("test1").get
         val ssn = SessionImpl.empty(newSid(), schema)
         ssn.getAttributes.size mustBe 0

         // This should add two attrs with random keys but same values
         val req = ssn.targetForState(state1)
         ssn.getAttributes.size mustBe 2
         ssn.getAttributes.values.forEach(_ mustBe Hook2Constructors.MSG_NULLARY)

      }

      ////////////////
      "initialize from non-nullary constructor if both available" in {

         val schemaSrc = """
{
   'meta':{
      'name':'Hook2Constructors2',
      'hooks':[
         {
   			'class':'com.variant.server.test.hooks.Hook2Constructors',
            'init': {'foo':'bar'}
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
         new PrintWriter(s"${schemataDir}/Hook2Constructors2.schema") {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get("Hook2Constructors2").isDefined mustBe true

         // Confirm runtime hooks were posted.
         val schema = server.schemata.get("Hook2Constructors2").get.liveGen.get
         val state1 = schema.getState("state1").get
         val test = schema.getVariation("test1").get
         val ssn = SessionImpl.empty(newSid(), schema)
         ssn.getAttributes.size mustBe 0

         // This should add two attrs with random keys but same values
         val req = ssn.targetForState(state1)
         ssn.getAttributes.size mustBe 1
         ssn.getAttributes.values.forEach(_ mustBe Hook2Constructors.MSG_SINGLE_ARG)

      }
   }
}
