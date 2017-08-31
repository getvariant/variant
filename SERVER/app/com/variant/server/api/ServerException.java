package com.variant.server.api;

import com.variant.core.UserError;
import com.variant.core.VariantException;
import com.variant.core.UserError.Severity;
import com.variant.core.ServerError;

/**
 * The super-type for of Variant server exception. 
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ServerException extends VariantException {

	private ServerException() {
		super();
	}
	
	private ServerException(String msg) {
		super(msg);
	}

	private ServerException(Throwable t) {
		super(t);
	}

	private ServerException(String msg, Throwable t) {
		super(msg, t);
	}
	
	/**
	 * Server internal exceptions. These are not the result of an invalid user action, but are due to an internal problem.
	 * 
	 * @since 0.7
	 */
	public static class Internal extends ServerException {
		
		public Internal(String msg) {
			super(msg);
		}

		public Internal(String msg, Throwable t) {
			super(msg, t);
		}
	}
	
	/**
	 * Server local user exceptions. These are the result of an invalid user action committed over the server extension API.
	 * 
	 * @since 0.7
	 */
	public static class User extends ServerException {

		public final UserError error;
		public final String[] args;
		
		/**
		 */
		public User(UserError error, String...args) {
			super();
			this.error = error;
			this.args = args;
		}

		/**
		 */
		public User(UserError error, Throwable t, String...args) {
			super(t);
			this.error = error;
			this.args = args;
		}

		/**
		 */
		public Severity getSeverity() {
			return error.getSeverity();
		}
		
		/**
		 */
		@Override
		public String getMessage() {
			return "[" + error.getCode() + "] " + error.asMessage((Object[])args);
		}

		/**
		 */
		public String getComment() {
			return error.getComment();
		}
	}
	
	/**
	 * Server remote user exceptions. These are the result of an invalid user action committed over a client API.
	 * 
	 * @since 0.7
	 */
	public static class Remote extends ServerException {

		public final ServerError error;
		public final String[] args;
		
		/**
		 * 
		 * @param template
		 * @param args
		 */
		public Remote(ServerError error, String...args) {
			super();
			this.error = error;
			this.args = args;
		}
	}

}
