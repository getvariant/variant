package com.variant.core.impl;

import com.variant.core.UserError;
import com.variant.core.UserError.Severity;


/**
 * The super-type for all Variant server exception. 
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class CoreException extends VariantException {

	private CoreException() {
		super();
	}
	
	private CoreException(String msg) {
		super(msg);
	}

	private CoreException(Throwable t) {
		super(t);
	}

	private CoreException(String msg, Throwable t) {
		super(msg, t);
	}
	
	/**
	 * Server internal exceptions. These are not the result of an invalid user action, but an internal bug.
	 * 
	 * @since 0.7
	 */
	public static class Internal extends CoreException {
		
		public Internal(Throwable t) {
			super(t);
		}

		public Internal(String msg) {
			super(msg);
		}

		public Internal(String msg, Throwable t) {
			super(msg, t);
		}
	}
	
	public static class User extends CoreException {

		public final UserError error;
		public final Object[] args;
		
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
			return error.getSeverity();
		}
		
		/**
		 * 
		 * @return
		 */
		@Override
		public String getMessage() {
			return "[" + error.getCode() + "] " + error.asMessage(args);
		}

	}

}
