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
               "timestamp" -> JsString(BuildInfo.builtAtString),
               "scalaVersion" -> JsString(BuildInfo.scalaVersion))),
         "schemata" -> JsArray(
               server.schemata.getLiveGens.map { schema =>
                  val jsonList = mutable.ListBuffer("name" -> JsString(schema.getMeta.getName))
                  schema.getMeta.getComment.ifPresent { comment =>
                     jsonList +=  ("comment" -> JsString(comment))
                  }
                  JsObject(jsonList)
               })
      ))
             
      /*
      val msg: StringBuilder =
         new StringBuilder(s"${VariantServer.productVersion._1} release ${VariantServer.productVersion._2}") ++=
            ".\n" ++=
            "Uptime: %s.\n".format(TimeUtils.formatDuration(server.uptime))

      val liveGens = server.schemata.getLiveGens

      if (liveGens.size == 0) {
         msg ++= "No schemata deployed."
      } else {
         msg ++= "Schemata:"
         liveGens.foreach(liveGen =>
            msg ++=
               "\n   Name: " + liveGen.getMeta.getName ++=
               "\n      Comment: " + liveGen.getMeta.getComment ++=
               "\n      States: " + liveGen.getStates.size ++=
               "\n      Variations: " + liveGen.getVariations.size ++=
               "\n      Scala: " + BuildInfo.scalaVersion)
      }

      msg ++= "\n"
		*/
      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, Json.prettyPrint(response)))
   }
}