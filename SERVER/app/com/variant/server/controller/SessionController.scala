package com.variant.server.controller

import scala.collection.JavaConversions._
import com.variant.core.ServerError.EmptyBody
import com.variant.core.session.CoreSession
import com.variant.core.util.Constants._
import com.variant.core.ConnectionStatus._
import com.variant.server.api.ServerException
import com.variant.server.boot.VariantServer
import com.variant.server.conn.ConnectionStore
import com.variant.server.conn.SessionStore
import com.variant.server.impl.SessionImpl

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

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

      val conn = connStore.getOrBust(getConnIdOrBust(req))        

      try {
         val ssnJson = req.body.asText.getOrElse {
            throw new ServerException.Remote(EmptyBody)
         }
         
         ssnStore.put(SessionImpl(CoreSession.fromJson(ssnJson, conn.schema), conn))
      }
      // If we're going out with an exception, don't forget to attach the connection status header.
      catch {
         case rex: ServerException.Remote => 
            throw rex.withHeaders(Map(HTTP_HEADER_CONN_STATUS -> conn.status.toString))
      }
            
      Ok.withHeaders(HTTP_HEADER_CONN_STATUS -> conn.status.toString())         
   }
 
   /**
    * GET 
    * Get a session by ID, if exists and was open in the current
    * or parallel connection.
    */
   def get(sid: String) = VariantAction { req =>

      val cid = getConnIdOrBust(req)
      val ssn = ssnStore.getOrBust(sid, cid)
      
      val response = JsObject(Seq(
         "session" -> JsString(ssn.toJson)
      ))
      Ok(response.toString)
   }
 
}
