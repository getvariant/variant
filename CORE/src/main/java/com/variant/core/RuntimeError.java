package com.variant.core;


/**
 * User errors that can be signaled on server or client.
 * @author Igor
 */
public class RuntimeError extends UserError {

	// 
	// 201-220              Configuration
	//
	public final static RuntimeError CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN = 
			new RuntimeError(201, Severity.FATAL, "Cannot set both [variant.config.resource] and [variant.config.file] system parameters");

	public final static RuntimeError CONFIG_RESOURCE_NOT_FOUND =
			new RuntimeError(202, Severity.FATAL, "Config resource [%s] could not be found"); 
	
	public final static RuntimeError CONFIG_FILE_NOT_FOUND =
			new RuntimeError(203, Severity.FATAL, "Config file [%s] could not be found"); 

	public final static RuntimeError CONFIG_PROPERTY_NOT_SET = 
			new RuntimeError(204, Severity.FATAL, "Configuration property [%s] must be set, but is not");

	// 
	// 241-250 Collateral messages emitted or caused by user code.
	// 
	
	public static final RuntimeError  HOOK_UNHANDLED_EXCEPTION =
			new RuntimeError(241, Severity.ERROR, "User hook class [%s] threw an exception [%s]. See logs for details.");

	public static final RuntimeError HOOK_USER_MESSAGE_INFO =
			new RuntimeError(242, Severity.INFO, "User hook generated the following message: [%s]");

	public static final RuntimeError HOOK_USER_MESSAGE_WARN =
			new RuntimeError(243, Severity.WARN, "User hook generated the following message: [%s]");

	public static final RuntimeError HOOK_USER_MESSAGE_ERROR =
			new RuntimeError(244, Severity.ERROR, "User hook generated the following message: [%s]");

	//
	// 251-270              Common runtime, shared btwn client and server.
	//
	public static final RuntimeError STATE_NOT_INSTRUMENTED_BY_TEST =
			new RuntimeError(251, Severity.ERROR, "State [%s] is not instrumented by test [%s]");

	/**
	 * 
	 * @param severity
	 * @param format
	 */
	protected RuntimeError(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}
