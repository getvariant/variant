package com.variant.server.config

import org.apache.http.HttpStatus
import net.liftweb.common.Failure
import net.liftweb.common.ParamFailure

/**
 * 
 */
object UserError {

   val MissingProperty = 0;
   val InvalidDate = 1;
   val UnsupportedProperty = 2;
   val ParamNotAString = 3
   
   val errors = Array(
     new UserError("Required property '%s' is missing.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Invalid date specification in property '%s'. Epoch millis expected.", HttpStatus.SC_BAD_REQUEST),
     new UserError("Unsupported property '%s' in payload", HttpStatus.SC_BAD_REQUEST),
     new UserError("Parameter '%s' must be a string", HttpStatus.SC_BAD_REQUEST)
   );
   
   //def apply(text: String, httpStatus: Int) = new UserError(text, httpStatus)
  
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