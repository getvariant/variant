package com.variant.client;

import com.variant.client.impl.ClientInternalError;
import com.variant.core.UserError;
import com.variant.core.VariantException;

/**
 * Superclass for all Variant client exceptions.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ClientException extends VariantException {
	
	private ClientException() {
		super();
	}
	
	private ClientException(String msg) {
		super(msg);
	}

	private ClientException(Throwable t) {
		super(t);
	}

	private ClientException(String msg, Throwable t) {
		super(msg, t);
	}
	
	/**
	 * Client internal exceptions. 
	 * These are not the result of an invalid user action, but an internal bug.
	 * 
	 * @since 0.7
	 */
	public static class Internal extends ClientException {
		
		public Internal(Throwable t) {
			super(t);
		}

		public Internal(String msg) {
			super(msg);
		}

		public Internal(String msg, Throwable t) {
			super(msg, t);
		}
		
		public Internal(ClientInternalError error, Object...args) {
			super(error.asMessage(args));
		}

	}
	
	/**
	 * Client User exceptions. 
	 * These are the result of an invalid user action.
	 * 
	 * @since 0.7
	 */
	public static class User extends ClientException {

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
		public UserError getError() {
			return error;
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
