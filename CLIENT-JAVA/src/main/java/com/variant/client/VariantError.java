package com.variant.client;

import com.variant.core.error.UserError;

/**
 * <p>User errors, emitted by Variant client. 
 * Adapters, wrapping variant client, should extend this class with their own errors in the reserved range 301-400, inclusive.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
public class VariantError extends UserError {
	
   /**
    * 
    */
   protected VariantError(int code, String format) {
      super(code, Severity.ERROR, format);
   }

	//
	// 271-300 Client Local user errors.
	//
	
	public static final VariantError TARGETING_TRACKER_NO_INTERFACE =
			new VariantError(271, "Targeting tracker class [%s] must implement interface [" + TargetingTracker.class.getName() + "]");
	
	public static final VariantError TARGETING_TRACKER_MISSING =
			new VariantError(272, "Targeting tracker class must be specified (use 'withTargetingTracker()' method)");

	public static final VariantError SESSION_ID_TRACKER_NO_INTERFACE =
			new VariantError(273, "Session ID tracker class [%s] must implement interface [" + SessionIdTracker.class.getName() +"]");

	public static final VariantError SESSION_ID_TRACKER_MISSING =
			new VariantError(274, "Session ID tracker class must be specified (use 'VariantClient.Builder.withSessionIdTracker()' method)");

   public static final VariantError SESSION_ID_TRCKER_INSTANTIATION_ERROR =
         new VariantError(275, "Unable to instantiate session ID tracker class [%s] due to error [%s]");

   public static final VariantError MALFORMED_VARIANT_URI =
			new VariantError(276, "Malformed Variant URI [%s]");

	public static final VariantError CANNOT_TRIGGER_SVE =
			new VariantError(277, "State visited event cannot be triggered");

	public static final VariantError PARAM_CANNOT_BE_NULL =
			new VariantError(278, "Method parameter [%s] cannot be null");
	
	public static final VariantError SERVER_CONNECTION_TIMEOUT =
			new VariantError(280, "Unable to connect to Variant server at [%s]");

}
