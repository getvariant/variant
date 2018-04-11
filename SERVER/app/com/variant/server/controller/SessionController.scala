package com.variant.server.controller

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

class SessionController @Inject() (
      override val connStore: ConnectionStore, 
      override val ssnStore: SessionStore,
      val connectedAction: ConnectedAction,
      val cc: ControllerComponents
      ) extends VariantController(connStore, ssnStore, cc)  {
   
   private val logger = Logger(this.getClass)	

   /**
    * PUT
    * Save or replace a new session in the store. 
    * If the session exists, the current connection ID must be
    * open and parallel to the original connection. 
    * Otherwise, the new session will be created in the supplied connection, 
    * so long as it's open. 
    */
   def save() = connectedAction { req =>

      val ssnJson = req.body.asText.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }

      val conn = req.attrs.get(connectedAction.ConnKey).get
      ssnStore.put(SessionImpl(CoreSession.fromJson(ssnJson, conn.schema), conn))
            
      Ok      
   }
 
   /**
    * GET 
    * Get a session by ID, if exists and was open in the current
    * or parallel connection.
    */
   def get(sid: String) = connectedAction { req =>

      val conn = req.attrs.get(connectedAction.ConnKey).get
      val ssn = ssnStore.getOrBust(sid, conn.id)
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)
      ))
      
      Ok(response.toString)
   }
 
}
