package com.variant.server.play.controller

import com.fasterxml.jackson.core.JsonParseException
import com.variant.core.ServerError
import com.variant.core.util.Constants
import com.variant.server.api.ServerException
import com.variant.server.boot.VariantServer

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import play.api.mvc.Request

/**
 * All Variant controllers inherit from this.
 */
abstract class VariantController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
   
   private val logger = Logger(this.getClass)	
   
   /**
    * An alias for the server
    */
   protected val server = VariantServer.instance
     
   /**
    * Parse body as JSON.
    */
   protected def getBody(req: Request[AnyContent]): Option[JsValue] = {
      
      req.body.asText match {
         case Some(body) => 
            if (body.length() == 0) {
               None
            }
            else {
               try {
                  Some(Json.parse(body))
               } catch {
                  case t: JsonParseException =>
                     // Lose new lines that Jackson inserts into messages.
                     throw new ServerException.Remote(ServerError.JsonParseError, t.getMessage.replaceAll("\\s+"," "))
               }
               
            }
         case None => None
      }      
      
   }
   
   /**
    * Get connection ID from header as an Option
    */
   protected def getConnId(req: Request[AnyContent]): Option[String] = {
      req.headers.get(Constants.HTTP_HEADER_CONNID)
   }

   /**
    * Get connection ID from HTTP header as String, or throw internal
    * remote exception if the header wasn't sent.
    *
   protected def getConnIdOrBust(req: Request[AnyContent]): String = {
      getConnId(req) match {
         case Some(cid) => cid
         case None => throw new ServerException.Remote(ServerError.ConnectionIdMissing)
      }
   }
*/   
}