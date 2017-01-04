package com.variant.client.impl;

import com.variant.core.exception.CommonError;

/**
 * Variant exceptions, emitted by the client code in response to a user error.
 */
public class ClientUserError extends CommonError {
	
	//
	// 331-400 Client Local user errors.
	//
	
	public static final ClientUserError BAD_CONN_URL =
			new ClientUserError(331, Severity.ERROR, "Invalid connection URL [%s]");

	public static final ClientUserError TARGETING_TRACKER_NO_INTERFACE =
			new ClientUserError(332, Severity.FATAL, "Targeting tracker class [%s] must implement interface [%s]");
	
	public static final ClientUserError SESSION_ID_TRACKER_NO_INTERFACE =
			new ClientUserError(333, Severity.FATAL, "Session ID tracker class [%s] must implement interface [%s]");

	public static final ClientUserError CONNECTION_CLOSED =
			new ClientUserError(334, Severity.ERROR, "This connection has been closed");

	public static final ClientUserError ACTIVE_REQUEST =
			new ClientUserError(335, Severity.ERROR, "Commit current state request first");

   /**
    * 
    */
   private ClientUserError(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}
