package com.variant.client.impl;

import com.variant.core.CommonError;

/**
 * Internal variant exceptions, emitted by the client code,
 * i.e. not coming from the server.
 */
public class ClientInternalError extends CommonError {

	//
	// 301-330 Client Local internal errors.
	//
	public static final ClientInternalError INTERNAL_ERROR =
			new ClientInternalError(301, Severity.ERROR, "Internal Variant client error [%s]");

	public static final ClientInternalError INTERNAL_SERVER_ERROR =
			new ClientInternalError(302, Severity.ERROR, "Internal Variant server error [%s] [%s]");

	public static final ClientInternalError NET_PAYLOAD_ELEMENT_MISSING =
			new ClientInternalError(303, Severity.ERROR, "Element [%s] is missing in payload [%s]");

   /**
    * 
    */
   private ClientInternalError(int code, Severity severity, String format) {
		super(code, severity, format);
	}

}
