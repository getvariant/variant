package com.variant.server.routs

import java.time.Duration

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import com.variant.core.util.TimeUtils
import com.variant.server.boot.VariantServer

/**
 * "/" route
 */
object Root {

   /**
    * "/" route.
    * Display health page.
    */
   def root = {

      val msg: StringBuilder = new StringBuilder(VariantServer.productName) ++= ".\n" ++=
         "Uptime: %s.\n".format(TimeUtils.formatDuration(VariantServer.uptime))

      val liveGens = VariantServer.schemata.getLiveGens

      if (liveGens.size == 0) {
         msg.append("No schemata deployed")
      } else {
         msg.append("Schemata:")
         liveGens.foreach(liveGen =>
            msg.append(
               "\n   Name: " + liveGen.getMeta.getName +
                  "\n      Comment: " + liveGen.getMeta.getComment +
                  "\n      States: " + liveGen.getStates.size +
                  "\n      Variations: " + liveGen.getVariations.size))
      }
      msg.append('\n')

      complete((StatusCodes.OK, msg.toString))
   }
}