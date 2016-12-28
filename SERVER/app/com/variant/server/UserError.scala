package com.variant.server

import org.apache.http.HttpStatus
import play.api.mvc.Result
import play.api.mvc.ResponseHeader
import play.api.http.HttpEntity
import play.api.libs.json
import play.api.libs.json._
import akka.util.ByteString

/**
 * ALL 400 BAD_REQUEST.
 */
object UserError {
        
   // Payload parse errors, syntax 001 - 125
   val JsonParseError = new UserError(1, "JSON parsing error: '%s'")
   val BadContentType = new UserError(2, "Unsupported content type", "Use 'application/json' or 'text/plain'.")
   
   // Payload parse errors, semantical 126-150
   val MissingProperty = new UserError(126, "Missing required property '%s'")
   val InvalidDate = new UserError(127, "Invalid date specification in property '%s'", "Epoch milliseconds expected")
   val UnsupportedProperty = new UserError(128, "Unsupported property '%s' in payload")
   val PropertyNotAString = new UserError(129, "Property '%s' must be a string")
   val EmptyBody = new UserError(130, "Body expected but was null")
   val MissingParamName = new UserError(131, "Parameter name is missing")
   
   // Event Writing errors 151 - 200
   val UnknownState = new UserError(151, "No recent state request in session")
   
   // Connection errors 201 - 250
   val UnknownSchema = new UserError(201, "Unknown schema [%s]")
   val TooManyConnections = new UserError(202, "Too many connections")
  
   // Session errors 251 - 300
   val SessionExpired = new UserError(251, "Session expired")

}

/**
 * 
 */
class UserError(val code: Int, val msgFormat: String, val comment: Option[String] = None) {
   
   def this(code: Int, msgFormat: String, comment: String) = this(code, msgFormat, Some(comment))
   
//   def toFailure(args:String*) = Result(Header(status = httpStatus, responsePhrase = message(args:_*)) 
   
   def asResult(args:String*) = {
      
      val bodyJson : JsObject = Json.obj(
         "code" -> code,
         "message" -> message(args:_*))
     
      if (comment.isDefined) bodyJson + ("comment" -> JsString(comment.get))
      
      Result(
          header = ResponseHeader(HttpStatus.SC_BAD_REQUEST, Map.empty),
          body = HttpEntity.Strict(ByteString(bodyJson.toString()), Some("application/json"))
        )   
    }
   
   def message(args:String*) = String.format(msgFormat, args:_*)

}