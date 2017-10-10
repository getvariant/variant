package com.variant.core.schema.parser.error;


/**
 * Collateral messages are emitted or caused by user code.
 * No schema source location.
 */
public class CollateralMessage extends SemanticError {

	/**
	 * 
	 */
	public CollateralMessage(int code, Severity severity, String format) {
		super(code, severity, format);
	}

	// 
	// 241-250 Collateral messages emitted or caused by user code.
	// 
	
	public static final CollateralMessage  HOOK_UNHANDLED_EXCEPTION =
			new CollateralMessage(241, Severity.ERROR, "User hook class [%s] threw an exception [%s]. See logs for details.");

	public static final CollateralMessage HOOK_USER_MESSAGE_INFO =
			new CollateralMessage(242, Severity.INFO, "User hook generated the following message: [%s] [%s]");

	public static final CollateralMessage HOOK_USER_MESSAGE_WARN =
			new CollateralMessage(243, Severity.WARN, "User hook generated the following message: [%s] [%s]");

	public static final CollateralMessage HOOK_USER_MESSAGE_ERROR =
			new CollateralMessage(244, Severity.ERROR, "User hook generated the following message: [%s] [%s]");

}
