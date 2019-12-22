package com.variant.server.routes

import scala.collection.mutable
import com.variant.server.boot.VariantServer
import com.variant.share.util.TimeUtils
import com.variant.server.build.BuildInfo

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes

import play.api.libs.json._

/**
 * "/" route
 */
object RootRoute extends VariantRoute {

   /**
    * "/" route.
    * Display health page.
    */
   def root(implicit server: VariantServer) = action {

      val response = JsObject(Seq(
         "name" -> JsString(VariantServer.name),
         "version" -> JsString(VariantServer.version),
         "uptimeSeconds" -> JsNumber(server.uptime.toSeconds),
         "build" -> JsObject(Seq(
               "timestamp" -> JsString(BuildInfo.buildTimestamp),
               "scalaVersion" -> JsString(BuildInfo.scalaVersion),
               "javaVersion" -> JsString(BuildInfo.javaVersion),
               "javaVmName" -> JsString(BuildInfo.javaVmName),
               "javaVmVersion" -> JsString(BuildInfo.javaVmVersion))),
         "schemata" -> JsArray(
               server.schemata.getLiveGens
                  .sortBy(_.getMeta.getName)
                  .map { schema =>
                     val jsonList = mutable.ListBuffer("name" -> JsString(schema.getMeta.getName))
                     schema.getMeta.getComment.ifPresent { comment =>
                        jsonList +=  ("comment" -> JsString(comment))
                     }
                     JsObject(jsonList)
               }
          )
      ))
             
      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, Json.prettyPrint(response) + '\n'))
   }
}