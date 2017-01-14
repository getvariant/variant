package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.conn.SessionStore
import play.api.Logger
import com.variant.core.exception.ServerError._
import play.api.libs.json._
import com.variant.server.boot.VariantServer
import com.variant.server.ConfigKeys
import com.variant.server.boot.ServerErrorRemote
import com.variant.core.session.CoreSession
import com.variant.server.conn.ConnectionStore
import com.variant.server.session.ServerSession
import com.variant.server.ServerException
import com.variant.server.conn.Connection
import play.api.mvc.MultipartFormData.ParseError

//@Singleton -- Is this for non-shared state controllers?
class SessionController @Inject() (override val connStore: ConnectionStore, server: VariantServer) extends VariantController  {
   
      private val logger = Logger(this.getClass)	
       
   /**
    * PUT new session into the session store.
    * @param cid Connection ID
    */
   def save() = VariantAction { req =>

      req.contentType match {
         
         case Some(ct) if ct.equalsIgnoreCase("text/plain") => 
            val (conn, ssn) = parse(req.body.asText.get)
            conn.addSession(ssn)
            Ok
         
         case _ => ServerErrorRemote(BadContentType).asResult()
      }

   }
 
   /**
    * GET a session by ID.
    * test with:
curl -v -X GET http://localhost:9000/variant/session/SID
    */
   def get(scid: String) = VariantAction {
      
      val (cid, sid) = parseScid(scid)
      val result = lookupSession(scid)
      
      if (result.isDefined) {
         val (conn, ssn) = result.get
         logger.debug(s"Found session [$sid]")
         val response = JsObject(Seq(
            "session" -> JsString(ssn.toJson)
         ))
         Ok(response.toString)
      }
      else {
         logger.debug(s"Not found session [$sid]")         
         ServerErrorRemote(SessionExpired).asResult()
      }
   }
 
   def post(id: String) = VariantAction {
      NotImplemented
   }
 
   def delete(id: String) = VariantAction {
      NotImplemented
   }
}
