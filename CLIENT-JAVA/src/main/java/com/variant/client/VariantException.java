package com.variant.client;

import com.variant.client.impl.ClientInternalError;
import com.variant.share.error.UserError;

/**
 * Superclass of all user exceptions thrown by Variant Java Client
 * 
 * @since 0.7
 */
@SuppressWarnings("serial")
public class VariantException extends RuntimeException {
	
   public final UserError error;
	public final String[] args;
	public final Throwable cause;
	
   /**
    * 
    * @param template
    * @param args
    */
   public VariantException(UserError error, Throwable cause, String...args) {
      this.error = error;
      this.cause = cause;
      this.args = args;
   }

   /**
	 * 
	 * @param template
	 * @param args
	 */
	public VariantException(UserError error, String...args) {
	   this(error, null, args);
	}

   /**
	 * 
	 * @return
	 */
	@Override
	public String getMessage() {
		return error.asMessage((Object[])args);
	}
	
	/**
	 */
	public static VariantException internal(String message, Throwable cause) {
      return new VariantException(ClientInternalError.INTERNAL_ERROR, cause, message);
   }

   /**
    */
   public static VariantException internal(String message) {
      return new VariantException(ClientInternalError.INTERNAL_ERROR, message);
   }

   /**
    */
   public static VariantException internal(ClientInternalError error, String...args) {
      return new VariantException(error, args);
   }

}
