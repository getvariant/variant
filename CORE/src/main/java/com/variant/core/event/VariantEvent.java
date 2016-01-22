package com.variant.core.event;

import java.util.Date;
import java.util.Map;

import com.variant.core.VariantSession;

/**
 * A Variant event. Events are generated either implicitly by the Variant RCE Container, 
 * or explicitly by the client code. All pending events are flushed by an implementation
 * of {@link com.variant.core.event.EventPersister}. Contains state pertinent to an event.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface VariantEvent {
				
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
	 * Custom parameters.
	 * 
	 * @return Custom parameters the implementor wishes to be logged alongside with this
	 *         event.  It is up to the {@link EventPersister} in effect how these parameters
	 *         will be recorded.
	 * @since 0.5
	 */
	public Map<String,Object> getParameterMap();

}
