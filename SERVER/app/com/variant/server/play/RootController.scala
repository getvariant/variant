package com.variant.server.play

import javax.inject.Inject
import org.apache.commons.lang3.time.DurationFormatUtils
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.boot.SessionStore
import play.api.Logger
import com.variant.core.impl.ServerError._
import com.variant.server.boot.VariantServer
import play.api.libs.json._
import play.api.mvc.Result
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.api.ServerException
import com.variant.core.schema.State
import com.variant.core.session.CoreStateRequest
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import java.lang.management.ManagementFactory
import scala.collection.mutable.StringBuilder
import com.variant.core.util.TimeUtils

//@Singleton -- Is this for non-shared state controllers?
class RootController @Inject() (
      val action: VariantAction,
      val cc: ControllerComponents,
      val server: VariantServer
      ) extends VariantController(cc, server)  {
      
  /**
   *  If a path ends in a slash, redirect to the same path without the trailing /.
   */
   def untrail(path:String) = action { MovedPermanently("/" + path) }

   /**
    * Print server status message
    */
   def status() = action { req =>
     
      val msg: StringBuilder = new StringBuilder(VariantServer.productName) ++= ".\n" ++=
        "Uptime: %s.\n".format(TimeUtils.formatDuration(ManagementFactory.getRuntimeMXBean().getUptime()))
      
      val schemata = server.schemata.getLiveGens
      if (schemata.size == 0) {
         msg.append("No schemata deployed")
      }
      else {
        msg.append("Schemata:")
        server.schemata.getLiveGens.foreach(liveGen => 
           msg.append(
           "\n   Name: " + liveGen.getMeta.getName +
           "\n      Comment: " + liveGen.getMeta.getComment + 
           "\n      States: " + liveGen.getStates.size + 
           "\n      Variations: " + liveGen.getVariations.size))
      }
      msg.append('\n')
      
      Ok(msg.toString())
   }

}
