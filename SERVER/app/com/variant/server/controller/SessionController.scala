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

//@Singleton -- Is this for non-shared state controllers?
class SessionController @Inject() (
      override val connStore: ConnectionStore, 
      override val ssnStore: SessionStore, 
      server: VariantServer) extends VariantController  {
   
      private val logger = Logger(this.getClass)	
       
   /**
    * PUT new session into the session store.
    * @param cid Connection ID
    */
   def save() = VariantAction { req =>

      val bodyJson = req.body.asJson.getOrElse {
         throw new ServerException.Remote(EmptyBody)
      }
      
      val cid = (bodyJson \ "cid").asOpt[String]
      val ssnJson = (bodyJson \ "ssn").asOpt[String]
         
      if (cid.isEmpty)
         throw new ServerException.Remote(MissingProperty, "cid")
   
      if (ssnJson.isEmpty) 
         throw new ServerException.Remote(MissingProperty, "ssn")

      // Lookup connection
      val conn = connStore.getOrBust(cid.get)

      logger.debug(s"Found connection [$cid]")      

      val coreSession = CoreSession.fromJson(ssnJson.get, server.schema.get);
      ssnStore.put(coreSession, conn)
      Ok
   }
 
   /**
    * GET a session by ID.
    * test with:
curl -v -X GET http://localhost:9000/variant/session/SID
    */
   def get(sid: String) = VariantAction {
      
      val ssn = ssnStore.getOrBust(sid)
      
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
