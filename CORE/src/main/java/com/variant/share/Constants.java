package com.variant.share;

/**
 * Shared constants.
 */
public interface Constants {

	// The name of State Visited Event.
	final public static String SVE_NAME = "$STATE_VISIT";
	
	// Although all our bodies are formatted as JSON, we can't use "application/json"
	// because Play hiccups reading empty bodies if they are expected to be JSON.
	final public static String HTTP_HEADER_CONTENT_TYPE = "text/plain; charset=utf-8";
	
	// Server-side timing is returned in this response header.
	final public static String HTTP_HEADER_SERVER_TIMIER = "Variant-Timer";
	
	// Customer logger's date format
   final static String LOGGER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";

}
