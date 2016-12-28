package com.variant.core;

/**
 * The immutable, uncontextualized part of a system error.
 * All errors are in subclasses.
 * 
 * @author Igor
 */
abstract public class UserError {
		
	public final Severity severity;
	public final String format;
	public final String code;
	
	protected UserError(Severity severity, String format) {
		this.severity = severity;
		this.format = format;
		this.code = getClass().getSimpleName();
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
		 * Fatal Error. Current operation will fail.
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
