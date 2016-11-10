package com.variant.server

import com.variant.core.exception.RuntimeErrorException
import com.variant.core.exception.RuntimeError

object ServerErrorException {
   
}

/**
 * 
 */
class ServerErrorException (error: RuntimeError, args: AnyRef*) extends RuntimeErrorException(error, args) {
  
}