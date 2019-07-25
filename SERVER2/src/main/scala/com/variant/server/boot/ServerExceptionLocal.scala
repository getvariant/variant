package com.variant.server.boot;

import com.variant.core.error.UserError;
import com.variant.core.error.UserError.Severity;
import com.variant.server.api.ServerException;

/**
 * Server local user exceptions. These are the result of an invalid user action committed over the server extension API.
 *
 * @since 0.7
 */

class ServerExceptionLocal(error: ServerMessageLocal, t: Throwable, args: Object*)
   extends ServerException(error.asMessage(args), t) {

   /**
    */
   override def getSeverity: Severity = error.getSeverity()

   /**
    */
   override def getMessage: String = error.asMessage(args);

}

object ServerExceptionLocal {

   @annotation.varargs
   def apply(error: ServerMessageLocal, args: Object*) = new ServerExceptionLocal(error, null, args)

}
