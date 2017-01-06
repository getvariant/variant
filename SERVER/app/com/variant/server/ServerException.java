package com.variant.server;

import com.variant.core.UserError;
import com.variant.core.VariantException;
import com.variant.core.UserError.Severity;

/**
 * The super-type for all Variant server exception. 
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
	 * Server internal exceptions. These are not the result of an invalid user action, but an internal bug.
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
	
	public static class User extends ServerException {

		private static final long serialVersionUID = 1L;
		private UserError error;
		private Object[] args;
		
		/**
		 * 
		 * @param template
		 * @param args
		 */
		public User(UserError error, Object...args) {
			super();
			this.error = error;
			this.args = args;
		}

		/**
		 * 
		 * @param template
		 * @param t
		 * @param args
		 */
		public User(UserError error, Throwable t, Object...args) {
			super(t);
			this.error = error;
			this.args = args;
		}

		/**
		 * 
		 * @return
		 */
		public Severity getSeverity() {
			return error.severity;
		}
		
		/**
		 * 
		 * @return
		 */
		@Override
		public String getMessage() {
			return error.asMessage(args);
		}

		/**
		 * 
		 * @return
		 */
		public String getComment() {
			return error.comment;
		}

	}

}
