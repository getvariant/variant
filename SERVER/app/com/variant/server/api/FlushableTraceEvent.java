package com.variant.server.api;

import java.util.Set;

import com.variant.core.TraceEvent;
import com.variant.core.schema.Test.Experience;

/**
 * A enriched Variant trace event that can be flushed by {@link EventFlusher}. Instantiated by Variant server
 * and passed to an externally configured implementation of {@link EventFlusher}.
 * Extends {@link TraceEvent} with methods exposing runtime details of the underlying {@code TraceEvent}.
 * 
 * @since 0.7
 */
public interface FlushableTraceEvent extends TraceEvent {

	
	/**
	 * The Variant session which created this trace event.
	 * 
	 * @return An object of type {@link Session}.
	 * 
	 * @since 0.7
	 */
	public Session getSession();
	
	/**
	 * Live experiences in effect at the time this event was generated.
	 * 
	 * @return A set of objects of type {@link Experience}.
	 * 
	 * @see VariantCoreStateRequest#getLiveExperiences()
	 * @since 0.7
	 */
	public Set<Experience> getLiveExperiences();

}
