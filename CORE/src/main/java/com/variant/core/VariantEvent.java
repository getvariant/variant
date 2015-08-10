package com.variant.core;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * EVENTS DAO.
 * 
 * @author Igor.
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
	 * Event's completion status.
	 * @return
	 */
	public Status getStatus();

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

	/**
	 * 
	 */
	public static enum Status {
		SUCCESS, 
		EXCEPTION
	}

}
