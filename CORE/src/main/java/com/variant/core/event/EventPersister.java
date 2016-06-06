package com.variant.core.event;

import java.util.Collection;

import com.variant.core.InitializationParams;

/**
 * <p>Container-instantiated implementation will use external mechanisms to persist Variant events.
 * An implementation must provide a no argument constructor, which will be used for instantiation. 
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface EventPersister {

	/**
	 * <p>The container will call this method immediately following the instantiation (via a no-argument
	 * constructor) to allow for state management.
	 * 
	 * @param coreApi The instance of Variant API that is initializing this object.
	 * @param initParams 
	 * 
	 * @since 0.5
	 */
	public void initialized(InitializationParams initParams) throws Exception;
	
	/**
	 * <p>Persist events to some storage mechanism.
	 * 
	 * @param events A collection of decorated variant events to be persisted.
	 * 
	 * @see EventPersisterH2, EventPersisterPostgres
	 * @since 0.5
	 */
	public void persist(Collection<PersistableVariantEvent> events) throws Exception;

}
