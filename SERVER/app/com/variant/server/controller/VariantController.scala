package com.variant.server.controller

import play.api.Logger
import play.api.mvc.Controller
import com.variant.server.ServerException
import com.variant.core.exception.ServerError._
import com.variant.server.conn.ConnectionStore
import com.variant.server.session.ServerSession
import com.variant.server.conn.Connection


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
   protected def lookupSession(scid: String): Option[ServerSession] = {

      val conn = lookupConnection(scid)      
      val (sid, cid) = parseScid(scid)

      val result = conn.getSession(sid)
      if (result.isDefined) logger.debug(s"Found session [$sid]")
      else logger.debug(s"Not found session [$sid]")
      
      result
   }
   
}