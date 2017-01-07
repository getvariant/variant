package com.variant.core.exception;

import com.variant.core.UserError;


/**
 * User errors that are signaled by the server to be sent across the network to the client.
 * 
 * @author Igor
 */
public class ServerError extends UserError {
	
	//
	// 601-610 Internal, Payload syntax error
	//
	//public static final ServerError InternalError = 
	//		new ServerError(601, "Internal server error [%s]");
	
	public static final ServerError JsonParseError = 
			new ServerError(602, "JSON parsing error: '%s'");
	
	public static final ServerError BadContentType = 
			new ServerError(603, "Unsupported content type", "Use 'application/json' or 'text/plain'.");
	   
	//
	// 611-630 Internal, Payload parse error
	//
	public static final ServerError MissingProperty = 
			new ServerError(611, "Missing required property '%s'");
	
	public static final ServerError InvalidDate = 
			new ServerError(612, "Invalid date specification in property '%s'", "Epoch milliseconds expected");
	
	public static final ServerError UnsupportedProperty = 
			new ServerError(613, "Unsupported property '%s' in payload");
	
	public static final ServerError PropertyNotAString = 
			new ServerError(614, "Property '%s' must be a string");
	
	public static final ServerError EmptyBody = 
			new ServerError(615, "Body expected but was null");
	
	public static final ServerError MissingParamName = 
			new ServerError(616, "Parameter name is missing");
	   
	//
	// 631-700 Internal, other internal errors.
	//	

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
	// 721-740 User, Connection
	//
	public static final ServerError SessionExpired = 
			new ServerError(721, "Session expired");

	//
	// 741-760 User, Event
	//
	public static final ServerError UnknownState = 
			new ServerError(741, "No recent state request in session");


	/**
	 * With comment
	 */
	protected ServerError(int code, String format, String comment) {
		super(code, Severity.ERROR, format, comment);
	}

	/**
	 * Without comment
	 */
	protected ServerError(int code, String format) {
		super(code, Severity.ERROR, format);
	}

	public boolean isInternal() {
		return code <= 700;
	}
}
