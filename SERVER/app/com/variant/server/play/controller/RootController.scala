package com.variant.server.play.controller

import javax.inject.Inject
import org.apache.commons.lang3.time.DurationFormatUtils
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.conn.SessionStore
import play.api.Logger
import com.variant.core.ServerError._
import com.variant.server.boot.VariantServer
import com.variant.server.schema.ServerSchema
import com.variant.server.conn.Connection
import com.variant.server.conn.ConnectionStore
import play.api.libs.json._
import play.api.mvc.Result
import com.variant.server.boot.ServerErrorRemote
import com.variant.server.api.ServerException
import com.variant.core.schema.State
import com.variant.core.session.CoreStateRequest
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import com.variant.server.play.action.DisconnectedAction

//@Singleton -- Is this for non-shared state controllers?
class RootController @Inject() (
      override val connStore: ConnectionStore, 
      override val ssnStore: SessionStore,
      val disconnectedAction: DisconnectedAction,
      val cc: ControllerComponents
      ) extends VariantController(connStore, ssnStore, cc)  {
      
  /**
   *  If a path ends in a slash, redirect to the same path without the trailing /.
   */
   def untrail(path:String) = disconnectedAction { MovedPermanently("/" + path) }

   /**
    * Print server status message
    */
   def status() = disconnectedAction { req =>
      Ok(server.productName + ", Uptime %s.".format(DurationFormatUtils.formatDuration(System.currentTimeMillis() - server.startTs, "HH:mm:ss")))
   }

}
