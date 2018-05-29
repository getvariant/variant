package com.variant.core;

import java.util.Date;
import java.util.Map;

/**
 * A Variant event that can be triggered. Not to be confused with life-cycle events.
 * <p>
 * Variant events are generated either implicitly by 
 * Variant server, or explicitly by the host application. Implicitly generated events
 * are triggered at state request commit time, while custom events can be triggered
 * at will by passing custom implementations of this interface to 
 * {@code com.variant.client.VariantSession.triggerEvent(VariantEvent)}.
 * <p>
 * An event has a name, a value, and, optionally, a set of custom parameters which enrich
 * event's application context, e.g. a user ID. Variant further enriches each event
 * with variation related data, such as user's active experiences. 
 * <p>
 * A good example of a custom event is an application failure event, which your code triggers
 * if an unexpected application failure was detected. This event could be processed downstream 
 * to exclude this session from experiment analysis or to turn off traffic into offending experience.
 * 
 * @since 0.5
 *
 */
public interface VariantEvent {

	/**
	 * The name of the event.
	 *
	 * @since 0.5
	 */
	public String getName();

	/**
	 * The value of the event.
	 * 
	 * @since 0.5
	 */
	public String getValue();	

	/**
	 * Event creation timestamp.
	 * 
	 * @since 0.5
	 */
	public Date getCreateDate();
	
	/**
	 * Event parameters. An event may have any number of custom parameters which are
	 * simple key-value pairs. These will be passed without interpretation to the externally
	 * configured event flusher which is expected to do something meaningful with them.
	 * 
	 * @return A map of event parameters as key/value pairs.
	 * @since 0.5
	 */
	public Map<String, String> getParameterMap();

}
