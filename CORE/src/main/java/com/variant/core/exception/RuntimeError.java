package com.variant.core.exception;


public class RuntimeError extends Error {

	public static final RuntimeError EXPERIENCE_WEIGHT_MISSING = 
			new RuntimeError(Severity.ERROR, "No weight specified for Test [%s], Experience [%s] and no custom targeter found");

	public static final RuntimeError STATE_NOT_INSTRUMENTED_FOR_TEST =
			new RuntimeError(Severity.ERROR, "State [%s] is not instrumented for test [%s]");

	/**
	 * 
	 * @param severity
	 * @param format
	 */
	protected RuntimeError(Severity severity, String format) {
		super(severity, format);
	}

}
