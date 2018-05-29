package com.variant.server.api;

import com.variant.core.impl.CommonError;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.UserError.Severity;
import com.variant.core.impl.VariantException;
/**
 * The super-type for of Variant server exception. 
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
abstract public class ServerException extends VariantException {

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
	 * @return
	 */
	abstract public Severity getSeverity();

	@Override
	public String toString() {
		return String.format("[%s] %s", getSeverity(), getMessage());
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
		
		@Override
		public Severity getSeverity() {
			return Severity.FATAL;
		}

	}
	
	/**
	 * Server local user exceptions. These are the result of an invalid user action committed over the server extension API.
	 * 
	 * @since 0.7
	 */
	public static class Local extends ServerException {

		public final CommonError error;
		public final String[] args;
		
		/**
		 */
		public Local(CommonError error, String...args) {
			super();
			this.error = error;
			this.args = args;
		}

		/**
		 */
		public Local(CommonError error, Throwable t, String...args) {
			super(t);
			this.error = error;
			this.args = args;
		}

		/**
		 */
		@Override
		public Severity getSeverity() {
			return error.getSeverity();
		}
		
		/**
		 */
		@Override
		public String getMessage() {
			return error.asMessage((Object[])args);
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
		
		@Override
		public Severity getSeverity() {
			return Severity.ERROR;
		}

		/**
		 */
		@Override
		public String getMessage() {
			return error.asMessage((Object[])args);
		}

	}

}
