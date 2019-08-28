package com.variant.server.test.api

import com.variant.server.api.StateRequest.Status._
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.schema.SchemaDeployer
import com.variant.server.boot.VariantServer
import play.api.libs.json._
import scala.io.Source
import java.io.File
import java.util.Optional
import com.variant.server.test.spec.TempSchemataDir
import java.io.PrintWriter
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods

/**
 * TODO: Need to also test annotations.
 * @author Igor
 *
 */
class EventFlusherCsvTest extends EmbeddedServerSpec with TempSchemataDir {

   val emptyTargetingTrackerBody = "{\"tt\":[]}"

   // No schemata to start with
   override lazy val schemata = Set.empty

   /*
    *
    */
   "Flusher spec without init" should {

      val schemaSrc = """
{                                                                              
   'meta':{                                                             		    	    
      'name':'FlusherTest1',
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

      "use defaults" in {

         // Write this string
         val fileName = s"${schemataDir}/flusher-test1.schema"
         new PrintWriter(fileName) {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get("FlusherTest1").isDefined mustBe true
         val schema = server.schemata.get("FlusherTest1").get.liveGen.get
         schema.getMeta.getFlusher.get.getClassName mustBe "com.variant.extapi.std.flush.TraceEventFlusherCsv"
         schema.getMeta.getFlusher.get.getInit mustBe Optional.empty
         schema.getMeta.getComment mustBe Optional.empty

         // the default csv file should have already been created.
         Source.fromFile(defaultFileName).getLines().mkString mustBe ""

         // create a new session.
         var sid = newSid

         HttpRequest(method = HttpMethods.POST, uri = s"/session/FlusherTest1/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            sid = ssnResp.session.getId
         }

         // Target session for "state1"
         var reqBody = Json.obj(
            "sid" -> sid,
            "state" -> "state1").toString

         // Target
         HttpRequest(method = HttpMethods.POST, uri = s"/request/FlusherTest1/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
         }

         // commit with no attrs
         reqBody = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/FlusherTest1/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
         }

         // Target again
         reqBody = Json.obj(
            "sid" -> sid,
            "state" -> "state2").toString

         HttpRequest(method = HttpMethods.POST, uri = s"/request/FlusherTest1/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
         }

         // Fail with attrs
         reqBody = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1" -> "val1", "key2" -> "val2")).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/FlusherTest1/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
         }

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
      'name':'FlusherTest2',
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
      val outFileName = "/tmp/EventFlusherCsvTest.csv";

      "override defaults" in {

         // Write this string
         val fileName = s"${schemataDir}/flusher-test2.schema"
         new PrintWriter(fileName) {
            write(schemaSrc)
            close
         }

         Thread.sleep(dirWatcherLatencyMillis)

         server.schemata.get("FlusherTest2").isDefined mustBe true
         val schema = server.schemata.get("FlusherTest2").get.liveGen.get
         schema.getMeta.getFlusher.get.getClassName mustBe "com.variant.extapi.std.flush.TraceEventFlusherCsv"
         schema.getMeta.getFlusher.get.getInit mustBe Optional.of(s"""{"header":true,"file":"${outFileName}"}""")
         schema.getMeta.getComment mustBe Optional.empty

         // The csv file should have already been created with the header in it.
         var lines = Source.fromFile(outFileName).getLines()

         val header = lines.next()
         header must fullyMatch regex
            """"event_id","event_name","created_on","session_id","attributes","variation","experience","is_control""""

         // create a new session.
         var sid = newSid

         HttpRequest(method = HttpMethods.POST, uri = s"/session/FlusherTest2/${sid}", entity = emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            sid = ssnResp.session.getId
         }

         // Target session for "state1"
         var reqBody = Json.obj(
            "sid" -> sid,
            "state" -> "state1").toString

         // Target
         HttpRequest(method = HttpMethods.POST, uri = s"/request/FlusherTest2/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
         }

         // commit with no attrs
         reqBody = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/FlusherTest2/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
         }

         // Target again
         reqBody = Json.obj(
            "sid" -> sid,
            "state" -> "state2").toString

         HttpRequest(method = HttpMethods.POST, uri = s"/request/FlusherTest2/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
         }

         // Fail with attrs
         reqBody = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal,
            "attrs" -> Map("key1" -> "val1", "key2" -> "val2")).toString

         HttpRequest(method = HttpMethods.DELETE, uri = s"/request/FlusherTest2/${sid}", entity = reqBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
         }

         val eventWriterMaxDelayMillis = schema.eventWriter.maxDelayMillis
         eventWriterMaxDelayMillis mustBe 2000

         Thread.sleep(eventWriterMaxDelayMillis * 2)

         lines = Source.fromFile(outFileName).getLines()
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
         new File(outFileName).delete
      }
   }
}
