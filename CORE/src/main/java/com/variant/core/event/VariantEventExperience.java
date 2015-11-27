package com.variant.core.event;

import java.util.Set;

import com.variant.core.schema.Test.Experience;

/**
 * EVENTS_EXPERIENCES DAO.
 * 
 * @author Igor.
 */
public interface VariantEventExperience {
	
	/**
	 * Opaque sequence generated record id.
	 * Not available until persisted in database.
	 * @return
	 */
	public Long getId();

	/**
	 * 
	 * @return
	 */
	public VariantEvent getEvent();
		
	/**
	 * 
	 * @return
	 */
	public Experience getExperience();
	
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
