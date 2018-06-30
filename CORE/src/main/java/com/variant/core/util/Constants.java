package com.variant.core.util;

/**
 * Shared constants.
 */
public interface Constants {

	// Although all our bodies are formatted as JSON, we can't use "application/json"
	// because Play hiccups reading empty bodies if they are expected to be JSON.
	final public static String HTTP_HEADER_CONTENT_TYPE = "text/plain; charset=utf-8";
	
	// Client sends schema name in the header.
	//final public static String HTTP_HEADER_SCHEMA_NAME = "X-Schema-ID";

/*	
	// Server responds with the status of the connection with each response.
	final public static String HTTP_HEADER_CONN_STATUS = "X-Connection-Status";
*/
}
