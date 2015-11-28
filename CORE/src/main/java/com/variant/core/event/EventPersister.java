package com.variant.core.event;

import java.util.Collection;

/**
 * <p>Container-instantiated implementation will use external mechanisms to persist Variant events.
 * An implementation must provide a no argument constructor. Because an implementation
 * must be instantiated by the container, it must provide a no-argument constructor.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface EventPersister {

	/**
	 * <p>The container will call this method immediately following the instantiation to allow
	 * the client code to initialize the object with some state. 
	 * 
	 * @since 0.5
	 */
	public void initialized();
	
	/**
	 * <p>Persist events to some storage mechanism. This method must generate and initialize
	 * event IDs for all passed events, by calling {@link com.variant.core.event.VariantEvent#setId(long)}.
	 * In the case of a relational database, these will be sequence generated numbers returned by JDBC
	 * after the insert.
	 * 
	 * @param events A collection of {@link com.variant.core.event.VariantEvent} objects to be persisted.
	 * @since 0.5
	 */
	public void persist(Collection<VariantEvent> events) throws Exception;
	
}
