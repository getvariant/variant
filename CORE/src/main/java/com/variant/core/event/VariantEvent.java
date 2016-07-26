package com.variant.core.event;

import java.util.Date;
import java.util.Map;

import com.variant.core.VariantStateRequest;
import com.variant.core.schema.Test.OnState.Variant;

/**
 * A Variant event. Events are generated either implicitly by the Variant RCE Container, 
 * or explicitly by the client code. Events are flushed to external storage once per state
 * request, during the {@link Variant#commitStateRequest(com.variant.core.VariantStateRequest, Object...)}
 * method by an implementation of {@link com.variant.core.event.EventPersister}. Client code
 * may generate its own events by passing its own implementations to 
 * {@link VariantStateRequest#triggerEvent(VariantEvent)}.
 * 
 * @author Igor Urisman.
 * @see VariantStateRequest#triggerEvent(VariantEvent)
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
