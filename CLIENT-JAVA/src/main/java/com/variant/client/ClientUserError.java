package com.variant.client;

import com.variant.core.CommonError;

/**
 * Variant exceptions, emitted by the client code in response to a user error.
 */
public class ClientUserError extends CommonError {
	
	//
	// 331-400 Client Local user errors.
	//
	
	//public static final ClientUserError BAD_CONN_URL =
	//		new ClientUserError(331, "Invalid connection URL [%s]");

	public static final ClientUserError TARGETING_TRACKER_NO_INTERFACE =
			new ClientUserError(332, "Targeting tracker class [%s] must implement interface [%s]");
	
	public static final ClientUserError SESSION_ID_TRACKER_NO_INTERFACE =
			new ClientUserError(333, "Session ID tracker class [%s] must implement interface [%s]");

	public static final ClientUserError CONNECTION_CLOSED =
			new ClientUserError(334, "This connection has been closed");

	public static final ClientUserError SESSION_EXPIRED =
			new ClientUserError(335, "This session has expired");

	public static final ClientUserError ACTIVE_REQUEST =
			new ClientUserError(336, "Commit current state request first");

   /**
    * 
    */
   private ClientUserError(int code, String format) {
		super(code, Severity.ERROR, format);
	}

}
