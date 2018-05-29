package com.variant.client;

import com.variant.client.impl.ClientInternalError;
import com.variant.core.impl.UserError;
import com.variant.core.impl.VariantException;

/**
 * Superclass of all Variant client exceptions. All exceptions thrown by Variant Java Client
 * inherit from this class.
 * 
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
	 * Variant client internal exception, caused by an internal problem, most likely a bug.
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
	 * Variant client user exceptions, caused by an invalid user action.
	 * 
	 * @since 0.7
	 */
	public static class User extends ClientException {

		private UserError error = null;
		private Object[] args = null;
		
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
		 * @param msg
		 */
		public User(String msg) {
			super(msg);
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
			return error == null ? super.getMessage() : error.asMessage(args);
		}
	}
}
