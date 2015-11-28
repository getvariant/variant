package com.variant.core.event;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import com.variant.core.VariantSession;

/**
 * A Variant event. Events are generated either implicitly by the Variant RCE Container, 
 * or explicitly by the client code. All pending events are flushed by an implementation
 * of {@link com.variant.core.event.EventPersister}. Contains state pertinent to an event.
 * 
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface VariantEvent {
			
	/**
	 * Unique event ID, as generated at the time of persistence by {@link com.variant.core.event.EventPersister#persist(Collection)}.
	 * 
	 * @return Event ID or null if not yet set.
	 * @since 0.5
	 */
	public Long getId();
	
	/**
	 * An implementation of {@link com.variant.core.event.EventPersister#persist(Collection)} must call this
	 * to set the event ID.
	 * 
	 * @since 0.5
	 */
	public void setId(long id);

	/**
	 * Event's name, as assigned by the implementation.
	 * 
	 * @return Event's name.
	 * @since 0.5
	 */
	public String getEventName();

	/**
	 * Event's value, as assigned by the implementation.
	 * 
	 * @return Event's value.
	 * @since 0.5
	 */
	public String getEventValue();

	/**
	 * Variant session that created this event.
	 * 
	 * @return An object of type {@link com.variant.core.VariantSession}.
	 * @since 0.5
	 */
	public VariantSession getSession();

	/**
	 * Create timestamp.
	 * 
	 * @return Create timestamp.
	 * @since 0.5
	 */
	public Date getCreateDate();
	
	/**
	 * An implementation must also implement {@link com.variant.core.event.VariantEventExperience}.
	 * 
	 * @see {@link com.variant.core.event.VariantEventExperience}
	 * @return A collection of objects of type {@link com.variant.core.event.VariantEventExperience},
	 *         which contain state specific to this event and a particular test experience in effect at
	 *         the time of generation of this event.
	 * @since 0.5
	 *      
	 */
	public Collection<VariantEventExperience> getEventExperiences();

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
