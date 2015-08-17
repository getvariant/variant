package com.variant.core.event;

import java.util.Collection;

/**
 * 
 * @author Igor
 *
 */
public interface EventPersister {

	/**
	 * Will be called by the Variant container upon initialization.
	 * Client code may use this for further initialization.
	 * @param args - strings passed to the configuration.
	 */
	public void initialized();
	
	/**
	 * Persist a bunch of events somewhere.
	 */
	public void persist(Collection<VariantEventSupport> events) throws Exception;
	
}
