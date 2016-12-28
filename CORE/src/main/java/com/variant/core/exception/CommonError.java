package com.variant.core.exception;

import com.variant.core.UserError;


/**
 * Expected user error that can be detected at run time.
 * @author Igor
 */
public class CommonError extends UserError {

	// Configuration errors
	public final static CommonError CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN = 
			new CommonError(Severity.FATAL, "Cannot set both [variant.config.resource] and [variant.config.file] system parameters");

	public final static CommonError CONFIG_RESOURCE_NOT_FOUND =
			new CommonError(Severity.FATAL, "Config resource [%s] could not be found"); 
	
	public final static CommonError CONFIG_FILE_NOT_FOUND =
			new CommonError(Severity.FATAL, "Config file [%s] could not be found"); 

	public final static CommonError CONFIG_PROPERTY_NOT_SET = 
			new CommonError(Severity.FATAL, "Configuration property [%s] must be set but is not");

	public static final CommonError CONFIG_INIT_PROPERTY_NOT_SET =
			new CommonError(Severity.ERROR, "Init property [%s] is required by class [%s] but is missing in system property [%s]");

	//public static final RuntimeError CONFIG_INIT_INVALID_JSON =
	//		new RuntimeError(Severity.ERROR, "Invalid JSON [%s] in system property [%s]");

	// Other errors
	public static final CommonError EXPERIENCE_WEIGHT_MISSING = 
			new CommonError(Severity.ERROR, "No weight specified for Test [%s], Experience [%s] and no custom targeter found");

	public static final CommonError STATE_NOT_INSTRUMENTED_BY_TEST =
			new CommonError(Severity.ERROR, "State [%s] is not instrumented by test [%s]");
	
	// SCHEMA_UNDEFINED                                  (Severity.ERROR, "Cannot create a session on an idle Variant instance. Deploy a schema first");
	// SESSION_EXPIRED                                   (Severity.ERROR, "This session has expired"); 
	// METHOD_UNSUPPORTED                                (Severity.ERROR, "Method unsupported in Core");

	// This is both runtime and parse time becuause some hooks post at parse time.
	public static final UserError  HOOK_LISTENER_EXCEPTION =
			new CommonError(Severity.ERROR, "User hook listener class [%s] threw an exception [%s]. See logs for details.");

	/**
	 * 
	 * @param severity
	 * @param format
	 */
	protected CommonError(Severity severity, String format) {
		super(severity, format);
	}

}
