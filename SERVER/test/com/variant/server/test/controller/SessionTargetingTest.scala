package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConverters._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.core.error.ServerError._
import com.variant.core.util.StringUtils
import com.variant.core.StateRequestStatus._
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.Constants._
import com.variant.core.schema.impl.SchemaImpl
import com.variant.server.schema.ServerSchemaParser
import com.variant.core.session.CoreSession
import com.variant.core.session.CoreStateRequest

class SessionTargetingTest extends EmbeddedServerSpec {

	val emptyTargetingTrackerBody = "{\"tt\":[]}"
   val schema = server.schemata.get("monstrosity0").get.liveGen.get

   "RequestController" should {

   	var sid = newSid

      "create new session" in {
         
         // Create a new session in the monstrosity0 schema, i.e. no phantom states.
   		// This allows for a deterministic test as we hop from state to state, not needing
   		// to worry about whether a state may be phantom in one of the already live experiences.
         assertResp(route(app, httpReq(POST, "/session/monstrosity0/" + sid).withBody(emptyTargetingTrackerBody)))
            .isOk         
            .withBodySession { ssn =>
            	// Server responds with a new sid, if requested one wasn't found.
               ssn.getId mustNot be (sid)
               sid = ssn.getId  
               ssn.getSchema.getMeta.getName mustBe "monstrosity0"
         }
      }

      "create and commit state request for state1" in {

      	val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state1"
            ).toString
         
         // Target and get the request.
         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state1").get
               //println("*** 1 " + stateReq.getLiveExperiences)
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().get.getStatus mustBe InProgress

         // Commit
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString
           
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Committed
               stateReq.getLiveExperiences.size mustBe 4
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state1").get
         }

         serverSsn.getStateRequest().get.getStatus mustBe Committed
         //println("*** " + serverSsn.getTargetingStabile().getAll.asScala)
      }

      "create and commit state request for state2" in {

      	val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state2"
            ).toString
         
         // Target and get the request.
         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2").get
               //println("*** 2 " + stateReq.getLiveExperiences)
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().get.getStatus mustBe InProgress

         // Commit
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Committed.ordinal
            ).toString
           
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Committed
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state2").get
         }
         
         serverSsn.getStateRequest().get.getStatus mustBe Committed
         //println("*** " + serverSsn.coreSession.getTargetingStabile.getAll.asScala)

      }

      "create and fail state request for state3" in {

      	val reqBody1 = Json.obj(
            "sid" -> sid,
            "state" -> "state3"
            ).toString
         
         // Target and get the request.
         assertResp(route(app, httpReq(POST, "/request").withTextBody(reqBody1)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               val stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe InProgress
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3").get
               //println("*** 3 " + stateReq.getLiveExperiences)
            }

         val serverSsn = server.ssnStore.get(sid).get.asInstanceOf[SessionImpl]

         serverSsn.getStateRequest().get.getStatus mustBe InProgress

         // Fail
         val reqBody2 = Json.obj(
            "sid" -> sid,
            "status" -> Failed.ordinal
            ).toString
           
         var stateReq: CoreStateRequest = null
         assertResp(route(app, httpReq(PUT, "/request").withTextBody(reqBody2)))
            .isOk
            .withBodyJson { json => 
               val coreSsn = CoreSession.fromJson((json \ "session").as[String], schema)
               stateReq = coreSsn.getStateRequest.get
               stateReq mustNot be (null)
               stateReq.getStatus mustBe Failed
               stateReq.getLiveExperiences.size mustBe 5
               stateReq.getResolvedParameters.size mustBe 1
               stateReq.getSession.getId mustBe sid
               stateReq.getState mustBe schema.getState("state3").get
         }
      }
   }
}
