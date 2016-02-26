package com.variant.core.ext;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.InitializationParams;
import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantEventDecorator;

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
	public void initialized(InitializationParams initParams) {}

	@Override
	public void persist(Collection<VariantEventDecorator> events) throws Exception {
		
			LOG.debug("Discarded " + events.size() + " events.");
	}


}
