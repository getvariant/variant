package com.variant.server.test

import java.util.Date

import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.util.Random

import com.variant.core.ConnectionStatus._
import com.variant.server.event.ServerEvent
import com.variant.server.impl.SessionImpl
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.server.test.util.EventReader
import com.variant.server.test.util.ParameterizedString

import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.test.Helpers.OK
import play.api.test.Helpers.PUT
import play.api.test.Helpers.contentAsJson
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.route
import play.api.test.Helpers.status
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
		
class EventWriterTest extends BaseSpecWithServer {

   val sessionJson = ParameterizedString("""
      {"sid":"${sid:}",
       "ts": ${ts:%d}, 
       "request": {"state": "state1","status": "OK","committed": true, 
                  "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "tests": ["test1","test2"]
      }
   """.format(System.currentTimeMillis()))
   
   "Event writer" should {

      val schema = server.schemata("big_covar_schema")
      val eventWriter = schema.eventWriter
      val eventReader = EventReader(eventWriter)

      "have expected confuration" in {
         eventWriter.maxBufferSize mustEqual 200
         eventWriter.fullSize mustEqual 100
	      eventWriter.maxDelayMillis mustEqual 2000

      }
    
      var cid: String = null
      
      "obtain a connection" in {
         // POST new connection
         assertResp(route(app, connectionRequest("big_covar_schema")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               cid = (json \ "id").as[String]
               cid mustNot be (null)
            }
      }
      
      "flush an event after EVENT_WRITER_FLUSH_MAX_DELAY_MILLIS" in {
         
         // Save session.
         val sid = newSid
         eventReader.read(e => e.getSessionId == sid).size mustBe 0 
         val body = sessionJson.expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid).withBody(body)))
            .isOk
            .withNoBody
            .withConnStatusHeader(OPEN)
         
         val ssn = server.ssnStore.get(sid, cid).get
         
         val (name, value, timestamp) = (Random.nextString(5), Random.nextString(5), Random.nextLong())
         val se = new ServerEvent(name, value, new Date(timestamp));
         ssn.asInstanceOf[SessionImpl].triggerEvent(se);
         
         // Read events back from the db, but must wait for the asych flusher.
         Thread.sleep(eventWriter.maxDelayMillis * 2)
         val eventsFromDatabase = eventReader.read(e => e.getSessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head
         event.getCreatedOn.getTime mustBe timestamp
         event.getName mustBe name
         event.getValue mustBe value
         event.getSessionId mustBe sid
         event.getEventExperiences.size() mustBe 3
         event.getEventExperiences.foreach(ee => {
            ee.getTestName match {
               case "test1" => {
                  ee.getExperienceName mustBe "A"
                  ee.isControl() mustBe true
               }
               case "test2" => {
                  ee.getExperienceName mustBe "B"
                  ee.isControl() mustBe false
               }
               case "test3" => {
                  ee.getExperienceName mustBe "C"
                  ee.isControl() mustBe false
               }
               case t => throw new RuntimeException("Unexpected test %s".format(t))
            }
         })  
      }

      "not flush before EVENT_WRITER_MAX_DELAY if fewer than EVENT_WRITER_PERCENT_FULL" in {
         
         val sid = newSid
         eventReader.read(e => e.getSessionId == sid).size mustBe 0 
         val body = sessionJson.expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, context + "/session", cid).withBody(body)))
            .isOk
            .withNoBody
            .withConnStatusHeader(OPEN)

         val ssn = server.ssnStore.get(sid, cid).get

         // Ensure the writer buffer is empty.
         eventWriter.flush()

         val startOfWrite = System.currentTimeMillis()
         
         for (i <- 1 to eventWriter.fullSize) { 
            val (name, value, timestamp) = (Random.nextString(5), Random.nextString(5), Random.nextLong())
            val se = new ServerEvent(name, value, new Date(timestamp));
            ssn.asInstanceOf[SessionImpl].triggerEvent(se);
         }
         
         val writeTook = System.currentTimeMillis() - startOfWrite
         assert(writeTook < 500, "Write took too long")
         
         // Wait a bit, but less than max delay - must not have flushed
         // TODO Occasionally, this fails due to a race condition.
         Thread.sleep(200)          
         eventReader.read(e => e.getSessionId == ssn.getId).size mustBe 0
         
         // Read after delay - must be flushed
         Thread.sleep(2000)
         eventReader.read(e => e.getSessionId == ssn.getId).size mustBe eventWriter.fullSize
      }

      "flush before EVENT_WRITER_MAX_DELAY if EVENT_WRITER_PERCENT_FULL" in {
         
         val sid = newSid
         eventReader.read(e => e.getSessionId == sid).size mustBe 0 
         val body = sessionJson.expand("sid" -> sid)
         assertResp(route(app,connectedRequest(PUT, context + "/session", cid).withBody(body)))
            .isOk
            .withNoBody
            .withConnStatusHeader(OPEN)

         val ssn = server.ssnStore.get(sid, cid).get
         
         val startOfWrite = System.currentTimeMillis()

         for (i <- 1 to eventWriter.fullSize + 1) { 
            val (name, value, timestamp) = (Random.nextString(5), Random.nextString(5), Random.nextLong())
            val se = new ServerEvent(name, value, new Date(timestamp));
            ssn.asInstanceOf[SessionImpl].triggerEvent(se);
         }
         
         val writeTook = System.currentTimeMillis() - startOfWrite
         assert(writeTook < 500, "Write took too long")
         
         // Wait a bit, but less than max delay - must be flushed
         Thread.sleep(eventWriter.maxDelayMillis - 1000)          
         eventReader.read(e => e.getSessionId == ssn.getId).size mustBe (eventWriter.fullSize + 1)
      }
   }
}