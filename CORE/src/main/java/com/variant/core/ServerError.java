package com.variant.core;

import java.lang.reflect.Field;

import com.variant.core.util.ReflectUtils;


/**
 * User errors that are emitted by the server to be sent across the network to the client.
 * 
 * @author Igor
 */
public class ServerError extends UserError {
	
	//
	// 601-610 Internal, Payload syntax error
	//
	public static final ServerError InternalError = 
			new ServerError(601, "Internal server error [%s]");
	
	public static final ServerError JsonParseError = 
			new ServerError(602, "JSON parsing error: '%s'");
	
	public static final ServerError BadContentType = 
			new ServerError(603, "Unsupported content type, use 'application/json'");
	   
	//
	// 611-630 Internal, Payload parse error
	//
	public static final ServerError MissingProperty = 
			new ServerError(611, "Missing required property '%s'");
	
	public static final ServerError InvalidDate = 
			new ServerError(612, "Invalid date specification in property '%s', epoch milliseconds expected");
	
	public static final ServerError UnsupportedProperty = 
			new ServerError(613, "Unsupported property '%s' in payload");
	
	public static final ServerError PropertyNotAString = 
			new ServerError(614, "Property '%s' must be a string");
	
	public static final ServerError EmptyBody = 
			new ServerError(615, "Body expected but was null");
	
	public static final ServerError MissingParamName = 
			new ServerError(616, "Parameter name is missing");

	public static final ServerError InvalidSCID = 
			new ServerError(617, "Invalid SCID [%s]");

//	public static final ServerError StateRequestAlreadyCommitted = 
//			new ServerError(618, "State request already committed");

	//
	// 631-700 Internal, other internal errors.
	//	

	/**
	 */
	public boolean isInternal() {
		return getCode() <= 700;
	}

	//
	// 701-720 User, Connection
	//
	public static final ServerError UnknownSchema = 
			new ServerError(701, "Unknown schema [%s]");
	
	public static final ServerError UnknownConnection = 
			new ServerError(702, "Unknown connection [%s]");
	
	public static final ServerError TooManyConnections = 
			new ServerError(703, "Too many connections");

	//
	// 721-740 User, Session
	//
	public static final ServerError SessionExpired = 
			new ServerError(721, "Requested session ID [%s] has expired");

	//
	// 741-760 User, Event
	//
	public static final ServerError UnknownState = 
			new ServerError(741, "No state request in session. Target this session for a state first");


	
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
	 * Without comment
	 */
	protected ServerError(int code, String format) {
		super(code, Severity.ERROR, format);
	}

}
