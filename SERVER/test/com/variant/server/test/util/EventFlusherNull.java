package com.variant.server.test.util;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigObject;
import com.variant.server.api.EventFlusher;
import com.variant.server.api.FlushableEvent;

/**
 * Null implementation: all events are discarded.  Useful for testing.
 * 
 * 
 * @author Igor
 *
 */
class EventFlusherNull implements EventFlusher {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventFlusherNull.class);
	
	@Override
	public void init(ConfigObject config) {}
	
	@Override
	public void flush(Collection<FlushableEvent> events) throws Exception {
			LOG.debug(String.format("Discarded %s events.", events.size()));
	}

}
