package com.variant.server.controller

import play.api.Logger
import play.api.mvc.Controller
import play.api.libs.json._
import com.variant.server.api.ServerException
import com.variant.core.ServerError._
import com.variant.server.conn.ConnectionStore
import com.variant.server.conn.Connection
import com.variant.server.impl.SessionImpl


abstract class VariantController extends Controller {

   val connStore: ConnectionStore
   
   private val logger = Logger(this.getClass)	

   /**
    * Parse SCID
    * SCID is Session id, followed by Conn ID, separated by :
    */
   protected def parseScid(sid:String) : (String,String) = {
      val tokens = sid.split("\\.")
      if (tokens.length != 2) throw new ServerException.Remote(InvalidSCID, sid)
      (tokens(0),tokens(1))
   }
   
   /**
    * Lookup connection by SCID. Return user error if none.
    */
   protected def lookupConnection(scid: String): Connection = {

      val (sid, cid) = parseScid(scid)

      // Lookup connection
      val result = connStore.get(cid)      
      if (!result.isDefined) {
         logger.debug(s"Not found connection [$cid]")      
         throw new ServerException.Remote(UnknownConnection, cid)
      }

      logger.debug(s"Found connection [$cid]")      
      result.get
   }

   /**
    * Lookup session by SCID
    */
   protected def lookupSession(scid: String): Option[(Connection, SessionImpl)] = {

      val conn = lookupConnection(scid)      
      val (sid, cid) = parseScid(scid)

      val result = conn.getSession(sid)
      if (result.isDefined) {
         logger.debug(s"Found session [$sid]")
         Some(conn, result.get.asInstanceOf[SessionImpl])
      }
      else {
         logger.debug(s"Not found session [$sid]")
         None
      }
   }
   
   /**
    * Most calls will have the same body structure.
    */
   protected def parseBody(body: String): (Connection, SessionImpl) = {

      val json = {
         try {
            Json.parse(body)
         }
         catch {
            case t: Throwable => throw new ServerException.Remote(JsonParseError, t.getMessage);
         }
      }
      val cid = (json \ "cid").asOpt[String]
      val ssnJson = (json \ "ssn").asOpt[String]
         
      if (cid.isEmpty)
         throw new ServerException.Remote(MissingProperty, "cid")
   
      if (ssnJson.isEmpty) 
         throw new ServerException.Remote(MissingProperty, "ssn")

      // Lookup connection
      val conn = connStore.get(cid.get)      
      if (!conn.isDefined) {
         logger.debug(s"Not found connection [${cid.get}]")      
            throw new ServerException.Remote(UnknownConnection, cid.get)
      }

      logger.debug(s"Found connection [$cid]")      

      (conn.get, SessionImpl(ssnJson.get))
   }

}