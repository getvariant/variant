package com.variant.server.api;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link EventFlusher}, which discards Variant events.
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
