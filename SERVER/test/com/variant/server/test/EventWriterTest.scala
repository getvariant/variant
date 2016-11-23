package com.variant.server.test

import scala.util.Random
import scala.collection.JavaConversions._
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import com.variant.server.event.ServerEvent
import com.variant.server.ServerPropertiesKey._
import java.util.Date
import com.variant.server.test.util.EventReader
		
class EventWriterTest extends ServerBaseSpec {

   val writer = server.eventWriter
   val props = server.properties
   val reader = EventReader(writer)
   
   "Event writer" should {

      "have expected confuration" in {
         writer.bufferSize mustEqual 200
         writer.pctFullSize mustEqual 100
	      writer.maxDelayMillis mustEqual 2000

      }
    
      "flush an event after EVENT_WRITER_FLUSH_MAX_DELAY_MILLIS" in {
         
         val sid = Random.nextInt(100000).toString
         reader.read(e => e.getSessionId == sid).size mustBe 0 

         // PUT session.
         val ssnBody = SessionTest.body.expand("sid" -> sid)
         val ssnResp = route(app, FakeRequest(PUT, context + "/session/" + sid).withTextBody(ssnBody)).get
         status(ssnResp) mustBe OK
         contentAsString(ssnResp) mustBe empty

         val ssn = store.asSession(sid).get
         val (name, value, timestamp) = (Random.nextString(5), Random.nextString(5), Random.nextLong())
         val se = new ServerEvent(name, value, new Date(timestamp));
         ssn.triggerEvent(se);
         
         // Read events back from the db, but must wait for the asych flusher.
         Thread.sleep(server.eventWriter.maxDelayMillis * 2)
         val eventsFromDatabase = reader.read(e => e.getSessionId == sid)
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
         
         val sid = Random.nextInt(100000).toString
         reader.read(e => e.getSessionId == sid).size mustBe 0 

         // PUT session.
         val ssnBody = SessionTest.body.expand("sid" -> sid)
         val ssnResp = route(app, FakeRequest(PUT, context + "/session/" + sid).withTextBody(ssnBody)).get
         status(ssnResp) mustBe OK
         contentAsString(ssnResp) mustBe empty

         val ssn = store.asSession(sid).get

         // Ensure the writer buffer is empty.
         writer.flush()

         val startOfWrite = System.currentTimeMillis()
         
         for (i <- 1 to writer.pctFullSize) { 
            val (name, value, timestamp) = (Random.nextString(5), Random.nextString(5), Random.nextLong())
            val se = new ServerEvent(name, value, new Date(timestamp));
            ssn.triggerEvent(se);
         }
         
         val writeTook = System.currentTimeMillis() - startOfWrite
         assert(writeTook < 500, "Write took too long")
         
         // Wait a bit, but less than max delay - must not have flushed
         Thread.sleep(200)          
         reader.read(e => e.getSessionId == ssn.getId).size mustBe 0
         
         // Read after delay - must be flushed
         Thread.sleep(2000)
         reader.read(e => e.getSessionId == ssn.getId).size mustBe writer.pctFullSize
      }

      "flush before EVENT_WRITER_MAX_DELAY if EVENT_WRITER_PERCENT_FULL" in {
         
         val sid = Random.nextInt(100000).toString
         reader.read(e => e.getSessionId == sid).size mustBe 0 

         // PUT session.
         val ssnBody = SessionTest.body.expand("sid" -> sid)
         val ssnResp = route(app, FakeRequest(PUT, context + "/session/" + sid).withTextBody(ssnBody)).get
         status(ssnResp) mustBe OK
         contentAsString(ssnResp) mustBe empty

         val ssn = store.asSession(sid).get
         
         val startOfWrite = System.currentTimeMillis()

         for (i <- 1 to writer.pctFullSize + 1) { 
            val (name, value, timestamp) = (Random.nextString(5), Random.nextString(5), Random.nextLong())
            val se = new ServerEvent(name, value, new Date(timestamp));
            ssn.triggerEvent(se);
         }
         
         val writeTook = System.currentTimeMillis() - startOfWrite
         assert(writeTook < 500, "Write took too long")
         
         // Wait a bit, but less than max delay - must be flushed
         Thread.sleep(writer.maxDelayMillis - 1000)          
         reader.read(e => e.getSessionId == ssn.getId).size mustBe (writer.pctFullSize + 1)
      }

   }
}