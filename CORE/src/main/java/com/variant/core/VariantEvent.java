package com.variant.core;

import java.util.Date;
import java.util.Map;

/**
 * A Variant event that can be triggered. Events are generated either implicitly by 
 * Variant server, or explicitly by the host application. Implicitly generated events
 * are triggered at state request commit time, while custom events can be triggered
 * at will by passing custom implementations of this interface to 
 * {@link com.variant.client.VariantSession#triggerEvent(VariantEvent)}.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface VariantEvent {
				
	/**
	 * The name of the event, such as "STATE_VISIT".
	 * @return Event name.
	 * @since 0.5
	 */
	public String getEventName();

	/**
	 * The value of the event, such as "Login Page".
	 * 
	 * @return Event value.
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
	 * Custom parameters. An event may have any number of custom parameters which are
	 * simple key-value pairs. These will be passed without interpretation to the externally
	 * configured event flusher which is expected to do something meaningful with them.
	 * 
	 * @return Custom parameters. Will be passed to the implementation of {@link EventFlusher}.
	 * @since 0.5
	 */
	public Map<String, String> getParameterMap();

}
