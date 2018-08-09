package com.variant.core.impl;

import java.lang.reflect.Field;

import com.variant.core.util.ReflectUtils;

/**
 * User errors that are emitted by the server to be sent across the network to the client.
 * 
 * @author Igor
 */
public class ServerError extends CommonError {
	
	//
	// 601-620 Internal, Payload syntax error
	//
	public static final ServerError InternalError = 
			new ServerError(601, "Internal server error [%s]");
	
	public static final ServerError JsonParseError = 
			new ServerError(602, "JSON parsing error: '%s'");
	
	public static final ServerError BadContentType = 
			new ServerError(603, "Unsupported content type, use 'application/json'");
	   
	//
	// 621-640 Internal, Payload parse error
	//
	public static final ServerError MissingProperty = 
			new ServerError(621, "Missing required property '%s'");
	
	public static final ServerError InvalidDate = 
			new ServerError(622, "Invalid date specification in property '%s', epoch milliseconds expected");
	
	public static final ServerError UnsupportedProperty = 
			new ServerError(623, "Unsupported property '%s' in payload");
	
	public static final ServerError PropertyNotAString = 
			new ServerError(624, "Property '%s' must be a string");
	
	public static final ServerError EmptyBody = 
			new ServerError(625, "Body expected but was null");
	
	public static final ServerError MissingParamName = 
			new ServerError(626, "Parameter name is missing");

     public static final ServerError MissingStateVisitedEvent = 
    		new ServerError(627, "State visited event was not sent when expected");

	//
	// 641-660 Internal, other.
	//


	/**
	 */
	public boolean isInternal() {
		return getCode() <= 660;
	}

	//
	// 661-700 User, Server API
	//

	// 
	// 661-680 User, Server API, Hooks
	// 
	public static final ServerError  HOOK_UNHANDLED_EXCEPTION =
			new ServerError(661, "Life-cycle hook class [%s] threw an exception [%s]. See server logs for details.");

	public static final ServerError HOOK_USER_MESSAGE_INFO =
			new ServerError(662, Severity.INFO, "Life-cycle hook generated the following message: [%s]");

	public static final ServerError HOOK_USER_MESSAGE_WARN =
			new ServerError(663, Severity.WARN, "Life-cycle hook generated the following message: [%s]");

	public static final ServerError HOOK_USER_MESSAGE_ERROR =
			new ServerError(664, Severity.ERROR, "Life-cycle hook generated the following message: [%s]");

	public final static ServerError HOOK_TARGETING_BAD_EXPERIENCE = 
			new ServerError(665, "Targeting hook [%s] for test [%s] cannot set experience [%s]");

	// 
	// 681-700 User, Server API, Other
	// 
	public static final ServerError STATE_NOT_INSTRUMENTED_BY_TEST =
			new ServerError(681, "State [%s] is not instrumented by test [%s]");

	//
	// 701-800 User, Client API
	//
	public static final ServerError UNKNOWN_SCHEMA = 
			new ServerError(701, "Unknown schema [%s]");
	
	public static final ServerError WRONG_CONNECTION = 
			new ServerError(702, "Cannot access existing session over connection to [%s]");
	
	public static final ServerError ACTIVE_REQUEST =
			new ServerError(703, "In-progress state request found in session. Commit or cancel it first");

	public static final ServerError SESSION_EXPIRED = 
			new ServerError(704, "Session ID [%s] does not exist");

	public static final ServerError UNKNOWN_STATE = 
			new ServerError(705, "No state request in session. Target this session for a state first");

	public final static ServerError STATE_UNDEFINED_IN_EXPERIENCE =
			new ServerError(706, "Currently active experience [%s] is undefined on state [%s]");

	public static final ServerError EXPERIENCE_WEIGHT_MISSING = 
			new ServerError(707, "No weight specified for Test [%s], Experience [%s] and no custom targeter found");


	/**
	 * Get the error by its code.
	 * 
	 * @param code
	 * @return
	 */
	public static ServerError byCode(int code) {
		try {
			for (Field f: ReflectUtils.getStaticFields(ServerError.class, ServerError.class)) {
				ServerError e = (ServerError) f.get(null);
				if (e.getCode() == code) return e;
			}
		}
		catch (Exception e) { 
			throw new CoreException.Internal(e);
		}
		return null;
	}
	
	/**
	 * Without severity
	 */
	protected ServerError(int code, String format) {
		super(code, Severity.ERROR, format);
	}

	/**
	 * With severity
	 */
	protected ServerError(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}
