package com.variant.server.api;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Null event flusher. All Variant events are discarded. 
 * Useful for toggle experiments when persistence of Variant events is not needed.
 * 
 * 
 * @since 0.8
 *
 */
public class EventFlusherNull implements EventFlusher {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventFlusherNull.class);
		
	@Override
	public void flush(Collection<FlushableEvent> events) throws Exception {
			LOG.debug(String.format("Discarded %s events.", events.size()));
	}

}
