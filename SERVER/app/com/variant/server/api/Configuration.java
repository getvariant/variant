package com.variant.server.api;

import java.util.Map;
import java.util.Optional;

/**
 * Effective server configuration.
 */
public interface Configuration {
 
	String getSchemataDir();

	int getSessionTimeout();

	int getVacuumInterval();

	String getEventFlusherClassName();

	Optional<String> getEventFlusherClassInit();

	int getEventWriterBufferSize();

	int getEventWriterMaxDelay();

	Map<String,Object> asMap();
	
	String getNetworkPort();
}