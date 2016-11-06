package com.variant.core.exception;


public class RuntimeError extends Error {

	public static final RuntimeError EXPERIENCE_WEIGHT_MISSING = 
			new RuntimeError(Severity.ERROR, "No weight specified for Test [%s], Experience [%s] and no custom targeter found");

	public static final RuntimeError STATE_NOT_INSTRUMENTED_FOR_TEST =
			new RuntimeError(Severity.ERROR, "State [%s] is not instrumented for test [%s]");

	// PROPERTY_BAD_CLASS                                (Severity.ERROR, "Don't know how to convert to class [%s]");
	
	public static final RuntimeError PROPERTY_INIT_INVALID_JSON =
			new RuntimeError(Severity.ERROR, "Invalid JSON [%s] in system property [%s]");
	// PROPERTY_INIT_PROPERTY_NOT_SET                    (Severity.ERROR, "Init property [%s] is required by class [%s] but is missing in system property [%s]");
	// ACTIVE_REQUEST                                    (Severity.ERROR, "Commit current state request first");
	// SCHEMA_OBSOLETE                                   (Severity.ERROR, "Operation failed becuase this schema (ID [%s]) has been undeployed");
	// SCHEMA_MODIFIED                                   (Severity.ERROR, "Opereation failed because the current schema ID [%s] differs from that with which this session was created [%s]");
	// SCHEMA_UNDEFINED                                  (Severity.ERROR, "Cannot create a session on an idle Variant instance. Deploy a schema first");
	// SESSION_EXPIRED                                   (Severity.ERROR, "This session has expired"); 
	// METHOD_UNSUPPORTED                                (Severity.ERROR, "Method unsupported in Core");
	// STATE_UNDEFINED_IN_EXPERIENCE                     (Severity.ERROR, "Currently active experience [%s] is undefined on state [%s]");
	// HOOK_TARGETING_BAD_EXPERIENCE                     (Severity.ERROR, "Targeting hook [%s] for test [%s] cannot set experience [%s]");
	// HOOK_TARGETING_UNDEFINED_EXPERIENCE               (Severity.ERROR, "Targeting hook [%s] for test [%s] cannot set experience [%s], which is undefined on state [%s]");

	/**
	 * 
	 * @param severity
	 * @param format
	 */
	protected RuntimeError(Severity severity, String format) {
		super(severity, format);
	}

}
