package com.variant.client.impl;

import com.variant.core.exception.CommonError;

/**
 * Server originated errors.
 * FATAL - Server is unusable. Application restart required, after the problem is fixed.
 * ERROR - Server is unusable. Application restart not required, after the problem is fixed.
 * WARN  - Server is usable, but
 * INFO  - Server is usable.
 */
public class ClientError extends CommonError {

	//
	// 301-400 Client Local errors.
	//
	
	public static final ClientError BAD_CONN_URL =
			new ClientError(301, Severity.ERROR, "Invalid connection URL [%s]");

	public static final ClientError TARGETING_TRACKER_NO_INTERFACE =
			new ClientError(302, Severity.FATAL, "Targeting tracker class [%s] must implement interface [%s]");
	
	public static final ClientError SESSION_ID_TRACKER_NO_INTERFACE =
			new ClientError(303, Severity.FATAL, "Session ID tracker class [%s] must implement interface [%s]");

	public static final ClientError CONNECTION_CLOSED =
			new ClientError(304, Severity.ERROR, "This connection has been closed");

	public static final ClientError ACTIVE_REQUEST =
			new ClientError(305, Severity.ERROR, "Commit current state request first");

   /**
    * 
    */
   private ClientError(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}
