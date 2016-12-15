package com.variant.core.exception;


/**
 * Expected user error that can be detected at run time.
 * @author Igor
 */
public class RuntimeError extends Error {

	// Configuration errors
	public final static RuntimeError CONFIG_PROPERTY_NOT_SET = 
			new RuntimeError(Severity.FATAL, "Configuration property [%s] must be set but is not");

	public static final RuntimeError CONFIG_INIT_PROPERTY_NOT_SET =
			new RuntimeError(Severity.ERROR, "Init property [%s] is required by class [%s] but is missing in system property [%s]");

	public static final RuntimeError CONFIG_INIT_INVALID_JSON =
			new RuntimeError(Severity.ERROR, "Invalid JSON [%s] in system property [%s]");

	// Other errors
	public static final RuntimeError EXPERIENCE_WEIGHT_MISSING = 
			new RuntimeError(Severity.ERROR, "No weight specified for Test [%s], Experience [%s] and no custom targeter found");

	public static final RuntimeError STATE_NOT_INSTRUMENTED_BY_TEST =
			new RuntimeError(Severity.ERROR, "State [%s] is not instrumented by test [%s]");
	
	// ACTIVE_REQUEST                                    (Severity.ERROR, "Commit current state request first");
	// SCHEMA_UNDEFINED                                  (Severity.ERROR, "Cannot create a session on an idle Variant instance. Deploy a schema first");
	// SESSION_EXPIRED                                   (Severity.ERROR, "This session has expired"); 
	// METHOD_UNSUPPORTED                                (Severity.ERROR, "Method unsupported in Core");

	public static final Error  HOOK_LISTENER_EXCEPTION =
			new RuntimeError(Severity.ERROR, "User hook listener class [%s] threw an exception [%s]. See logs for details.");

	/**
	 * 
	 * @param severity
	 * @param format
	 */
	protected RuntimeError(Severity severity, String format) {
		super(severity, format);
	}

}
