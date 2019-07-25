package com.variant.server.boot;

import com.variant.core.error.UserError;
import com.variant.core.error.UserError.Severity;
import com.variant.server.api.ServerException;

/**
 * Server local user exceptions. These are the result of an invalid user action committed over the server extension API.
 *
 * @since 0.7
 */

class ServerExceptionLocal(error: ServerMessageLocal, t: Throwable, args: String*)
   extends ServerException(error.asMessage(args), t) {

   override def getSeverity: Severity = error.getSeverity()

   override def getMessage: String = error.asMessage(args: _*);

}

object ServerExceptionLocal {

   def apply(error: ServerMessageLocal, args: String*) = new ServerExceptionLocal(error, null, args: _*)

}
