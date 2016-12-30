package com.variant.server.boot

import org.apache.http.HttpStatus
import play.api.mvc.Result
import play.api.mvc.ResponseHeader
import play.api.http.HttpEntity
import play.api.libs.json._
import akka.util.ByteString
import play.api.libs.json.Json.toJsFieldJsValueWrapper

/**
 * API errors map to an network response.
 */
object ServerErrorApi {
    
   //
   // 601-610 Internal, Payload syntax error
   //
   val InternalError = new ServerErrorApi(601, "Internal server error [%s]")
   val JsonParseError = new ServerErrorApi(602, "JSON parsing error: '%s'")
   val BadContentType = new ServerErrorApi(603, "Unsupported content type", "Use 'application/json' or 'text/plain'.")
   
   //
   // 611-630 Internal, Payload parse error
   //
   val MissingProperty = new ServerErrorApi(611, "Missing required property '%s'")
   val InvalidDate = new ServerErrorApi(612, "Invalid date specification in property '%s'", "Epoch milliseconds expected")
   val UnsupportedProperty = new ServerErrorApi(613, "Unsupported property '%s' in payload")
   val PropertyNotAString = new ServerErrorApi(614, "Property '%s' must be a string")
   val EmptyBody = new ServerErrorApi(615, "Body expected but was null")
   val MissingParamName = new ServerErrorApi(616, "Parameter name is missing")
   
   //
   // 631-700 Internal, other internal errors.
   //

   //
   // 701-720 User, Connection
   //
   val UnknownSchema = new ServerErrorApi(701, "Unknown schema [%s]")
   val TooManyConnections = new ServerErrorApi(702, "Too many connections")

   //
   // 721-740 User, Connection
   //
   val SessionExpired = new ServerErrorApi(721, "Session expired")

   //
   // 741-760 User, Event
   //
   val UnknownState = new ServerErrorApi(741, "No recent state request in session")
}

/**
 * If user error, send HTTP 400 and the actual error.
 * If Internal error, log the error locally and send HTTP 500 and an internal error notification.
 */
class ServerErrorApi(val code: Int, val msgFormat: String, val comment: Option[String] = None) {
   
   def this(code: Int, msgFormat: String, comment: String) = this(code, msgFormat, Some(comment))

   import play.api.Logger
   import ServerErrorApi._
   
   val logger = Logger(this.getClass)
         
   def asResult(args:String*) = {
      
      if (code > 700) {
         val bodyJson : JsObject = Json.obj(
            "code" -> code,
            "message" -> message(args:_*))
        
         if (comment.isDefined) bodyJson + ("comment" -> JsString(comment.get))
         
         Result(
             header = ResponseHeader(HttpStatus.SC_BAD_REQUEST, Map.empty),
             body = HttpEntity.Strict(ByteString(bodyJson.toString()), Some("application/json"))
           )   
      }
      else {

         logger.error("Internal API error [%s] [%s]".format(code, message(args:_*)))

         
         val bodyJson : JsObject = Json.obj(
            "code" -> InternalError.code,
            "message" -> InternalError.message(code.toString))

         Result(
             header = ResponseHeader(HttpStatus.SC_INTERNAL_SERVER_ERROR, Map.empty),
             body = HttpEntity.Strict(ByteString(bodyJson.toString()), Some("application/json"))
           )            
      }
    }
   
   def message(args:String*) = String.format(msgFormat, args:_*)

}