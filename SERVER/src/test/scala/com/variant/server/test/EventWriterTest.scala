package com.variant.server.test

import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.util.Random

import com.variant.server.impl.SessionImpl
import com.variant.server.impl.TraceEventImpl
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.util.TraceEventReader
import com.variant.server.test.util.ParameterizedString
import com.variant.server.test.routes.SessionTest

import play.api.libs.json._
import java.time.format.DateTimeFormatter
import java.time.Instant
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import com.variant.server.test.spec.TraceEventsSpec

class EventWriterTest extends EmbeddedServerSpec with TraceEventsSpec {

   "Event writer" should {

      val schema = server.schemata.get("monstrosity").get.liveGen.get
      val flusher = schema.flusherService.getFlusher
      val eventReader = TraceEventReader(flusher)

      val bufferCacheSize = server.config.eventWriterBufferSize
      val flushSize = server.config.eventWriterFlushSize
      val maxDelayMillis = server.config.eventWriterMaxDelay * 1000
      val flushParallelism = server.config.eventWriterFlushParallelism

      "have expected confuration" in {
         bufferCacheSize mustBe 200
         flushSize mustBe 10
         maxDelayMillis mustBe 2000
         flushParallelism mustBe 2
      }

      "flush an event after EVENT_WRITER_FLUSH_MAX_DELAY_MILLIS" in {

         // Create Session
         var sid = newSid
         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         eventReader.read(e => e.sessionId == sid).size mustBe 0

         // Target for state1
         val reqBody1 = Json.obj(
            "state" -> "state1").toString
         HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = reqBody1) ~> router ~> check {
            val ssnResp = SessionResponse(response)
         }

         // Trigger custom event.
         val customName = Random.nextString(5)
         val se = TraceEventImpl.mkTraceEvent(customName);

         val ssn = server.ssnStore.get(sid).get
         ssn.asInstanceOf[SessionImpl].triggerEvent(se);

         // Read events back from the db, but must wait for the async flusher.
         val millisWaited = maxDelayMillis * 2
         Thread.sleep(millisWaited)
         val eventsFromDatabase = eventReader.read(e => e.sessionId == sid)
         eventsFromDatabase.size mustBe 1
         val event = eventsFromDatabase.head
         event.createdOn.toEpochMilli mustBe (System.currentTimeMillis() - millisWaited) +- 100
         event.name mustBe customName
         event.sessionId mustBe sid
         event.eventExperiences.size mustBe 4
         event.eventExperiences.map(_.testName) mustBe Set("test2", "test3", "test5", "test6")

         Thread.sleep(5000)
      }
      /*
      "not flush before EVENT_WRITER_MAX_DELAY if fewer than EVENT_WRITER_PERCENT_FULL" in {

         var sid = newSid

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         eventReader.read(e => e.sessionId == sid).size mustBe 0

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

         var sid = newSid

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         eventReader.read(e => e.sessionId == sid).size mustBe 0

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
      *
      */
   }
}