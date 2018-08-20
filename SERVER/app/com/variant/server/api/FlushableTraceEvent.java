package com.variant.server.api;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.variant.core.schema.Test.Experience;

/**
 * A enriched Variant trace event that can be flushed by an event flusher. 
 * Instantiated by Variant server and passed to an externally configured implementation of {@link EventFlusher}.
 * Extends {@link TraceEvent} with methods exposing runtime details about the triggering session.
 * 
 * @since 0.7
 */
public interface FlushableTraceEvent {

	/**
	 * The name of the event.
	 *
	 * @since 0.7
	 */
	public String getName();

	/**
	 * Event creation timestamp.
	 * 
	 * @since 0.7
	 */
	public Date getCreateDate();
	
	/**
	 * A read-only map of event attributes
	 * @return
	 */
	public Map<String, String> getAttributes();
	
	/**
	 * The Variant session which triggered this trace event.
	 * 
	 * @return An object of type {@link Session}.
	 * 
	 * @since 0.7
	 */
	public Session getSession();
	
	/**
	 * Live experiences in effect at the time this event was triggered.
	 * 
	 * @return A set of objects of type {@link Experience}.
	 * 
	 * @see VariantCoreStateRequest#getLiveExperiences()
	 * @since 0.7
	 */
	public Set<Experience> getLiveExperiences();

}
