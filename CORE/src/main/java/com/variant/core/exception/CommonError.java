package com.variant.core.exception;

import com.variant.core.UserError;


/**
 * User errors that can be signaled on server or client.
 * @author Igor
 */
public class CommonError extends UserError {

	// 
	// 201-220              Configuration
	//
	public final static CommonError CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN = 
			new CommonError(201, Severity.FATAL, "Cannot set both [variant.config.resource] and [variant.config.file] system parameters");

	public final static CommonError CONFIG_RESOURCE_NOT_FOUND =
			new CommonError(202, Severity.FATAL, "Config resource [%s] could not be found"); 
	
	public final static CommonError CONFIG_FILE_NOT_FOUND =
			new CommonError(203, Severity.FATAL, "Config file [%s] could not be found"); 

	public final static CommonError CONFIG_PROPERTY_NOT_SET = 
			new CommonError(204, Severity.FATAL, "Configuration property [%s] must be set but is not");

//	public static final CommonError CONFIG_INIT_PROPERTY_NOT_SET =
//			new CommonError(5, Severity.ERROR, "Init property [%s] is required by class [%s] but is missing in system property [%s]");

	//public static final RuntimeError CONFIG_INIT_INVALID_JSON =
	//		new RuntimeError(Severity.ERROR, "Invalid JSON [%s] in system property [%s]");
	
	// SCHEMA_UNDEFINED                                  (Severity.ERROR, "Cannot create a session on an idle Variant instance. Deploy a schema first");
	// SESSION_EXPIRED                                   (Severity.ERROR, "This session has expired"); 
	// METHOD_UNSUPPORTED                                (Severity.ERROR, "Method unsupported in Core");

	//
	// 241-250              Parse time user hooks
	//
	public static final CommonError  HOOK_LISTENER_EXCEPTION =
			new CommonError(241, Severity.ERROR, "User hook listener class [%s] threw an exception [%s]. See logs for details.");

	//
	// 251-270              Common runtime
	//
	public static final CommonError HOOK_LISTENER_ERROR =
			new CommonError(251, Severity.ERROR, "User hook listener raised the following error: [%s]");

	public static final CommonError STATE_NOT_INSTRUMENTED_BY_TEST =
			new CommonError(252, Severity.ERROR, "State [%s] is not instrumented by test [%s]");

	/**
	 * 
	 * @param severity
	 * @param format
	 */
	protected CommonError(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}
