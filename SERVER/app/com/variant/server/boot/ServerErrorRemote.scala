package com.variant.server.boot

import play.api.mvc.Result
import play.api.mvc.ResponseHeader
import play.api.http.HttpEntity
import play.api.libs.json._
import akka.util.ByteString
import com.variant.core.ServerError
import com.variant.server.api.ServerException
import play.api.mvc.Results

/**
 * Wrap the core ServerError type with some server side semantics.
 */
object ServerErrorRemote {
   
   def apply(error: ServerError) = new ServerErrorRemote(error)
}

class ServerErrorRemote(error: ServerError) extends Results {
   
   import play.api.Logger
   
   val logger = Logger(this.getClass)
    
   /**
    * Serialize the runtime error as json in a way that will allow the client to
    * deserialize it as an error.
    */
   def asResult(args:String*): Result = {
     
      // Internal errors are also logged, along with the call stack.
      if (error.isInternal) {
         val msg = new StringBuilder("Internal API error: %s".format(error.asMessage(args:_*)))
         // drop the first frame as it will be currentThread().
         Thread.currentThread().getStackTrace.drop(1).foreach(se => msg.append("\n  at ").append(se))
         logger.error(msg.toString())
      }
      
      val bodyJson : JsObject = Json.obj(
         "isInternal" -> JsBoolean(error.isInternal),
         "code" -> error.getCode,
         "args" -> JsArray(args.map {JsString(_)}))
     
      BadRequest(bodyJson.toString())
//          header = ResponseHeader(HttpStatus.SC_BAD_REQUEST, Map.empty),
//          body = HttpEntity.Strict(ByteString(bodyJson.toString()), Some("application/json"))
//        )   

    }
   
}