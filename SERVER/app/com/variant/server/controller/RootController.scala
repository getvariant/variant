package com.variant.server.controller

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
import com.variant.server.session.ServerSession
import com.variant.core.session.CoreStateRequest
import play.api.mvc.AnyContent

//@Singleton -- Is this for non-shared state controllers?
class RootController @Inject() (override val connStore: ConnectionStore, server: VariantServer) extends VariantController  {
      
   def splash() = VariantAction { req =>
      Ok(server.productName + ", " +
         {
            if (server.isUp) "Uptime %s.".format(DurationFormatUtils.formatDuration(System.currentTimeMillis() - server.startTs, "HH:mm:ss")) 
            else "Down."
         }   
      )
   }

}
