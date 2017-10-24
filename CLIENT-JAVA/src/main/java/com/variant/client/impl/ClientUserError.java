package com.variant.client.impl;

import com.variant.core.RuntimeError;
import com.variant.core.ServerError;

/**
 * <p>User error emitted by the client and originating on the client. 
 * 
 * @see RuntimeError
 * @see ServerError
 * 
 * @author Igor Urisman
 * @since 0.7
 */
public class ClientUserError extends RuntimeError {
	
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
