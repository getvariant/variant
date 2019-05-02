package com.variant.server.api;

import java.util.Map;

/**
 * Effective server configuration.
 */
public interface Configuration {
 
	String getSchemataDir();

	int getSessionTimeout();

	int getVacuumInterval();

	String getEventFlusherClassName();

	String getEventFlusherClassInit();

	int getEventWriterBufferSize();

	int getEventWriterMaxDelay();

	Map<String,Object> asMap();
	
	String getNetworkPort();
}