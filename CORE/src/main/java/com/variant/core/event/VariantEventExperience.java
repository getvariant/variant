package com.variant.core.event;

import java.util.Collection;
import java.util.Set;

import com.variant.core.schema.Test.Experience;

/**
 * This object encapsulates state pertinent to both a variant event and a test experience 
 * which was in effect at the time of generation of the event. This state is flushed by an 
 * implementation of {@link com.variant.core.event.EventPersister} at the time when the
 * related event is flushed.
 * 
 * @author Igor Urisman.
 * @since 0.5
 */
public interface VariantEventExperience {
	
	/**
	 * Unique event ID, as generated at the time of persistence by {@link com.variant.core.event.EventPersister#persist(Collection)}.
	 * 
	 * @return Event ID or null if not yet set.
	 * @since 0.5
	 */
	public Long getId();

	/**
	 * The associated event.
	 * 
	 * @return An object of type {@link com.variant.core.event.VariantEvent}
     * @since 0.5
	 */
	public VariantEvent getEvent();
		
	/**
	 * The associated test experience.
	 * 
	 * @return An object of type {@link com.variant.core.schema.Test.Experience}.
     * @since 0.5
	 */
	public Experience getExperience();
	
	/**
	 * Add a custom parameter as a key-value pair. Returns the old value associated with this key if any.
	 * 
	 * @param key A {@link java.util.String} key.
	 * @param value An arbitrary Object associated with this key. At the time of persistance, 
	 *              {@link com.variant.core.event.EventPersister#persist(Collection)} will call the
	 *              {@link java.lang.Object#toString()} method on this object.
	 * @return
	 * @since 0.5
	 */
	public Object setParameter(String key, Object value);	

	/**
	 * Get value associated with this parameter.
	 * 
	 * @param key A {@link java.util.String} key.
	 * @return The Object currently associated with this parameter or null if none.
	 * @since 0.5
	 */
	public Object getParameter(String key);
	
	/**
	 * Get all parameters' keys.
	 * 
	 * @return A {@link java.util.Set} collection of all parameter keys.
	 * @since 0.5
	 */
	public Set<String> getParameterKeys();

}
