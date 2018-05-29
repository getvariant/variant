package com.variant.core;

import com.variant.core.schema.ParserMessage;

/**
 * Superclass of all user errors, both client and server.
 * 
 * @since 0.5
 */

/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ERROR NUMBERS
=====================================================================
Range   Base         Major Area               Minor Area
------- ------------ ------------------------ -----------------------
001-300 Common       
001-200              Schema parser
001-020                                       Meta
021-050                                       State
051-150                                       Test
151-170                                       Parameters
171-180                                       Syntax
181-200                                       Other
201-220              Configuration
221-250              -- Available

251-300 Client Local
251-270              Internal
271-300              User

300-400 -- Available

401-600 Server Local 
401-420              Server bootstrap
421-450              Schema deployment

451-500              User                     Server API
501-520              Other server runtime

521-600              -- Available

601-800 Server
601-660              Internal
601-620                                       Payload syntax error
621-640                                       Payload parse error
641-660                                       Other internal errors
661-700              User  Server API
661-680                                       Life-cycle Hooks
681-700                                       Other
701-800              User, Client API

801-999              Reserved


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

abstract public class UserError {
		
	protected final String msgFormat;
	private int code;
	private Severity severity;
	
	protected UserError(int code, Severity severity, String msgFormat) {
		this.code = code;
		this.severity = severity;
		this.msgFormat = msgFormat;
	}

	public int getCode() {
		return code;
	}
	
	public Severity getSeverity() {
		return severity;
	}
		
	/**
	 * As a message.
	 * @param args
	 * @return
	 */
	public String asMessage(Object...args) {
		return "[" + code + "] " + String.format(msgFormat, args);
	}

	/**
	 * User error severity.
	 * 
	 * @since 0.5
	 */
	public enum Severity {

		/**
		 * Information only message.
		 * @since 0.5
		 */
		INFO,
		/**
		 * Warning. Current operation will proceed.
		 * @since 0.5
		 */
		WARN,
		/**
		 * Error. If received at parse time, parser may proceed, but Variant will not deploy the schema.
		 * If received at run time, current operation will fail. 
		 * @since 0.5
		 */
		ERROR,
		/**
		 * Fatal Error. Variant server is not functional and will shutdown.
		 * @since 0.5
		 */
		FATAL;

		/**
		 * Is other severity greater than this?
		 * @param other The other severity.
		 * @return True if other severity is greater than this.
		 * @since 0.5
		 */
		public boolean greaterThan(Severity other) {
			return ordinal() > other.ordinal();
		}

		/**
		 * Is other severity greater or equal than this?
		 * @param other The other severity.
		 * @return True if other severity is greater or equal to this.
		 * @since 0.5
		 */
		public boolean greaterOrEqual(Severity other) {
			return ordinal() >= other.ordinal();
		}

		/**
		 * Is other severity less than this?
		 * @param other The other severity.
		 * @return True if other severity is less than this.
		 * @since 0.5
		 */
		public boolean lessThan(Severity other) {
			return ordinal() < other.ordinal();
		}

		/**
		 * Is other severity less than this?
		 * @param other The other severity.
		 * @return True if other severity is less or equal to this.
		 * @since 0.5
		 */
		public boolean lessOrEqual(Severity other) {
			return ordinal() <= other.ordinal();
		}
	}

}
