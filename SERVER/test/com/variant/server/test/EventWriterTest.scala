package com.variant.server.test

import scala.collection.JavaConversions._
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import com.variant.server.event.ServerEvent
import java.util.Date
import com.variant.server.test.util.EventReader
import com.variant.server.test.controller.SessionTest
import com.variant.server.conn.Connection
import com.variant.server.test.util.ParameterizedString
import play.api.libs.json.Json
import scala.util.Random
import com.variant.server.impl.SessionImpl
		
class EventWriterTest extends BaseSpecWithServer {

   val schemaId = server.schema.get.getId
   val eventWriter = server.schema.get.eventWriter
   val eventReader = EventReader(eventWriter)

   val sessionJson = ParameterizedString("""
      {"sid":"${sid:SID}",
       "ts": ${ts:%d}, 
       "schid": "%s",
       "request": {"state": "state1","status": "OK","committed": true, 
                  "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "tests": ["test1","test2"]
      }
   """.format(System.currentTimeMillis(), schemaId))
   
   "Event writer" should {

      "have expected confuration" in {
         eventWriter.maxBufferSize mustEqual 200
         eventWriter.fullSize mustEqual 100
	       eventWriter.maxDelayMillis mustEqual 2000

      }
    
      var connId: String = null
      
      "obtain a connection" in {
         // POST new connection
         val connResp = route(app, FakeRequest(POST, context + "/connection/big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         connId = (json \ "id").as[String]
         connId mustNot be (null)
      }
      
      "flush an event after EVENT_WRITER_FLUSH_MAX_DELAY_MILLIS" in {
         
         // PUT session.
         val sid = newSid
         eventReader.read(e => e.getSessionId == sid).size mustBe 0 
         val ssnBody = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> sid)
            )
         val ssnResp = route(app, FakeRequest(PUT, context + "/session").withJsonBody(ssnBody)).get
         status(ssnResp) mustBe OK
         contentAsString(ssnResp) mustBe empty
         
         val ssn = ssnStore.get(sid).get
         
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
         val ssnBody = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> sid)
            )
         val ssnResp = route(app, FakeRequest(PUT, context + "/session").withJsonBody(ssnBody)).get
         status(ssnResp) mustBe OK
         contentAsString(ssnResp) mustBe empty

         val ssn = ssnStore.get(sid).get

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
         val ssnBody = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> sid)
            )
         val ssnResp = route(app, FakeRequest(PUT, context + "/session").withJsonBody(ssnBody)).get
         status(ssnResp) mustBe OK
         contentAsString(ssnResp) mustBe empty

         val ssn = ssnStore.get(sid).get
         
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