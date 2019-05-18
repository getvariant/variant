package com.variant.server.test.api


import com.variant.server.api.StateRequest.Status._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.schema.SchemaDeployer
import com.variant.server.boot.VariantServer
import play.api.test.Helpers._
import play.api.libs.json._
import scala.io.Source
import java.io.File
import java.util.Optional

/**
 * TODO: Need to also test annotations.
 * @author Igor
 * 
 */
class EventFlusherCsvTest extends EmbeddedServerSpec {

   val emptyTargetingTrackerBody = "{\"tt\":[]}"
	
	/*
    * 
    */
	"Flusher spec without init" should {

	   val schemaSrc = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'FlusherTest',
      'flusher': {
        'class':'com.variant.extapi.std.flush.TraceEventFlusherCsv'  
       }
   },                                                                   
	'states':[{'name':'state1'},{'name':'state2'}],                                                                   
	'variations':[
	   {                                                                
		   'name':'test1',
	      'experiences':[                                               
            {                                                          
				   'name':'A',                                             
				   'weight':1,                                            
				   'isControl':true
	         },                                                         
		      {                                                          
		         'name':'B',                                             
				   'weight':100                                             
				}                                                          
	      ],
			'onStates':[
			   {                                                          
				   'stateRef':'state1'
	         },
			   {                                                          
				   'stateRef':'state2',                                     
				   'variants':[                                            
				      {'experienceRef':'B'}
			      ]                                                       
	         }	                                               
	      ]                       
	   }
   ]                                                                   
}"""
		val defaultFileName = "variant-events.csv";
	   
	   //-\\
    	"use defaults" in {
   	
	      val schemaDeployer = SchemaDeployer.fromString(schemaSrc)
	      val server = VariantServer.instance
	      server.useSchemaDeployer(schemaDeployer)
	      val response = schemaDeployer.parserResponses(0)
	   	response.getMessages.size mustBe 0
	
	   	server.schemata.get("FlusherTest").isDefined mustBe true
	   	val schema = server.schemata.get("FlusherTest").get.liveGen.get
	   	schema.getMeta.getFlusher.get.getClassName mustBe "com.variant.extapi.std.flush.TraceEventFlusherCsv"
	   	schema.getMeta.getFlusher.get.getInit mustBe Optional.empty
			schema.getMeta.getComment mustBe Optional.empty
			
	      // the default csv file should have already been created.
	   	Source.fromFile(defaultFileName).getLines().mkString mustBe ""
	
	      // create a new session.
	 		var sid = newSid
	 		
	      assertResp(route(app, httpReq(POST, "/session/FlusherTest/" + sid).withBody(emptyTargetingTrackerBody)))
	 			.isOk
	         .withBodySession { ssn => sid = ssn.getId }
		
	      
	      // Target session for "state1"
	      var reqBody = Json.obj(
	      	"sid" -> sid,
	         "state" -> "state1"
	       ).toString
	         
	      // Target and commit the request with no attributes.
	  		assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody)))
	      	.isOk

         reqBody = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString
           
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody)))
            .isOk

	      // Target again and fail the request with custom attributes.
	      reqBody = Json.obj(
	      	"sid" -> sid,
	         "state" -> "state2"
	       ).toString
	  		assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody)))
	      	.isOk

         reqBody = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
            ).toString
           
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody)))
            .isOk

         val eventWriterMaxDelayMillis = schema.eventWriter.maxDelayMillis
	      eventWriterMaxDelayMillis mustBe 2000
	      
	      Thread.sleep(eventWriterMaxDelayMillis * 2)
	
	      val lines = Source.fromFile(defaultFileName).getLines()
	      
	      val event1 = lines.next()
	      event1 must startWith regex
   			s""""[a-fA-F0-9]+","\\$$STATE_VISIT","[\\d\\.\\:\\-TZ]*","${sid}","\\$$STATUS=Committed;\\$$STATE=state1","test1","(A|B)","((true)|(false))""""
	      
	      val event2 = lines.next()
	      event2 must startWith regex
   			s""""[a-fA-F0-9]+","\\$$STATE_VISIT","[\\d\\.\\:\\-TZ]*","${sid}","key1=val1;key2=val2;\\$$STATE=state2;\\$$STATUS=Failed","test1","(A|B)","((true)|(false))""""

	      lines.hasNext mustBe false
      }
 	
		"cleanup" in {
			new File(defaultFileName).delete
		}
	}
	
	/*
    * 
    */
	"Flusher spec with init" should {

   	val schemaSrc = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'FlusherTest',
      'flusher': {
        'class':'com.variant.extapi.std.flush.TraceEventFlusherCsv',
        'init':{'header':true, 'file':'/tmp/EventFlusherCsvTest.csv'}
       }
   },                                                                   
	'states':[{'name':'state1'},{'name':'state2'}],                                                                   
	'variations':[
	   {                                                                
		   'name':'test1',
	      'experiences':[                                               
            {                                                          
				   'name':'A',                                             
				   'weight':1,                                            
				   'isControl':true
	         },                                                         
		      {                                                          
		         'name':'B',                                             
				   'weight':100                                             
				}                                                          
	      ],
			'onStates':[
			   {                                                          
				   'stateRef':'state1'
	         },
			   {                                                          
				   'stateRef':'state2',                                     
				   'variants':[                                            
				      {'experienceRef':'B'}
			      ]                                                       
	         }	                                               
	      ]                       
	   }
   ]                                                                   
}"""
  		 val fileName = "/tmp/EventFlusherCsvTest.csv";

	   //-\\
    	"override defaults" in {
   	   
	      val schemaDeployer = SchemaDeployer.fromString(schemaSrc)
	      val server = VariantServer.instance
	      server.useSchemaDeployer(schemaDeployer)
	      val response = schemaDeployer.parserResponses(0)
	   	response.getMessages.size mustBe 0
	
	   	server.schemata.get("FlusherTest").isDefined mustBe true
	   	val schema = server.schemata.get("FlusherTest").get.liveGen.get
	   	schema.getMeta.getFlusher.get.getClassName mustBe "com.variant.extapi.std.flush.TraceEventFlusherCsv"
	   	schema.getMeta.getFlusher.get.getInit mustBe Optional.of(s"""{"header":true,"file":"${fileName}"}""")
	   	schema.getMeta.getComment mustBe Optional.empty
	
	      // The csv file should have already been created with the header in it.
	      var lines = Source.fromFile(fileName).getLines()

	      val header = lines.next()
	      header must fullyMatch regex
				""""event_id","event_name","created_on","session_id","attributes","variation","experience","is_control""""
	
	      // create a new session.
	 		var sid = newSid
	 		
	      assertResp(route(app, httpReq(POST, "/session/FlusherTest/" + sid).withBody(emptyTargetingTrackerBody)))
	 			.isOk
	         .withBodySession { ssn => sid = ssn.getId }
		
	      
	      // Target session for "state1"
	      var reqBody = Json.obj(
	      	"sid" -> sid,
	         "state" -> "state1"
	       ).toString
	         
	      // Target and commit the request with no attributes.
	  		assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody)))
	      	.isOk

         reqBody = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString
           
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody)))
            .isOk

	      // Target again and fail the request with custom attributes.
	      reqBody = Json.obj(
	      	"sid" -> sid,
	         "state" -> "state2"
	       ).toString
	  		assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody)))
	      	.isOk

         reqBody = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1"->"val1", "key2"->"val2")
            ).toString
           
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody)))
            .isOk

         val eventWriterMaxDelayMillis = schema.eventWriter.maxDelayMillis
	      eventWriterMaxDelayMillis mustBe 2000
	      
	      Thread.sleep(eventWriterMaxDelayMillis * 2)
	
	      lines = Source.fromFile(fileName).getLines()
	      lines.next() // skip the header we alread checked.

			val event1 = lines.next()
	      event1 must startWith regex
   			s""""[a-fA-F0-9]+","\\$$STATE_VISIT","[\\d\\.\\:\\-TZ]*","${sid}","\\$$STATUS=Committed;\\$$STATE=state1","test1","(A|B)","((true)|(false))""""
	      
	      val event2 = lines.next()
	      event2 must startWith regex
   			s""""[a-fA-F0-9]+","\\$$STATE_VISIT","[\\d\\.\\:\\-TZ]*","${sid}","key1=val1;key2=val2;\\$$STATE=state2;\\$$STATUS=Failed","test1","(A|B)","((true)|(false))""""

	      lines.hasNext mustBe false
      }
 	
		"cleanup" in {
			new File(fileName).delete
		}
	}
}
