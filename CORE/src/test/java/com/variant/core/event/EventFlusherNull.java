package com.variant.core.event;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantCoreInitParams;
import com.variant.server.event.EventFlusher;
import com.variant.server.event.VariantFlushableEvent;

/**
 * Null implementation: all events are discarded.  Useful for testing.
 * 
 * 
 * @author Igor Urisman
 *
 */
public class EventFlusherNull implements EventFlusher {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventFlusherNull.class);
	
	@Override
	public void init(VariantCoreInitParams initParams) {}
	
	@Override
	public void flush(Collection<VariantFlushableEvent> events) throws Exception {
			LOG.debug(String.format("Discarded %s events.", events.size()));
	}


}
