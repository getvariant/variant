package com.variant.core.schema;

/**
 * Represents a message generated by Variant schema parser. These are made available to
 * host code via {@link ParserResponse#getMessages()}.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface ParserMessage {
	
	/**
	 * Severity of the message.
	 * 
	 * @return An object of type {@link Severity}.
	 * @since 0.5
	 */
	public Severity getSeverity();
	
	/**
	 * Text of the message.
	 * 
	 * @return The text of the message.
	 * @since 0.5
	 */
	public String getText();
	
	/**
	 * Internally assigned code of this message. For reference purposes only.
	 * 
	 * @return Code string.
	 * @since 0.5
	 */
	public String getCode();

	/**
	 * Line number in the source document where the syntax error was encountered.
	 * 
	 * @return Line number, if this is a syntax error, or null if this is a semantical error.
	 * @since 0.5
	 */
	public Integer getLine();
	
	/**
	 * Column number in the source document where the syntax error was encountered.
	 * 
	 * @return Column number if this is a syntax error, or null if this is a semantical error.
	 * @since 0.5
	 */
	public Integer getColumn();
	
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
		public boolean greaterOrEqualThan(Severity other) {
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
		public boolean lessOrEqualThan(Severity other) {
			return ordinal() <= other.ordinal();
		}
	}
}
