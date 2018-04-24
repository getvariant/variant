package com.variant.server.play.controller

import com.variant.core.ServerError.EmptyBody
import com.variant.core.session.CoreSession
import com.variant.core.ServerError._
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
import com.variant.server.boot.VariantServer

class SessionController @Inject() (
      val connectedAction: ConnectedAction,
      val cc: ControllerComponents,
      val server: VariantServer
      ) extends VariantController(cc, server)  {
   
   private val logger = Logger(this.getClass)	

   /**
    * Save or replace a new session.
    *
    * IF the session exists THEN
    *   IF the current connection ID matches or is parallel 
    *   to the session's original connection
    *   THEN Replace the session.
    *   ELSE SessionExpired
    * 
    * ELSE (the session does not exist)
    *   IF connection is OPEN, create new session.
    *   ELSE IF connection is CLOSED_BY_SERVER THEN UnknownConnection
    *   ELSE InternalError
    *   
    */
   def saveSession() = connectedAction { req =>

      val ssnJson = req.body.asText.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }

      val conn = req.attrs.get(connectedAction.ConnKey).get
      server.ssnStore.put(SessionImpl(CoreSession.fromJson(ssnJson, conn.schema), conn))
      Ok      
   }
 
   /**
    * Get a session by ID, if exists and was open in the current or parallel connection.
    */
   def getSession(sid: String) = connectedAction { req =>

      val conn = req.attrs.get(connectedAction.ConnKey).get
      val ssn = server.ssnStore.getOrBust(sid, conn)
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)
      ))
      
      Ok(response.toString)
   }
   
   /**
    * 
    */
   def addAttribute() = connectedAction { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }
      
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "sid")         
      }
      val name = (bodyJson \ "name").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "name")         
      }
      val value = (bodyJson \ "value").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "value")         
      }

      val conn = req.attrs.get(connectedAction.ConnKey).get
      val ssn = server.ssnStore.getOrBust(sid, conn)

      val prevValue = ssn.setAttribute(name, value)
      
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)
      ))
      
      if (prevValue != null) response + ("returns" -> JsString(prevValue))
      
      Ok(response.toString())
   }
 
}
