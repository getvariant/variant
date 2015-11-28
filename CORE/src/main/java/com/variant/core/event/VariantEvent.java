package com.variant.core.event;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import com.variant.core.VariantSession;

/**
 * Type representing a Variant event. Events are generated either implicitly
 * by the Variant RCE Container, or explicitly by the client code as the user
 * session traverses experiment(s). All pending events are flushed by an 
 * 
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface VariantEvent {
			
	/**
	 * Opaque sequence generated record id.
	 * Not available until persisted in database.
	 * @return
	 */
	public Long getId();
	
	/**
	 * Container calls this to set the ID at the time of persistence.
	 * 
	 * @return
	 */
	public void setId(long id);

	/**
	 * Event's name.
	 * @return
	 */
	public String getEventName();

	/**
	 * Event's value;
	 * @return
	 */
	public String getEventValue();

	/**
	 * VariantSession that created this event.
	 * @return
	 */
	public VariantSession getSession();

	/**
	 * Timestamp created.
	 * @return
	 */
	public Date getCreateDate();
	
	/**
	 * Subclasses must provide a way to get to event-experiences.
	 * @return
	 */
	public Collection<VariantEventExperience> getEventExperiences();

	/**
	 * Add a custom parameter as a key-value pair. Returns the old value associated with this key
	 * if any.  See Map.put().
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object setParameter(String key, Object value);	

	/**
	 * Get value associated with this param.  See Map.get().
	 * @param key
	 * @return
	 */
	public Object getParameter(String key);
	
	/**
	 * Get all parameters' keys.
	 * @return
	 */
	public Set<String> getParameterKeys();

}
