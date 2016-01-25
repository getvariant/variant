package com.variant.core.ext;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.event.EventPersister;
import com.variant.core.event.VariantEvent;
import com.variant.core.schema.Test.Experience;
import com.variant.core.util.Tuples.Pair;

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
	public void initialized() {}

	@Override
	public void persist(Collection<Pair<VariantEvent, Collection<Experience>>> events)
			throws Exception {
		
			LOG.debug("Discarded " + events.size() + " events.");
	}


}
