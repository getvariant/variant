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
import com.variant.server.impl.SessionImpl
import com.variant.server.test.hooks.TestTargetingHookNil
import com.variant.server.test.hooks.TestQualificationHookNil
import com.variant.server.schema.SchemaDeployerString
import play.api.Application
import org.scalatest.TestData
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Configuration
import com.variant.server.boot.VariantApplicationLoader
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerTest
import com.variant.server.boot.VariantServer

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class EventFlusherTest extends PlaySpec with OneAppPerTest {

  /**
   * This will implicitely rebuild the server before each test.
   */
   implicit override def newAppForTest(testData: TestData): Application = {

      if (testData.name.contains("EVENT_FLUSHER_CLASS_NAME")) 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config.withoutPath("variant.event.flusher.class")))
            .build() 
      else 
         new GuiceApplicationBuilder()
            .configure(new Configuration(VariantApplicationLoader.config))
            .build()
   }


  /*
   * 
   */
	"Schema with no flusher" should {

    val schemaNoFlusherSrc = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'FlusherTest'
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
	   }
   ]                                                                   
}"""

	   //-\\
    "default to one defined in conf." in {    

      val schemaDeployer = SchemaDeployerString(schemaNoFlusherSrc)
      val server = VariantServer.instance
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponse
   		response.getMessages.size mustBe 0
   		server.schema.isDefined mustBe true
   		val schema = server.schema.get
   		schema.getFlusher() mustBe null
   		
   		// As defined in conf-test/variant.conf
   		schema.eventWriter.flusher.getClass mustBe classOf[com.variant.server.api.EventFlusherH2]
   		
    }

    //-\\
    "emit EVENT_FLUSHER_CLASS_NAME if none defined in conf." in {    

      val schemaDeployer = SchemaDeployerString(schemaNoFlusherSrc)
      val server = VariantServer.instance
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponse
   		response.getMessages.size mustBe 1
   		server.schema.isDefined mustBe false
   		
     	var msg = response.getMessages.get(0)
     	msg.getSeverity mustBe ERROR
     	msg.getText mustBe ServerErrorLocal.FLUSHER_NOT_CONFIGURED.asMessage()
   		
    }

  }
}
