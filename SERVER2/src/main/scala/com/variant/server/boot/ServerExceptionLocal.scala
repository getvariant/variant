package com.variant.server.boot;

import com.variant.core.error.UserError;
import com.variant.core.error.UserError.Severity;
import com.variant.server.api.ServerException;

/**
 * Server local user exceptions. These are the result of an invalid user action committed over the server extension API.
 *
 * @since 0.7
 */

class ServerExceptionLocal(val error: ServerMessageLocal, t: Throwable, args: String*)
   extends ServerException(error.asMessage(args: _*), t) {

   override def getMessage: String = error.asMessage(args: _*);

   override def equals(that: Any) = {
      that != null &&
         that.isInstanceOf[ServerExceptionLocal] &&
         getMessage == that.asInstanceOf[ServerExceptionLocal].getMessage
   }

}

object ServerExceptionLocal {

   def apply(error: ServerMessageLocal, args: String*) = new ServerExceptionLocal(error, null, args: _*)

}
