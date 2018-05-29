package com.variant.server.test;

import com.variant.core.schema.State
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import com.variant.core.UserError.Severity._
import com.variant.core.schema.Test
import org.scalatest.Assertions._
import com.variant.server.boot.ServerErrorLocal._
import com.variant.server.api.ServerException
import com.variant.core.schema.parser.ParserMessageImpl
import com.variant.server.test.hooks.StateParsedHook
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.test.hooks.TestParsedHook
import com.variant.server.impl.SessionImpl
import com.variant.server.test.hooks.TestTargetingHookNil
import com.variant.server.test.hooks.TestQualificationHookNil
import com.variant.server.schema.SchemaDeployer.fromString
import play.api.Application
import org.scalatest.TestData
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Configuration
import com.variant.server.boot.VariantApplicationLoader
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerTest
import com.variant.server.boot.VariantServer
import com.variant.server.schema.SchemaDeployer

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

      // Petclinic schema will no parse without this.
      sys.props +=("variant.ext.dir" -> "distr/ext")
      
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
      server.schemata.size mustBe 2 
      server.schemaDeployer.parserResponses.size mustBe 2
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

      val schemaDeployer = SchemaDeployer.fromString(schemaNoFlusherSrc)
      val server = VariantServer.instance
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
   		response.getMessages.size mustBe 0
   		server.schemata.get("FlusherTest").isDefined mustBe true
   		val schema = server.schemata("FlusherTest")
   		schema.getFlusher() mustBe null
   		
   		// As defined in conf-test/variant.conf
   		schema.eventWriter.flusher.getClass mustBe classOf[com.variant.server.api.EventFlusherH2]
   		
    }

    //-\\
    "emit EVENT_FLUSHER_CLASS_NAME if none defined in conf." in {    

      val schemaDeployer = SchemaDeployer.fromString(schemaNoFlusherSrc)
      val server = VariantServer.instance
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
   		response.getMessages.size mustBe 1
   		server.schemata.size mustBe 0
   		
     	var msg = response.getMessages.get(0)
     	msg.getSeverity mustBe ERROR
     	msg.getText mustBe ServerErrorLocal.FLUSHER_NOT_CONFIGURED.asMessage()
   		
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
        'class':'com.variant.server.api.EventFlusherNull'  
       }
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
    "Override the default" in {    

      val schemaDeployer = SchemaDeployer.fromString(schemaNoFlusherSrc)
      val server = VariantServer.instance
      server.useSchemaDeployer(schemaDeployer)
      val response = schemaDeployer.parserResponses(0)
   		response.getMessages.size mustBe 0
   		server.schemata.get("FlusherTest").isDefined mustBe true
   		val schema = server.schemata("FlusherTest")
   		schema.getFlusher() mustNot be (null)
   		
   		// As defined in conf-test/variant.conf
   		schema.eventWriter.flusher.getClass mustBe classOf[com.variant.server.api.EventFlusherNull]
   		
    }

  }
}
