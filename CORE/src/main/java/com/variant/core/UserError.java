package com.variant.core;

import com.variant.core.schema.ParserMessage;

/**
 * The immutable, uncontextualized part of a system error.
 * All errors are in subclasses.
 * 
 * @author Igor
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
171-200                                       Other
201-220              Configuration
241-250              --Available
251-270              Other common runtime
271-300              --Available

301-400 Client Local
301-330              Internal
331-400              User
        
401-600 Server local 
401-420              Server bootstrap
421-440              Schema deployment
441-460              Event writing
461-480              User                     Server API
481-500              Other server runtime
501-600              -- Available

601-800 Server
601-700              Internal
601-610                                       Payload syntax error
611-630                                       Payload parse error
631-700                                       Other internal errors
701-800              User, Client API
701-720                                       Connection
721-740                                       Session
741-760                                       Event
761-800                                       --Unused

801-999              Reserved


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

abstract public class UserError {
		
	protected final String msgFormat;
	private int code;
	private Severity severity;
	private String comment;
	
	protected UserError(int code, Severity severity, String msgFormat, String comment) {
		this.code = code;
		this.severity = severity;
		this.msgFormat = msgFormat;
		this.comment = comment;
	}

	protected UserError(int code, Severity severity, String msgFormat) {
		this(code, severity, msgFormat, null);
	}

	public int getCode() {
		return code;
	}
	
	public Severity getSeverity() {
		return severity;
	}
	
	public String getComment() {
		return comment;
	}
	
	/**
	 * Runtime message
	 * @param args
	 * @return
	 */
	public String asMessage(Object...args) {
		return String.format(msgFormat, args);
	}

	/**
	 * Severity of a {@link ParserMessage}.
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
		 * Error. If received at parse time, parser will proceed, but Variant will not deploy the schema.
		 * If received at run time, current operation will fail. 
		 * @since 0.5
		 */
		ERROR,
		/**
		 * Fatal Error. Variant environment is not usable.
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
