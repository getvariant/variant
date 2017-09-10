package com.variant.core;

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


	//
	// 241-250  Available
	//
	
	//
	// 251-270              Common runtime, including common hook errors.
	//
	public static final CommonError STATE_NOT_INSTRUMENTED_BY_TEST =
			new CommonError(251, Severity.ERROR, "State [%s] is not instrumented by test [%s]");

	public static final CommonError  HOOK_UNHANDLED_EXCEPTION =
			new CommonError(252, Severity.ERROR, "User hook class [%s] threw an exception [%s]. See logs for details.");

	/**
	 * 
	 * @param severity
	 * @param format
	 */
	protected CommonError(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}
