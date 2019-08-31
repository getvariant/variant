package com.variant.client.impl;

import com.variant.client.SessionIdTracker;
import com.variant.client.TargetingTracker;
import com.variant.core.error.UserError;
import com.variant.core.error.ServerError;

/**
 * <p>User error emitted by the client and originating on the client. 
 * 
 * @see RuntimeError
 * @see ServerError
 * 
 * @author Igor Urisman
 * @since 0.7
 */
public class ClientUserError extends UserError {
	
	//
	// 271-300 Client Local user errors.
	//
	
	public static final ClientUserError TARGETING_TRACKER_NO_INTERFACE =
			new ClientUserError(271, "Targeting tracker class [%s] must implement interface [" + TargetingTracker.class.getName() + "]");
	
	public static final ClientUserError TARGETING_TRACKER_MISSING =
			new ClientUserError(272, "Targeting tracker class must be specified (use 'withTargetingTracker()' method)");

	public static final ClientUserError SESSION_ID_TRACKER_NO_INTERFACE =
			new ClientUserError(273, "Session ID tracker class [%s] must implement interface [" + SessionIdTracker.class.getName() +"]");

	public static final ClientUserError SESSION_ID_TRACKER_MISSING =
			new ClientUserError(274, "Session ID tracker class must be specified (use 'VariantClient.Builder.withSessionIdTracker()' method)");

   public static final ClientUserError SESSION_ID_TRCKER_INSTANTIATION_ERROR =
         new ClientUserError(275, "Unable to instantiate session ID tracker class [%s] due to error [%s]");

   public static final ClientUserError MALFORMED_VARIANT_URI =
			new ClientUserError(276, "Malformed Variant URI [%s]");

	public static final ClientUserError CANNOT_TRIGGER_SVE =
			new ClientUserError(277, "State visited event cannot be triggered");

	public static final ClientUserError PARAM_CANNOT_BE_NULL =
			new ClientUserError(278, "Method parameter [%s] cannot be null");

	/*
	public static final ClientUserError LIFECYCLE_LISTENER_EXCEPTION =
			new ClientUserError(???, "Unhandled exception [%s] in lifecycle listener class [%s]");
   */
	
	public static final ClientUserError SERVER_CONNECTION_TIMEOUT =
			new ClientUserError(280, "Unable to connect to Variant server at [%s]");

   /**
    * 
    */
   private ClientUserError(int code, String format) {
		super(code, Severity.ERROR, format);
	}

}
