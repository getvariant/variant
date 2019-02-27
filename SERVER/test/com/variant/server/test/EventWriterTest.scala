package com.variant.server.test

import java.util.Date

import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.util.Random

import com.variant.server.impl.SessionImpl
import com.variant.server.impl.TraceEventImpl
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.util.TraceEventReader
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
		
class EventWriterTest extends EmbeddedServerSpec {

   val sessionJson = ParameterizedString("""
      {"sid":"${sid:}",
       "ts": ${ts:%d}, 
       "request": {"state": "state1", "status": 1, 
                  "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "tests": ["test1","test2"]
      }
   """.format(System.currentTimeMillis()))
   
   "Event writer" should {

      val schema = server.schemata.get("monstrosity").get.liveGen.get
      val eventWriter = schema.eventWriter
      val eventReader = TraceEventReader(eventWriter)

      "have expected confuration" in {
         eventWriter.maxBufferSize mustEqual 200
         eventWriter.fullSize mustEqual 100
	      eventWriter.maxDelayMillis mustEqual 2000

      }
            
      "flush an event after EVENT_WRITER_FLUSH_MAX_DELAY_MILLIS" in {
         
         // Save session.
         val sid = newSid
         eventReader.read(e => e.sessionId == sid).size mustBe 0 
         val body = sessionJson.expand("sid" -> sid)
         assertResp(route(app, httpReq(PUT, "/session/monstrosity").withBody(body)))
            .isOk
            .withNoBody
         
         val ssn = server.ssnStore.get(sid).get
         
         val name = Random.nextString(5)
         val se = TraceEventImpl.mkTraceEvent(name);
         ssn.asInstanceOf[SessionImpl].triggerEvent(se);
         
         // Read events back from the db, but must wait for the asych flusher.
         val millisWaited = eventWriter.maxDelayMillis * 2
         Thread.sleep(millisWaited)
         val eventsFromDatabase = eventReader.read(e => e.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head
         event.createdOn.getTime mustBe (System.currentTimeMillis() - millisWaited) +- 100
         event.name mustBe name
         event.sessionId mustBe sid
         event.eventExperiences.size mustBe 3
         event.eventExperiences.foreach(ee => {
            ee.testName match {
               case "test1" => {
                  ee.experienceName mustBe "A"
                  ee.isControl mustBe true
               }
               case "test2" => {
                  ee.experienceName mustBe "B"
                  ee.isControl mustBe false
               }
               case "test3" => {
                  ee.experienceName mustBe "C"
                  ee.isControl mustBe false
               }
               case t => throw new RuntimeException("Unexpected test %s".format(t))
            }
         })  
      }

      "not flush before EVENT_WRITER_MAX_DELAY if fewer than EVENT_WRITER_PERCENT_FULL" in {
         
         val sid = newSid
         eventReader.read(e => e.sessionId == sid).size mustBe 0 
         val body = sessionJson.expand("sid" -> sid)
         assertResp(route(app, httpReq(PUT, "/session/monstrosity").withBody(body)))
            .isOk
            .withNoBody

         val ssn = server.ssnStore.get(sid).get

         // Ensure the writer buffer is empty.
         eventWriter.flush()

         val startOfWrite = System.currentTimeMillis()
         
         for (i <- 1 to eventWriter.fullSize) { 
            val name = Random.nextString(5)
            val se = TraceEventImpl.mkTraceEvent(name);
            ssn.asInstanceOf[SessionImpl].triggerEvent(se);
         }
         
         val writeTook = System.currentTimeMillis() - startOfWrite
         assert(writeTook < 500, "Write took too long")
         
         // Wait a bit, but less than max delay - must not have flushed
         // TODO Occasionally, this fails due to a race condition.
         Thread.sleep(200)          
         eventReader.read(e => e.sessionId == ssn.getId).size mustBe 0
         
         // Read after delay - must be flushed
         Thread.sleep(2000)
         eventReader.read(e => e.sessionId == ssn.getId).size mustBe eventWriter.fullSize
      }

      "flush before EVENT_WRITER_MAX_DELAY if EVENT_WRITER_PERCENT_FULL" in {
         
         val sid = newSid
         eventReader.read(e => e.sessionId == sid).size mustBe 0 
         val body = sessionJson.expand("sid" -> sid)
         assertResp(route(app,httpReq(PUT, "/session/monstrosity").withBody(body)))
            .isOk
            .withNoBody

         val ssn = server.ssnStore.get(sid).get
         
         val startOfWrite = System.currentTimeMillis()

         for (i <- 1 to eventWriter.fullSize + 1) { 
            val (name, value, timestamp) = (Random.nextString(5), Random.nextString(5), Random.nextLong())
            val se = TraceEventImpl.mkTraceEvent(name);
            ssn.asInstanceOf[SessionImpl].triggerEvent(se);
         }
         
         val writeTook = System.currentTimeMillis() - startOfWrite
         assert(writeTook < 500, "Write took too long")
         
         // Wait a bit, but less than max delay - must be flushed
         Thread.sleep(eventWriter.maxDelayMillis - 1000)          
         eventReader.read(e => e.sessionId == ssn.getId).size mustBe (eventWriter.fullSize + 1)
      }
   }
}