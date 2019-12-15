package com.variant.server.routes

import java.time.Duration

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import com.variant.share.util.TimeUtils
import com.variant.server.boot.VariantServer
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.headers.Server

/**
 * "/" route
 */
object RootRoute extends VariantRoute {

   /**
    * "/" route.
    * Display health page.
    */
   def root(implicit server: VariantServer) = action {

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
               "\n      Variations: " + liveGen.getVariations.size)
      }

      msg ++= "\n"

      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, msg.toString))
   }
}