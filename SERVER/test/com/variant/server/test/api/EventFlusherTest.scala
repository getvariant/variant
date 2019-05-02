package com.variant.server.test.api

import org.scalatest.TestData
import org.scalatestplus.play.OneAppPerTest
import org.scalatestplus.play.PlaySpec

import com.variant.core.error.UserError.Severity.ERROR
import com.variant.extapi.std.flush.TraceEventFlusherNull
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.boot.VariantServer
import com.variant.server.play.VariantApplicationLoader
import com.variant.server.schema.SchemaDeployer

import play.api.Application
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import com.variant.server.api.ConfigKeys
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import com.variant.server.boot.ServerExceptionLocal

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class EventFlusherTest extends PlaySpec with GuiceOneAppPerTest {

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

   /**
    * Above built server should deploy 2 schemas with not errors.
    */
   "Server should come up with a valid schema" in {
      val server = app.injector.instanceOf[VariantServer]
      server.schemata.size mustBe 3
      server.schemaDeployer.parserResponses.size mustBe 3
      server.schemaDeployer.parserResponses.foreach { _.getMessages.size() mustBe 0 }
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
    "default to one defined in conf." in {    

      val schemaDeployer = SchemaDeployer.fromString(schemaNoFlusherSrc)
      val server = VariantServer.instance
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
   		response.getMessages.size mustBe 0
   		server.schemata.get("FlusherTest").isDefined mustBe true
   		val schema = server.schemata.get("FlusherTest").get.liveGen.get
   		schema.getMeta.getFlusher() mustBe null
   		
   		// As defined in conf-test/variant.conf
   		schema.eventWriter.flusher.getClass.getName mustBe "com.variant.extapi.std.flush.jdbc.TraceEventFlusherH2"
   		
    }

    //-\\ This rebuilds the server with the config missing path variant.event.flusher.class
    "emit EVENT_FLUSHER_CLASS_NAME if none defined in conf." in {    

   	 
      val caughtEx = intercept[ServerExceptionLocal] {
      	SchemaDeployer.fromString(schemaNoFlusherSrc)
      }
      caughtEx.getMessage mustBe (
                     new ServerExceptionLocal(
                           ServerErrorLocal.CONFIG_PROPERTY_NOT_SET, "variant.event.flusher.class.name").getMessage)
      
       VariantServer.instance.isUp mustBe false   		
    }

  }
	
  /*
   * 
   */
	"Schema with a flusher" should {

    val schemaNoFlusherSrc = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'FlusherTest',
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

      val schemaDeployer = SchemaDeployer.fromString(schemaNoFlusherSrc)
      val server = VariantServer.instance
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
   		response.getMessages.size mustBe 0
   		server.schemata.get("FlusherTest").isDefined mustBe true
   		val schema = server.schemata.get("FlusherTest").get.liveGen.get
   		schema.getMeta.getFlusher() mustNot be (null)
   		
   		// As defined in conf-test/variant.conf
   		schema.eventWriter.flusher.getClass mustBe classOf[com.variant.extapi.std.flush.TraceEventFlusherNull]
   		
    }

  }
}
