package com.variant.core.exception;


/**
 * Expected user error that can be detected at run time.
 * @author Igor
 */
public class RuntimeError extends Error {

	public final static RuntimeError CONFIG_PROPERTY_NOT_SET = 
			new RuntimeError(Severity.FATAL, "Configuration property [%s] must be set but is not");

	public static final RuntimeError EXPERIENCE_WEIGHT_MISSING = 
			new RuntimeError(Severity.ERROR, "No weight specified for Test [%s], Experience [%s] and no custom targeter found");

	public static final RuntimeError STATE_NOT_INSTRUMENTED_BY_TEST =
			new RuntimeError(Severity.ERROR, "State [%s] is not instrumented by test [%s]");

	// PROPERTY_BAD_CLASS                                (Severity.ERROR, "Don't know how to convert to class [%s]");
	
	public static final RuntimeError PROPERTY_INIT_INVALID_JSON =
			new RuntimeError(Severity.ERROR, "Invalid JSON [%s] in system property [%s]");
	
	public static final RuntimeError PROPERTY_INIT_PROPERTY_NOT_SET =
			new RuntimeError(Severity.ERROR, "Init property [%s] is required by class [%s] but is missing in system property [%s]");
	
	// ACTIVE_REQUEST                                    (Severity.ERROR, "Commit current state request first");
	// SCHEMA_OBSOLETE                                   (Severity.ERROR, "Operation failed becuase this schema (ID [%s]) has been undeployed");
	// SCHEMA_MODIFIED                                   (Severity.ERROR, "Opereation failed because the current schema ID [%s] differs from that with which this session was created [%s]");
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
