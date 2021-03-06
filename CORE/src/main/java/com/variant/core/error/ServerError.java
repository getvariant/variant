package com.variant.core.error;

import java.lang.reflect.Field;

import com.variant.core.util.ReflectUtils;

/**
 * User errors that are emitted by the server to be sent across the network to the client.
 * 
 * @author Igor
 */
public class ServerError extends UserError {
	
	//
	// 601-620 Internal, Payload syntax error
	//
	public static final ServerError InternalError = 
			new ServerError(601, "Unexpected internal server error [%s]");
	
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
			new ServerError(625, "Request body required");
	
	public static final ServerError MissingParamName = 
			new ServerError(626, "Parameter name is missing");

	public static final ServerError InvalidRequestStatus = 
			new ServerError(627, "Invalid request status '%s'");

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
			new ServerError(665, "Targeting hook [%s] for variation [%s] cannot set experience [%s]");

	// 
	// 681-700 User, Server API, Other
	// 
/*	
	public static final ServerError STATE_NOT_INSTRUMENTED_BY_VARIATION =
			new ServerError(681, "State [%s] is not instrumented by variation [%s]");
*/
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

	public static final ServerError CANNOT_COMMIT = 
			new ServerError(705, "Cannot commit a failed state request.");

	public static final ServerError CANNOT_FAIL = 
			new ServerError(706, "Cannot fail a committed state request.");

	public final static ServerError STATE_PHANTOM_IN_EXPERIENCE =
			new ServerError(707, "Cannot target state [%s] because it is phantom in live experience [%s]");

	public static final ServerError EXPERIENCE_WEIGHT_MISSING = 
			new ServerError(708, "No weight specified for variation [%s], experience [%s] and no targeting hooks defined");


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
