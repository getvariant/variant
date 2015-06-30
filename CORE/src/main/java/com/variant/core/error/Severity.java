package com.variant.core.error;

/**
 * Error Severity
 * 
 * @author Igor
 *
 */
public enum Severity {

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
