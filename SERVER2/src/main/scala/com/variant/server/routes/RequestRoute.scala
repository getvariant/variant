package com.variant.server.routes

import scala.collection.JavaConverters.mapAsJavaMapConverter

import com.variant.core.Constants._
import com.variant.core.error.ServerError._
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.boot.ServerExceptionInternal
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.boot.VariantServer
import com.variant.server.api.TraceEvent
import com.variant.server.api.StateRequest
import com.variant.server.api.StateRequest.Status._
import com.variant.server.impl.SessionImpl
import com.variant.server.impl.StateRequestImpl
import com.variant.server.util.JavaImplicits.оptional2Option

import play.api.libs.json._
import com.variant.server.impl.TraceEventImpl
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.model.HttpResponse
/*
object RequestRoute extends VariantRoute with LazyLogging {

   /**
    * POST
    * Create state request by targeting a session for a state.
    */
   def create(schemaName: String, sid: String, body: String)(implicit server: VariantServer): HttpResponse = {

      val ssn = getSession(schemaName, sid) getOrElse {
         throw ServerExceptionRemote(ServerError.SESSION_EXPIRED, sid)
      }

      val bodyJson = Json.parse(body)


      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")
      }
      val stateName = (bodyJson \ "state").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "state")
      }

      val stabile = (bodyJson \ "stab").asOpt[List[String]]

      val ssn = server.ssnStore.getOrBust(sid)

      // It's an error to request another state, before committing/failing current request.
      //val req = Option(ssn.getStateRequest.orElse(null))
      ssn.getStateRequest.foreach { req =>
         if (req.getStatus == InProgress)
            throw new ServerExceptionRemote(ACTIVE_REQUEST)
      }

      val state = ssn.schemaGen.getState(stateName)
      if (!state.isDefined)
         throw new ServerExceptionInternal("State [%s] not in schema [%s]".format(stateName, ssn.schemaGen.getMeta.getName))

      // If stabile was sent, process it, discarding elements not in the schema,
      // and add to the session's stabile. This should have been the first state request in the life
      // of a session and here we were sent the content of the foreground targeting tracker.
      if (stabile.isDefined) {
         val ssts = new SessionScopedTargetingStabile()
         stabile.get.foreach { e =>
            val tokens = e.split("\\.");
            ssn.schemaGen.getVariation(tokens(0)).foreach { v =>
               v.getExperience(tokens(1)).foreach { ssts.add(_) }
            }
         }

         if (ssts.size > 0) ssn.setTargetingStabile(ssts)
      }

      ssn.schemaGen.runtime.targetForState(ssn, state.get)

      val response = JsObject(Seq(
         "session" -> JsString(ssn.asInstanceOf[SessionImpl].coreSession.toJson())
      ))

      Ok(response.toString)
   }

   /**
    * PUT
    * Commit or fail a state request. Trigger the implicit state visited event.
    */
   def commit() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerExceptionRemote(EmptyBody)
      }

      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")
      }

      val status = {
         val ordinal = (bodyJson \ "status").asOpt[Int].getOrElse {
            throw new ServerExceptionRemote(MissingProperty, "status")
         }
         StateRequest.Status.values()(ordinal)
      }

      if (!status.isIn(Committed, Failed))
         throw new ServerExceptionRemote(InvalidRequestStatus, status.toString())

      val attrs = (bodyJson \ "attrs").asOpt[Map[String,String]].getOrElse {
         Map[String,String]()
      }


      val ssn = server.ssnStore.getOrBust(sid)
      // Ok to assume we always have the request?
      val stateReq = ssn.getStateRequest.get.asInstanceOf[StateRequestImpl]

      if (stateReq.getStatus == Committed && status == Failed)
			throw new ServerExceptionRemote(CANNOT_FAIL);

		else if (stateReq.getStatus == Failed && status == Committed)
			throw new ServerExceptionRemote(CANNOT_COMMIT);

		else if (stateReq.getStatus == InProgress) {

         stateReq.asInstanceOf[StateRequestImpl].setStatus(status);

         // Trigger state visited event, but only if we have live experiences
         // at this state. As opposed to custom events, state visited events
         // cannot be orphan because we didn't really visit that state.
         if (!stateReq.getLiveExperiences().isEmpty()) {
         	val sve = new TraceEventImpl(SVE_NAME, attrs.asJava)
				sve.getAttributes.put("$STATUS", stateReq.getStatus.toString);
				sve.getAttributes.put("$STATE", stateReq.getState.getName);
      	   ssn.triggerEvent(sve);
         }

      }
      val response = JsObject(Seq(
         "session" -> JsString(ssn.coreSession.toJson)
      )).toString()

      Ok(response)
   }
}
*
*/
