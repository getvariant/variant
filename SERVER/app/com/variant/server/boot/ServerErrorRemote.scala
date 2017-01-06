package com.variant.server.boot

import org.apache.http.HttpStatus
import play.api.mvc.Result
import play.api.mvc.ResponseHeader
import play.api.http.HttpEntity
import play.api.libs.json._
import akka.util.ByteString
import com.variant.core.exception.ServerError

/**
 * Wrap the core ServerError type with some server side semantics
 */
object ServerErrorRemote {
   
   def apply(error: ServerError) = new ServerErrorRemote(error)
}

class ServerErrorRemote(error: ServerError) {
   
   import play.api.Logger
   
   val logger = Logger(this.getClass)
    
   def asResult(args:String*) = {
      
      if (error.code > 700) {
         val bodyJson : JsObject = Json.obj(
            "code" -> error.code,
            "message" -> error.asMessage(args:_*))
        
         if (error.comment != null) bodyJson + ("comment" -> JsString(error.comment))
         
         Result(
             header = ResponseHeader(HttpStatus.SC_BAD_REQUEST, Map.empty),
             body = HttpEntity.Strict(ByteString(bodyJson.toString()), Some("application/json"))
           )   
      }
      else {
         // Internal errors are logged in their entirety to the server log,
         // but are sent to client only by their error number.
         // For the server log, we want a call stack with the error.
         val msg = new StringBuilder("Internal API error [%s] [%s]".format(error.code, error.asMessage(args:_*)))
         // drop the first frame as it will be currentThread().
         Thread.currentThread().getStackTrace.drop(1).foreach(se => msg.append("\n  at ").append(se))
         logger.error(msg.toString())

         
         val bodyJson : JsObject = Json.obj(
            "code" -> ServerError.InternalError.code,
            "message" -> ServerError.InternalError.asMessage(error.asMessage(args:_*)))

         Result(
             header = ResponseHeader(HttpStatus.SC_INTERNAL_SERVER_ERROR, Map.empty),
             body = HttpEntity.Strict(ByteString(bodyJson.toString()), Some("application/json"))
           )            
      }
    }
   
}