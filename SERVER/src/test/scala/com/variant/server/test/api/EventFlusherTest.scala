package com.variant.server.test.api

import org.scalatest.TestData

import com.variant.share.error.UserError.Severity.ERROR
import com.variant.extapi.std.flush.TraceEventFlusherNull
import com.variant.server.boot.ServerMessageLocal
import com.variant.server.boot.VariantServer
import com.variant.server.schema.SchemaDeployer

import com.variant.server.boot.ServerExceptionLocal
import java.util.Optional
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.TempSchemataDir
import java.io.PrintWriter

/**
 * TODO: Need to also test annotations.
 * @author Igor
 *
 */
class EventFlusherTest extends EmbeddedServerSpec with TempSchemataDir {

   // No schemata to start with
   override lazy val schemata = Set.empty

   /*
   *
   */
   "Schema with no flusher" should {

      val schemaSrc = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'FlusherTest1'
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
      //-\\
      "use defaults" in {

         // Write this string
         val fileName = s"${schemataDir}/flusher-test1.schema"
         new PrintWriter(fileName) {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.isUp mustBe true
         server.schemata.get("FlusherTest1").isDefined mustBe true
         val schema = server.schemata.get("FlusherTest1").get.liveGen.get
         schema.getMeta.getFlusher() mustBe Optional.empty

         // As defined in conf-test/variant.conf
         schema.flusherService.getFlusher.getClass.getName mustBe "com.variant.extapi.std.flush.jdbc.TraceEventFlusherH2"
      }

      "emit EVENT_FLUSHER_CLASS_NAME if none defined in conf." in {

         val caughtEx = intercept[ServerExceptionLocal] {
            reboot { builder =>
                  builder.withoutConfiguration(Seq("variant.event.flusher.class.name"))
            }
         }
         caughtEx.getMessage mustBe (
            new ServerExceptionLocal(
               ServerMessageLocal.CONFIG_PROPERTY_NOT_SET, "variant.event.flusher.class.name").getMessage)

      }
   }

   /*
   *
   */
   "Schema with a flusher" should {

      val schemaSrc = """
{
   'meta':{
      'name':'FlusherTest2',
      'flusher': {
        'class':'com.variant.extapi.std.flush.TraceEventFlusherNull'
       }
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

      //-\\
      "Override the default" in {

         // Restart because we have no server.
         reboot()

         // Write this string
         val fileName = s"${schemataDir}/flusher-test2.schema"
         new PrintWriter(fileName) {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get("FlusherTest2").isDefined mustBe true
         val schema = server.schemata.get("FlusherTest2").get.liveGen.get
         schema.getMeta.getFlusher() mustNot be(null)

         // As defined in conf-test/variant.conf
         schema.flusherService.getFlusher.getClass mustBe classOf[com.variant.extapi.std.flush.TraceEventFlusherNull]

      }
   }
}
