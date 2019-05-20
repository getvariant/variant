package com.variant.server.api;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.variant.core.schema.Variation.Experience;

/**
 * An enriched Variant trace event that is passed to a trace event flusher. 
 * Instantiated by Variant server and passed to the implementation of the trace event flusher 
 * in {@link TraceEventFlusher#flush(java.util.Collection)}.
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
	 * The reasonably unique id of this event that is safe to use as unique key by external storage.
	 *
	 * @returns Randomly generated 128-bit number converted to a hexadecimal string.
	 * @since 0.10
	 */
	public String getId();

	/**
	 * Event creation timestamp.
	 * 
	 * @since 0.7
	 */
	public Instant getTimestamp();
	
	/**
	 * A read-only map of event attributes.
     *
	 * @since 0.7
	 */
	public Map<String, String> getAttributes();
	
	/**
	 * The Variant session ID, which triggered this trace event.
     * The entire session object is not available 
     * because of the potential latency between the time a trace event is triggered
     * and the time when user code gets access to this object, the triggering session 
     * may have completely changed or even expired.
	 * 
	 * @return Session ID.
	 * 
	 * @since 0.7
	 */
	public String getSessionId();
	
	/**
	 * Live experiences in effect for the triggering session, at the time this event was triggered.
	 * 
	 * @return A set of objects of type {@link Experience}.
	 * 
	 * @see VariantCoreStateRequest#getLiveExperiences().  Cannot be null, but be empty.
	 * @since 0.7
	 */
	public Set<Experience> getLiveExperiences();

}
