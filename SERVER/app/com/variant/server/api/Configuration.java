package com.variant.server.api;

import java.util.Optional;

/**
 * Runtime server configuration currently in effect.
 * 
 * @since 0.10
 */
public interface Configuration {
 
	String getSchemataDir();

	int getSessionTimeout();

	int getSessionVacuumInterval();

	String getEventFlusherClassName();

	Optional<String> getEventFlusherClassInit();

	int getEventWriterBufferSize();

	int getEventWriterMaxDelay();
	
	String getNetworkPort();
}