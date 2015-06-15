package com.variant.core.config.parser;

/**
 * Parser error.
 * 
 * @author Igor
 */
public class ParserError {
	
	private ParserErrorTemplate template;
	private Integer line = null, column = null;
	private String[] args;
	
	/**
	 * 
	 * @param template
	 * @param args
	 */
	ParserError(ParserErrorTemplate template, String...args) {
		this.template = template;
		this.args = args;
	}

	/**
	 * 
	 * @param template
	 * @param line
	 * @param column
	 * @param args
	 */
	ParserError(ParserErrorTemplate template, int line, int column, String...args) {
		this(template, args);
		this.line = line;
		this.column = column;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * How severe is the error
	 */
	public static enum Severity {
		
		// --- Order is important --- //
		NONE,     // Nothing.
		INFO,     // Information only. Not an error.
		WARN,     // Parse can continue, fit for run time.
		ERROR,    // Parse can continue, but not fit for run time
		FATAL;    // Parse cannot continue.

		/**
		 * Is other severity greater than this?
		 * @param other
		 * @return
		 */
		public boolean greaterThan(Severity other) {
			return ordinal() > other.ordinal();
		}

		/**
		 * Is other severity less than this?
		 * @param other
		 * @return
		 */
		public boolean lessThan(Severity other) {
			return ordinal() < other.ordinal();
		}

		/**
		 * Is other severity greater or equal than this?
		 * @param other
		 * @return
		 */
		public boolean greaterOrEqualThan(Severity other) {
			return ordinal() >= other.ordinal();
		}

	}

	/**
	 * 
	 * @return
	 */
	public Severity getSeverity() {
		return template.getSeverity();
	}

	/**
	 * 
	 * @return
	 */
	public String getMessage() {
		return String.format(template.getFormat(), (Object[]) args);
	}

	/**
	 * 
	 * @return
	 */
	public Integer getLine() {
		return line;
	}
	
	/**
	 * 
	 * @return
	 */
	public Integer getColumn() {
		return column;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		
		return getSeverity().name() + " - " + getMessage() + (line == null ? "" : String.format(" @ line %s, column %s", line, column));
	}

}
