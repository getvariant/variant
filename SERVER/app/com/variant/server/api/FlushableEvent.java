package com.variant.server.api;

import java.util.Set;

import com.variant.core.TraceEvent;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.CoreSession;

/**
 * A enriched Variant trace event that can be flushed by {@link EventFlusher}. Instantiated by Variant server
 * and passed to an externally configured implementation of {@link EventFlusher}.
 * Extends {@link TraceEvent} with methods exposing runtime details of the underlying {@code TraceEvent}.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
public interface FlushableEvent extends TraceEvent {

	
	/**
	 * The current Variant session.
	 * 
	 * @return An object of type {@link VariantCoreSession}.
	 * 
	 * @since 0.7
	 */
	public CoreSession getSession();
	
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
