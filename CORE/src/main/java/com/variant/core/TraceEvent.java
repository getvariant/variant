package com.variant.core;

import java.util.Date;

/**
 * A Variant trace event that can be triggered. Variant trace events are the elementary data points, 
 * generated by user traffic as it flows through Variant variations with the purpose of subsequent
 * analysis by a downstream process. Trace events can be triggered implicitly, by Variant, 
 * or explicitly by the client code. In either case, the client code can attach attributes to these events, 
 * to aid in the downstream analysis.
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
 */
public interface TraceEvent {

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
	 * Set a trace event attribute.
	 * Local operation. Event attributes are not sent to the server
	 * until they are triggered either explicitly with the <code>Session.triggerTraceEvent()</code>
	 * method, or implicitly when a state request object is committed.
	 * 
	 * @param name Attribute name. Cannot be <code>null</code>.
	 * @param value Attribute value. Cannot be <code>null</code>.
	 * @return The string value previously associated with this <code>name</code>, or <code>null</code> if none.
	 * 
	 * @since 0.9
	 */
	public String setAttribute(String name, String value);
	
	/**
	 * Retrieve the value of an event attribute.
	 * 
	 * @param name Attribute name.
	 * @return The string value associated with this name.
	 * 
	 * @since 0.9
	 */
	public String getAttribute(String name);

	/**
	 * Remove a session attribute.
	 * Local operation. Event attributes are not sent to the server
	 * until they are triggered either explicitly with the <code>Session.triggerTraceEvent()</code>
	 * method, or implicitly when a state request object is committed.
	 * 
	 * @param name Attribute name.
	 * @return The string value previously associated with this <code>name</name>, or <code>null</code> if none.
	 * 
	 * @since 0.9
	 */
	public String clearAttribute(String name);

}
