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
	 * <p>Persist events to some storage mechanism.
	 * 
	 * @param events A collection of decorated variant events to be persisted.
	 * 
	 * @see EventPersisterH2, EventPersisterPostgres
	 * @since 0.5
	 */
	public void persist(Collection<VariantEventDecorator> events) throws Exception;
	
}
