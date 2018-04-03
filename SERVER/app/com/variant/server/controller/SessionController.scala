package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.conn.SessionStore
import play.api.Logger
import com.variant.core.ServerError._
import play.api.libs.json._
import com.variant.server.boot.VariantServer
import com.variant.server.boot.ServerErrorRemote
import com.variant.core.session.CoreSession
import com.variant.server.conn.ConnectionStore
import com.variant.server.api.ServerException
import com.variant.server.api.ConfigKeys
import com.variant.server.conn.Connection
import play.api.mvc.MultipartFormData.ParseError
import com.variant.server.impl.SessionImpl

//@Singleton -- Is this for non-shared state controllers?
class SessionController @Inject() (
      override val connStore: ConnectionStore, 
      override val ssnStore: SessionStore, 
      server: VariantServer) extends VariantController  {
   
      private val logger = Logger(this.getClass)	
       
   /**
    * PUT
    * Save or replace a new session in the store. 
    * If the session exists, the current connection ID must be
    * open and parallel to the original connection. 
    * Otherwise, the new session will be created in the supplied connection, 
    * so long as it's open. 
    */
   def save() = VariantAction { req =>

      val ssnJson = req.body.asText.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }
            
      val conn = connStore.getOrBust(getCIDOrBust(req))        
      ssnStore.put(SessionImpl(CoreSession.fromJson(ssnJson, conn.schema), conn))
      Ok
   }
 
   /**
    * GET 
    * Get a session by ID, if exists and was open in the current
    * or parallel connection.
    */
   def get(sid: String) = VariantAction { req =>

      val cid = getCIDOrBust(req)
      val ssn = ssnStore.getOrBust(sid, cid)
      
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)
      ))
      Ok(response.toString)
   }
 
   def post(id: String) = VariantAction {
      NotImplemented
   }
 
   def delete(id: String) = VariantAction {
      NotImplemented
   }
}
