package com.variant.server.config

import org.apache.http.HttpStatus
import net.liftweb.common.Failure
import net.liftweb.common.ParamFailure

/**
 * 
 */
object UserError {

   val MissingProperty = 0
   val InvalidDate = 1
   val UnsupportedProperty = 2
   val PropertyNotAString = 3
   val EmptyBody = 4
   val UnknownState = 5
   val SessionExpired = 6
   
   val errors = Array(
     
     // 400
     new UserError("Required property '%s' is missing.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Invalid date specification in property '%s'. Epoch millis expected.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Unsupported property '%s' in payload", HttpStatus.SC_BAD_REQUEST),
     new UserError("Property '%s' must be a string", HttpStatus.SC_BAD_REQUEST),
     new UserError("Empty body", HttpStatus.SC_BAD_REQUEST),
     new UserError("Unknown state", HttpStatus.SC_BAD_REQUEST),
     
     // 403
     new UserError("Session expired", HttpStatus.SC_FORBIDDEN)

   );
   
}

/**
 * 
 */
class UserError(msgFormat: String, httpStatus: Int) {
   
   /**
    * 
    */
   def toFailure(args:String*) = ParamFailure(message(args:_*), httpStatus)
   
   /**
    * Tests use this directly.
    */
   def message(args:String*) = String.format(msgFormat, args:_*)
}