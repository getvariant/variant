package com.variant.core.util;

/**
 * Shared constants.
 */
public interface Constants {

	// Although all our bodies are formatted as JSON, we can't use "application/json"
	// because Play hiccups reading empty bodies if they are expected to be JSON.
	final public static String HTTP_HEADER_CONTENT_TYPE = "text/plain; charset=utf-8";
	
	final public static String HTTP_HEADER_CONNID = "X-Connection-ID";

}
