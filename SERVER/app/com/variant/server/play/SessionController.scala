package com.variant.server.play

import com.variant.core.impl.ServerError.EmptyBody
import com.variant.core.session.CoreSession
import com.variant.core.impl.ServerError._
import com.variant.server.api.ServerException
import com.variant.server.boot.SessionStore
import com.variant.server.impl.SessionImpl
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.ControllerComponents
import com.variant.core.impl.ServerError
import com.variant.server.boot.VariantServer

class SessionController @Inject() (
      val action: VariantAction,
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
    *   ELSE IF connection is DRAINING THEN UnknownConnection
    *   ELSE InternalError
    *   
    */
   def saveSession() = action { req =>

      val ssnJson = req.body.asText.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }

      server.ssnStore.put(SessionImpl(CoreSession.fromJson(ssnJson, conn.schema), conn))
      Ok      
   }
 
   /**
    * Get a session by ID, if exists in any of the given schema gens.
    */
   def getSession(schemaName:String, sid:String) = action { req =>
     
      val ssn = server.ssnStore.getOrBust(schemaName, sid)
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
      val value = (bodyJson \ "value").asOpt[String].getOrElse(null)

      val conn = req.attrs.get(connectedAction.ConnKey).get
      val ssn = server.ssnStore.getOrBust(sid, conn)

      val prevValue = ssn.setAttribute(name, value)
      
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson),
         "returns" -> JsString(prevValue)
      ))
            
      Ok(response.toString())
   }
 
   /**
    * Clear an attribute
    */
   def clearAttribute() = connectedAction { req =>

      val bodyJson = getBody(req).getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }
      val sid = (bodyJson \ "sid").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "sid")         
      }
      val name = (bodyJson \ "name").asOpt[String].getOrElse {
         throw new ServerException.Remote(MissingProperty, "name")         
      }

      val conn = req.attrs.get(connectedAction.ConnKey).get
      val ssn = server.ssnStore.getOrBust(sid, conn)
      val prevValue = ssn.clearAttribute(name)
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson),
         "returns" -> JsString(prevValue)
      ))
            
      Ok(response.toString)
   }

}
