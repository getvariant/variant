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
import akka.http.scaladsl.model.StatusCodes._
import com.variant.core.error.ServerError
import com.variant.server.test.spec.TraceEventsSpec
import com.variant.server.test.spec.Async
import com.variant.server.api.StateRequest
import com.variant.server.api.StateRequest.Status.Committed
import com.variant.server.api.StateRequest.Status.Failed
import com.variant.server.api.StateRequest.Status.InProgress
import org.scalatest.exceptions.TestFailedException
import com.variant.server.impl.ConfigKeys

class EventWriterTest extends EmbeddedServerSpec with TraceEventsSpec with Async {

   val emptyTargetingTrackerBody = "{\"tt\":[]}"

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
/*
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
         val customName = "custom name"
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

         // Ensure the writer buffer is empty.
         server.eventBufferCache.size mustBe 0

      }

      "not flush before EVENT_WRITER_MAX_DELAY if fewer than EVENT_WRITER_FLUSH_SIZE" in {

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
         server.eventBufferCache.size mustBe 0

         val startOfWrite = System.currentTimeMillis()

         for (i <- 1 until flushSize) {
            val name = "custom name " + i
            val se = TraceEventImpl.mkTraceEvent(name);
            ssn.asInstanceOf[SessionImpl].triggerEvent(se);
         }

         val writeTook = System.currentTimeMillis() - startOfWrite
         assert(writeTook < 500, "Write took too long")

         // Wait a bit, but less than max delay - must not have flushed
         // TODO Occasionally, this fails due to a race condition.
         Thread.sleep(maxDelayMillis / 10)
         eventReader.read(e => e.sessionId == ssn.getId).size mustBe 0

         // Read after delay - must be flushed
         Thread.sleep(maxDelayMillis * 2)
         eventReader.read(e => e.sessionId == ssn.getId).size mustBe (flushSize - 1)
      }

      "flush before EVENT_WRITER_MAX_DELAY if at least EVENT_WRITER_FLUSH_SIZE events" in {

         var sid = newSid

         HttpRequest(method = HttpMethods.POST, uri = s"/session/monstrosity/${sid}", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.session.getId mustNot be(sid)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
            sid = ssnResp.session.getId
         }

         // Ensure the writer buffer is empty.
         server.eventBufferCache.size mustBe 0

         eventReader.read(e => e.sessionId == sid).size mustBe 0

         val ssn = server.ssnStore.get(sid).get

         val startOfWrite = System.currentTimeMillis()

         for (i <- 1 to flushSize) {
            val (name, value, timestamp) = ("event " + i, Random.nextString(5), Random.nextLong())
            val se = TraceEventImpl.mkTraceEvent(name);
            ssn.asInstanceOf[SessionImpl].triggerEvent(se);
         }

         val writeTook = System.currentTimeMillis() - startOfWrite
         assert(writeTook < 50, "Write took too long")

         // Wait a bit, but less than max delay - must be flushed
         Thread.sleep(maxDelayMillis / 2)
         eventReader.read(e => e.sessionId == ssn.getId).size mustBe (flushSize)
      }
      */
      "create and flush a whole bunch of events without losing any" in {
         
         
         val sessions = 50
         val hops = 50
         val ssnIds = new Array[String](sessions)
         
         // We'll need a more potent event cache
         reboot { builder =>
            builder.withConfiguration(Map(ConfigKeys.EVENT_WRITER_BUFFER_SIZE -> 1000))
               .withConfiguration(Map(ConfigKeys.EVENT_WRITER_FLUSH_SIZE -> 100))
         }

         // Create the sessions
         for (i <- 0 until sessions) async {

            HttpRequest(method = HttpMethods.POST, uri = "/session/monstrosity/blah", entity = emptyTargetingTrackerBody) ~> router ~> check {
               val ssnResp = SessionResponse(response)
               ssnResp.schema.getMeta.getName mustBe "monstrosity"
               ssnIds(i) = ssnResp.session.getId
            }
         }

         joinAll()
         
         val specialKey = "specialKey"
         val specialVal = "This is how we'll be able to tell the events we're about to insert from the ones that are already there"
         
         var count = 0
         for (i <- 0 until sessions) async {
            
            for (j <- 0 until hops) {
               val nextState = "state" + (j % 5 + 1)
               if (targetForState(ssnIds(i), nextState)) {
                  commitStateRequest(ssnIds(i), (specialKey, specialVal))
                  count += 1
               }
               Thread.sleep(200 + Random.nextInt(600))
            }
         }
         
         joinAll(60000)
         println("*************** " + count)

         Thread.sleep(maxDelayMillis * 2)
         eventReader.read(_.attributes(specialKey) == specialVal).size mustBe count
     
      }
   }

   /**
    * Target or state may give 707 (Cannot target for phantom state) in which
    * case we return false. Any other error cases a test assertion.
    */
   private def targetForState(sid: String, name: String):Boolean = {
      
      val body = Json.obj("state" -> name).toString
      
      HttpRequest(method = HttpMethods.POST, uri = s"/request/monstrosity/${sid}", entity = body) ~> router ~> check {
         response.status match {
            case OK => true
              
            case BadRequest => 
               if (ServerErrorResponse(response).code != ServerError.STATE_PHANTOM_IN_EXPERIENCE.getCode) {
                  throw new TestFailedException("Unexpected User Error [" + ServerErrorResponse(response).toString() + "]", 1)
               }
               false
               
            case _ =>
               throw new TestFailedException(s"Unexpected HTTP Status ${response.status} with body [${response.entity}]", 2)
        }
      }      
   }
   
   private def commitStateRequest(sid: String, attr: (String,String)) {
      
      val body = Json.obj(
         "status" -> Committed.ordinal,
         "attrs" -> Map(attr._1 -> attr._2)).toString

      HttpRequest(method = HttpMethods.DELETE, uri = s"/request/monstrosity/${sid}", entity = body) ~> router ~> check {
         val ssnResp = SessionResponse(response)
         ssnResp.session.getId mustBe sid
         ssnResp.schema.getMeta.getName mustBe "monstrosity"
      }
   }

}