package com.variant.server.test.controller
/*
import java.util.Optional

import scala.collection.JavaConverters._

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

import com.variant.server.api.StateRequest.Status._
import com.variant.core.error.ServerError._
import com.variant.core.session.CoreSession
import com.variant.core.session.CoreStateRequest
import com.variant.server.impl.SessionImpl
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.util.EventExperienceFromDatabase
import com.variant.server.test.util.TraceEventReader

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.test.Helpers.GET
import play.api.test.Helpers.POST
import play.api.test.Helpers.PUT
import play.api.test.Helpers.route
import com.variant.core.Constants
import com.variant.server.api.TraceEvent
import com.variant.server.impl.TraceEventImpl
*/
/**
 * Petclinic demo app test.
 * TODO: FIX THIS. The problem is that petclinic now uses a non jdbc writer.  See #244
 *
 * class PetclinicTest extends EmbeddedServerSpec {
 *
 *
 * "Schema petclinic" should {
 *
 * val schema = server.schemata.get("petclinic").get.liveGen.get
 * val schemaId = schema.id
 * val writer = schema.eventWriter
 * val reader = TraceEventReader(writer)
 * var sid = newSid
 *
 * "create new session" in {
 *
 * // ssn.setAttribute("user-agent", "Safari")
 *
 * assertResp(route(app, httpReq(POST, "/session/petclinic/" + sid).withBody(emptyTargetingTrackerBody)))
 * .isOk
 * .withBodySession { ssn =>
 * ssn.getId mustNot be (sid)
 * sid = ssn.getId
 * ssn.getSchema.getMeta.getName mustBe "petclinic"
 * }
 * }
 *
 * "set an session attribute" in {
 *
 * val body: JsValue = Json.obj(
 * "sid" -> sid,
 * "map" -> Map(
 * "disqual" -> "true",  // this will cause disqualification
 * "foo" -> "bar"
 * )
 * )
 * assertResp(route(app, httpReq(PUT, "/session/attr").withBody(body.toString())))
 * .isOk
 * }
 *
 * "disqualify session from test" in {
 *
 * assertResp(route(app, httpReq(POST, "/session/petclinic/" + sid).withBody(emptyTargetingTrackerBody)))
 * .isOk
 * .withBodyJson { json =>
 * val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
 * coreSsn.getStateRequest mustBe Optional.empty
 * }
 *
 * // State request object.
 * val reqBody1 = Json.obj(
 * "sid" -> sid,
 * "state" -> "newVisit"
 * ).toString
 *
 * // Target and get the request.
 * assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
 * .isOk
 * .withBodyJson { json =>
 * val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
 * val stateReq = coreSsn.getStateRequest.get
 * stateReq mustNot be (null)
 * stateReq.getStatus mustBe InProgress
 * stateReq.getLiveExperiences.size mustBe 0
 * coreSsn.getDisqualifiedVariations.size mustBe 1
 * stateReq.getResolvedParameters.size mustBe 1
 * // Resolved parameter must always be from the state def because we're disqualified
 * stateReq.getResolvedParameters.get("path") mustBe schema.getState("newVisit").get.getParameters.get("path")
 * stateReq.getSession.getId mustBe sid
 * stateReq.getState mustBe schema.getState("newVisit").get
 * }
 *
 * val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]
 * serverSsn.triggerEvent(TraceEventImpl.mkTraceEvent("Custom Event", Map("foo"->"bar").asJava))
 *
 * // Commit the request.
 * val reqBody2 = Json.obj(
 * "sid" -> sid,
 * "status" -> Committed.ordinal
 * ).toString
 *
 * assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
 * .isOk
 * .withBodyJson { json =>
 * val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
 * val stateReq = coreSsn.getStateRequest.get
 * stateReq mustNot be (null)
 * stateReq.getStatus mustBe Committed
 * stateReq.getLiveExperiences.size mustBe 0
 * coreSsn.getDisqualifiedVariations.size mustBe 1
 * // Resolved parameter must always be from the state def because we're disqualified
 * stateReq.getResolvedParameters.size mustBe 1
 * stateReq.getResolvedParameters.get("path") mustBe schema.getState("newVisit").get.getParameters.get("path")
 * stateReq.getSession.getId mustBe sid
 * stateReq.getState mustBe schema.getState("newVisit").get
 * }
 *
 * // Send custom event.
 * val eventBody = TraceEventTest.body.expand("sid" -> sid, "name" -> "eventName")
 * //status(resp)(akka.util.Timeout(5 minutes)) mustBe OK
 * assertResp(route(app, httpReq(POST, "/event").withTextBody(eventBody)))
 * .isOk
 * .withNoBody
 *
 * // Confirm that the SVE and the custom events are both orphans.
 * Thread.sleep(2000)
 * val flushedEvents = reader.read(e => e.sessionId == sid)
 * flushedEvents.size mustBe 2
 * flushedEvents.exists { _.eventExperiences.size > 0 } mustBe false
 * }
 * }
 * }
 *
 */
