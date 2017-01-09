package com.variant.server.controller

import play.api.mvc.Controller
import com.variant.server.ServerException
import com.variant.core.exception.ServerError

abstract class VariantController extends Controller {
  
   /**
    * SCID is Session id, followed by Conn ID, separated by :
    */
   protected def parseScid(sid:String) : (String,String) = {
      val tokens = sid.split("\\.")
      if (tokens.length != 2) throw new ServerException.Remote(ServerError.InvalidSCID, sid)
      (tokens(0),tokens(1))
   }

}