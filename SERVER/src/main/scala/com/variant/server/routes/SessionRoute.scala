package com.variant.server.routes

import scala.collection.JavaConverters._
import com.variant.share.error.ServerError
import com.variant.share.session.SessionScopedTargetingStabile
import com.variant.share.util.StringUtils
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
import java.util.Random
import scala.util.Try

object SessionRoute extends VariantRoute with LazyLogging {

   val rand = new Random

   /**
    * Get a session by ID, if exists in any of the given schema's generations.
    */
   def get(schemaName: String, sid: String)(implicit server: VariantServer): HttpResponse = action {

      val ssn = getSession(schemaName, sid) getOrElse {
         throw ServerExceptionRemote(ServerError.SESSION_EXPIRED, sid)
      }

      stdSessionResponse(ssn)
   }

   /**
    * Get a session by ID, if exists in any of the given schema's generations
    * or create in the live gen. This request should always come with the
    * targeting tracker content, but we only use it if we're creating the session.
    */
   def getOrCreate(schemaName: String, sid: String)(implicit server: VariantServer, ctx: RequestContext): HttpResponse = action { body =>

      val ssn: SessionImpl = getSession(schemaName, sid).getOrElse {

         val liveGen = server.schemata.getLiveGen(schemaName).getOrElse {
            throw new ServerExceptionRemote(ServerError.UNKNOWN_SCHEMA, schemaName)
         }

         val trackedExperiences = (body \ "tt").asOpt[List[String]].getOrElse {
            throw new ServerExceptionRemote(ServerError.MissingProperty, "tt")
         }

         val newSsn = SessionImpl.empty(StringUtils.random64BitString(rand), liveGen)
         val stabile = new SessionScopedTargetingStabile()
         trackedExperiences.foreach { s => stabile.add(SessionScopedTargetingStabile.Entry.parse(s)) }
         newSsn.setTargetingStabile(stabile)
         server.ssnStore.put(newSsn)

         newSsn
      }

      stdSessionResponse(ssn)
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
    *
    * def save(schemaName: String, sid: String, body: String)(implicit server: VariantServer): HttpResponse = {
    *
    * val sid = (Json.parse(body) \ "sid").asOpt[String].getOrElse {
    * throw new ServerExceptionRemote(ServerError.MissingProperty, "sid")
    * }
    *
    * val gen = server.ssnStore.get(sid) match {
    *
    * case Some(ssn) =>
    *
    * // Must match the schema name.
    * if (ssn.schemaGen.getMeta.getName != schemaName)
    * throw new ServerExceptionRemote(ServerError.WRONG_CONNECTION, schemaName)
    *
    * ssn.schemaGen
    *
    * case None =>
    * // Given schema must have a live gen.
    * server.schemata.getLiveGen(schemaName).getOrElse {
    * throw new ServerExceptionRemote(ServerError.UNKNOWN_SCHEMA, schemaName)
    * }
    * }
    *
    * val ssn = SessionImpl(body, gen)
    * server.ssnStore.put(ssn)
    *
    * HttpResponse(StatusCodes.OK)
    * }
    *
    */

   /**
    * Client is sending a map of attributes to be merged with the shared state.
    * Attributes sent will be added to the map, potentially replacing
    * existing values.
    */
   def putAttributes(schemaName: String, sid: String)(implicit server: VariantServer, ctx: RequestContext): HttpResponse = action { body =>

      val ssn = getSession(schemaName, sid).getOrElse {
         throw ServerExceptionRemote(ServerError.SESSION_EXPIRED, sid)
      }

      val attrMap = (body \ "attrs").asOpt[Map[String, String]].getOrElse {
         throw new ServerExceptionRemote(ServerError.MissingProperty, "attrs")
      }

      ssn.getAttributes.putAll(attrMap.asJava)

      stdSessionResponse(ssn)
   }

   /**
    * Client is sending a list of attribute names to be removed from this session.
    */
   def deleteAttributes(schemaName: String, sid: String)(implicit server: VariantServer, ctx: RequestContext): HttpResponse = action { body =>

      val attrs = (body \ "attrs").asOpt[Array[String]].getOrElse {
         throw new ServerExceptionRemote(ServerError.MissingProperty, "attrs")
      }

      val ssn = getSession(schemaName, sid).getOrElse {
         throw ServerExceptionRemote(ServerError.SESSION_EXPIRED, sid)
      }

      attrs.foreach { ssn.getAttributes.remove _ }

      stdSessionResponse(ssn)
   }

}
