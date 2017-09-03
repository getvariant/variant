package com.variant.server.test.util;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigObject;
import com.variant.core.EventFlusher;
import com.variant.core.FlushableEvent;

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
