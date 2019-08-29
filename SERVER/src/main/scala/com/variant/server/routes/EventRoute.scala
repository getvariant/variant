package com.variant.server.routes

import scala.collection.JavaConverters.mapAsJavaMapConverter

import com.typesafe.scalalogging.LazyLogging
import com.variant.core.error.ServerError
import com.variant.server.boot.ServerExceptionRemote
import com.variant.server.boot.VariantServer
import com.variant.server.impl.SessionImpl
import com.variant.server.impl.TraceEventImpl

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.RequestContext

object EventRoute extends VariantRoute with LazyLogging {

   /**
    * POST
    * Trigger a custom event.
    */
   def triggerEvent(schemaName: String, sid: String)(implicit server: VariantServer, ctx: RequestContext): HttpResponse = action { body =>

      val ssn = getSession(schemaName, sid) getOrElse {
         throw ServerExceptionRemote(ServerError.SESSION_EXPIRED, sid)
      }

      val name = (body \ "name").asOpt[String].getOrElse {
         throw new ServerExceptionRemote(ServerError.MissingProperty, "name")
      }

      val attrs = (body \ "attrs").asOpt[Map[String, String]].getOrElse {
         Map[String, String]()
      }

      // Trigger the event.
      ssn.asInstanceOf[SessionImpl].triggerEvent(new TraceEventImpl(name, attrs.asJava))

      stdSessionResponse(ssn)

   }
}

