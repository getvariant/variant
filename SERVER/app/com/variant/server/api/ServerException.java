package com.variant.server.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.variant.core.UserError;
import com.variant.core.VariantException;
import com.variant.core.UserError.Severity;
import com.variant.core.ServerError;
import com.variant.core.util.Tuples.Pair;
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
		
		private final HashMap<String,String> headers = new HashMap<String,String>();
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

		/**
		 * Attache headers to the response that this exception will
		 * be eventually converted to
		 * @param headers
		 * @return this object for ease of chaining.
		 */
		public Remote withHeaders(Map<String, String> headers) {
			this.headers.putAll(headers);
			return this;
		}

		/**
		 * Currently attached headers
		 */
		public Map<String, String> getHeaders() {
			return headers;
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
