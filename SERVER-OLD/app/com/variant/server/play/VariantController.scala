package com.variant.server.play

import com.fasterxml.jackson.core.JsonParseException
import com.variant.core.error.ServerError
import com.variant.core.Constants._
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
import com.variant.server.boot.ServerExceptionRemote

/**
 * All Variant controllers inherit from this.
 */
abstract class VariantController @Inject() (
      cc: ControllerComponents,
      server: VariantServer
      ) extends AbstractController(cc) {
   
   private val logger = Logger(this.getClass)	

   /**
    * An alias for the server
    */
   //protected val server = VariantServer.instance
     
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
                     throw new ServerExceptionRemote(ServerError.JsonParseError, t.getMessage.replaceAll("\\s+"," "))
               }
               
            }
         case None => None
      }       
   }
   
}