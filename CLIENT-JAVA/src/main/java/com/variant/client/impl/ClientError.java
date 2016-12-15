package com.variant.client.impl;

import com.variant.core.exception.RuntimeError;

/**
 * Server originated errors.
 * FATAL - Server is unusable. Application restart required, after the problem is fixed.
 * ERROR - Server is unusable. Application restart not required, after the problem is fixed.
 * WARN  - Server is usable, but
 * INFO  - Server is usable.
 */
public class ClientError extends RuntimeError {

	public static final ClientError PROPERTY_BAD_CLASS =
			new ClientError(Severity.ERROR, "Don't know how to convert to class [%s]");

	public static final ClientError TARGETING_TRACKER_NO_INTERFACE =
			new ClientError(Severity.FATAL, "Targeting tracker class [%s] must implement interface [%s]");

	public static final ClientError PROPERTY_INIT_INVALID_JSON =
			new ClientError(Severity.ERROR, "Invalid JSON [%s] in system property [%s]");
	
	public static final ClientError PROPERTY_INIT_PROPERTY_NOT_SET =
			new ClientError(Severity.ERROR, "Init property [%s] is required by class [%s] but is missing in system property [%s]");

	public static final ClientError SESSION_ID_TRACKER_NO_INTERFACE =
			new ClientError(Severity.FATAL, "Session ID tracker class [%s] must implement interface [%s]");

	public static final ClientError CONNECTION_CLOSED =
			new ClientError(Severity.ERROR, "This connection has been closed");
	
   /**
    * 
    */
   private ClientError(Severity severity, String format) {
		super(severity, format);
	}

}
