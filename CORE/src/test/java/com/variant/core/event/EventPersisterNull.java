package com.variant.core.event;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantCoreInitParams;
import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantPersistableEvent;

/**
 * Null implementation: all events are discarded.  Useful for testing.
 * 
 * 
 * @author Igor Urisman
 *
 */
public class EventPersisterNull implements EventPersister {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventPersisterNull.class);
	
	@Override
	public void initialized(VariantCoreInitParams initParams) {}
	
	@Override
	public void persist(Collection<VariantPersistableEvent> events) throws Exception {
			LOG.debug(String.format("Discarded %s events.", events.size()));
	}


}
