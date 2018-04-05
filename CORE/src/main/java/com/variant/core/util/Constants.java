package com.variant.core.util;

/**
 * Shared constants.
 */
public interface Constants {

	// Although all our bodies are formatted as JSON, we can't use "application/json"
	// because Play hiccups reading empty bodies if they are expected to be JSON.
	final public static String HTTP_HEADER_CONTENT_TYPE = "text/plain; charset=utf-8";
	
	// Client sends connection ID in this header in all calls except initial connection.
	final public static String HTTP_HEADER_CONNID = "X-Connection-ID";
	
	// Server responds with the status of the connection with each response.
	// Note that some client calls on a closed connection will result in explicit exception
	// while some will succeed, but 
	// final public static String HTTP_HEADER_CONN_STATUS = "X-Connection-Status";

}
