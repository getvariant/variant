package com.variant.server.play.controller

import com.variant.core.ServerError.EmptyBody
import com.variant.core.session.CoreSession
import com.variant.server.api.ServerException
import com.variant.server.conn.ConnectionStore
import com.variant.server.conn.SessionStore
import com.variant.server.impl.SessionImpl
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.ControllerComponents
import com.variant.server.play.action.ConnectedAction
import com.variant.core.ServerError
import com.variant.core.ConnectionStatus._

class SessionController @Inject() (
      val connectedAction: ConnectedAction,
      val cc: ControllerComponents
      ) extends VariantController(cc)  {
   
   private val logger = Logger(this.getClass)	

   /**
    * PUT
    *
    * Save or replace a new session.
    *
    * If connection is OPEN: 
    *   If the session exists:
    *     The current connection ID must match or be parallel 
    *     to the session's last modifying connection. 
    *   If the session does not exist:
    *     The new session will be created in the supplied connection. 
    *
    * If connection is CLOSED_BY_SERVER, throw UnknownConnection error.
    *   
    */
   def save() = connectedAction { req =>

      val ssnJson = req.body.asText.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }

      val conn = req.attrs.get(connectedAction.ConnKey).get
      
      if (conn.status == CLOSED_BY_SERVER)  // ConnectedAction enforces that it's
                                            // either OPEN or CLOSED_BY_SERVER
         throw new ServerException.Remote(ServerError.UnknownConnection, conn.id)

      server.ssnStore.put(SessionImpl(CoreSession.fromJson(ssnJson, conn.schema), conn))
            
      Ok      
   }
 
   /**
    * GET 
    * Get a session by ID, if exists and was open in the current or parallel connection.
    */
   def get(sid: String) = connectedAction { req =>

      val conn = req.attrs.get(connectedAction.ConnKey).get
      val ssn = server.ssnStore.getOrBust(sid, conn.id)
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)
      ))
      
      Ok(response.toString)
   }
 
}
