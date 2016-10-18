package com.variant.server

import org.apache.http.HttpStatus
import play.api.mvc.Result
import play.api.mvc.ResponseHeader
import play.api.http.HttpEntity
import akka.util.ByteString

/**
 * 
 */
object UserError {

   val JsonParseError = 0
   val MissingProperty = 1
   val InvalidDate = 2
   val UnsupportedProperty = 3
   val PropertyNotAString = 4
   val EmptyBody = 5
   val UnknownState = 6
   val BadContentType = 7
   val SessionExpired = 8
   
   val errors = Array(
     
     // 400
     new UserError("JSON parsing error: '%s'.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Missing required property '%s'.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Invalid date specification in property '%s'. Epoch millis expected.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Unsupported property '%s' in payload.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Property '%s' must be a string.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Body expected but was null.", HttpStatus.SC_BAD_REQUEST),
     new UserError("No recent state request in session.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Unsupported content type. Use 'application/json' or 'text/plain'.", HttpStatus.SC_BAD_REQUEST),
     
     // 403
     new UserError("Session expired", HttpStatus.SC_FORBIDDEN)

   );
}

/**
 * 
 */
class UserError(msgFormat: String, httpStatus: Int) {
   
//   def toFailure(args:String*) = Result(Header(status = httpStatus, responsePhrase = message(args:_*)) 
   def asResult(args:String*) = {
      Result(
          header = ResponseHeader(httpStatus, Map.empty),
          body = HttpEntity.Strict(ByteString(asMessage(args:_*)), Some("text/plain"))
        )   
    }
   
   def asMessage(args:String*) = String.format(msgFormat, args:_*)

}