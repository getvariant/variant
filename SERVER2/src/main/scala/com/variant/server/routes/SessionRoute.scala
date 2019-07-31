package com.variant.server.routes

import com.variant.core.error.ServerError
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.boot.VariantServer
import scala.io.Source
import com.typesafe.scalalogging.LazyLogging
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.model.HttpEntity
import play.api.libs.json._
import akka.http.scaladsl.server.Directives._
import com.variant.server.boot.ServerExceptionRemote
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ContentTypes
import com.variant.server.impl.SessionImpl
import com.variant.core.util.StringUtils
import java.util.Random

object SessionRoute extends VariantRoute with LazyLogging {

   val rand = new Random

   /**
    * Get a session by ID, if exists in any of the given schema's generations.
    */
   def get(schemaName: String, sid: String)(implicit server: VariantServer): HttpResponse = {

      // Schema must exist.
      val schema = server.schemata.get(schemaName).getOrElse {
         throw ServerExceptionRemote(ServerError.UNKNOWN_SCHEMA, schemaName)
      }

      val ssn = server.ssnStore.get(sid) match {

         case Some(ssn) =>
            // Must match the schema name.
            if (ssn.schemaGen.getMeta.getName != schemaName)
               throw new ServerExceptionRemote(ServerError.WRONG_CONNECTION, schemaName)
            ssn

         case None =>
            throw ServerExceptionRemote(ServerError.SESSION_EXPIRED, sid)
      }

      val entity = JsObject(Seq(
         "session" -> JsString(ssn.toJson),
         "schema" -> JsObject(Seq(
            "id" -> JsString(ssn.schemaGen.id),
            "src" -> JsString(ssn.schemaGen.source)))))

      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, entity.toString()))

   }

   /**
    * Get a session by ID, if exists in any of the given schema's generations
    * or create in the live gen. This request should always come with the
    * targeting tracker content, but we only use it if we're creating the session.
    */
   def getOrCreate(schemaName: String, sid: String, body: String)(implicit server: VariantServer): HttpResponse = {

      // Schema must exist.
      val schema = server.schemata.get(schemaName).getOrElse {
         throw ServerExceptionRemote(ServerError.UNKNOWN_SCHEMA, schemaName)
      }

      val trackedExperiences = (Json.parse(body) \ "tt").asOpt[List[String]].getOrElse {
         throw new ServerExceptionRemote(ServerError.MissingProperty, "tt")
      }

      val result: SessionImpl = server.ssnStore.get(sid) match {

         // Have the session. Must match the schema name.
         case Some(ssn) => {

            if (ssn.schemaGen.getMeta.getName != schema)
               throw new ServerExceptionRemote(ServerError.WRONG_CONNECTION, schemaName)

            ssn
         }

         // Don't have the session. Try to create, but change the SID,
         // so the client knows the session was recreated.
         case None => {

            val liveGen = server.schemata.getLiveGen(schemaName).getOrElse {
               throw new ServerExceptionRemote(ServerError.UNKNOWN_SCHEMA, schemaName)
            }

            val newSsn = SessionImpl.empty(StringUtils.random64BitString(rand), liveGen)
            val stabile = new SessionScopedTargetingStabile()
            trackedExperiences.foreach { s => stabile.add(SessionScopedTargetingStabile.Entry.parse(s)) }
            newSsn.setTargetingStabile(stabile)
            server.ssnStore.put(newSsn)

            newSsn
         }
      }

      // If we're here, we have the session
      val entity = JsObject(Seq(
         "session" -> JsString(result.toJson),
         "schema" -> JsObject(Seq(
            "id" -> JsString(result.schemaGen.id),
            "src" -> JsString(result.schemaGen.source)))))

      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, entity.toString()))
   }

   /**
    * Save or replace a new session.
    * It's not clear if real code uses it, but the test use it to create a particular session state.
    *
    * IF the session exists THEN
    *   IF its gen must matches the schema in request,
    *   THEN Replace the session.
    *   ELSE SessionExpired  // Really weird case !!!
    *
    * ELSE (the session does not exist)
    *   IF given schema exists and has a live gen, create
    *   ELSE throw UnknownSchema error.
    *
    */
   def save(schema: String, sid: String, body: String)(implicit server: VariantServer): HttpResponse = {

      val sid = (Json.parse(body) \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(ServerError.MissingProperty, "sid")
      }

      val gen = server.ssnStore.get(sid) match {

         case Some(ssn) =>

            // Must match the schema name.
            if (ssn.schemaGen.getMeta.getName != schema)
               throw new ServerExceptionRemote(ServerError.WRONG_CONNECTION, schema)

            ssn.schemaGen

         case None =>
            // Given schema must have a live gen.
            server.schemata.getLiveGen(schema).getOrElse {
               throw new ServerExceptionRemote(ServerError.UNKNOWN_SCHEMA, schema)
            }
      }

      val ssn = SessionImpl(body, gen)
      server.ssnStore.put(ssn)

      HttpResponse(StatusCodes.OK)
   }
   /*
   /**
    * Client is sending a map to be merged with the shared state.
    * Attributes sent will be added to the map, potentially replacing
    * existing values. Attributes in the shared state that were not
    * in the sent map remain in intact.
    */
   def sendAttributeMap() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerExceptionRemote(EmptyBody)
      }

      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")
      }
      val attrMap = (bodyJson \ "map").asOpt[Map[String, String]].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "map")
      }

      val ssn = server.ssnStore.getOrBust(sid)
      //ssn.getAttributes.clear()
      ssn.getAttributes.putAll(attrMap.asJava)

      // Serialize the session before adding the attribute.
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)))

      Ok(response.toString())
   }

   /**
    * Client is sending a list of attribute names to be removed from this session.
    */
   def deleteAttributes() = action { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerExceptionRemote(EmptyBody)
      }

      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "sid")
      }
      val attrs = (bodyJson \ "attrs").asOpt[Array[String]].getOrElse {
         throw new ServerExceptionRemote(MissingProperty, "attrs")
      }

      val ssn = server.ssnStore.getOrBust(sid)
      attrs.foreach { ssn.getAttributes.remove _ }

      // Serialize the session before adding the attribute.
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)))

      Ok(response.toString())
   }
   *
   */

}
