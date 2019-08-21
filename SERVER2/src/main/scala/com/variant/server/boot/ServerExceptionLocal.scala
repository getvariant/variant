package com.variant.server.boot;

import com.variant.core.error.UserError;
import com.variant.core.error.UserError.Severity;
import com.variant.server.api.ServerException;

/**
 * Server local user exceptions. These are the result of an invalid user action committed over the server extension API.
 *
 * @since 0.7
 */

case class ServerExceptionLocal(error: ServerMessageLocal, t: Throwable, args: String*)
   extends ServerException(error.asMessage(args: _*), t) {

   def this(error: ServerMessageLocal, args: String*) = this(error, null, args: _*)

   override def getMessage: String = error.asMessage(args: _*);

   override def equals(that: Any) = {
      that != null &&
         that.isInstanceOf[ServerExceptionLocal] &&
         getMessage == that.asInstanceOf[ServerExceptionLocal].getMessage
   }

}

object ServerExceptionLocal {

   @annotation.varargs
   def apply(error: ServerMessageLocal, t: Throwable, args: String*) = new ServerExceptionLocal(error, t, args: _*)

   @annotation.varargs
   def apply(error: ServerMessageLocal, args: String*) = new ServerExceptionLocal(error, args: _*)

}
