package com.variant.server.event;

import java.util.Collection;

import com.variant.core.api.VariantEvent;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.CoreSession;

/**
 * Variant event that can be flushed. Instantiated by Variant server
 * and passed to an externally configured implementation of {@link EventFlusher}.
 * Extends {@link VariantEvent} with additional methods required for proper logging,
 * such as caller's current Variant session.
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public interface FlushableEvent extends VariantEvent {

	
	/**
	 * The current Variant session.
	 * 
	 * @return An object of type {@link VariantCoreSession}.
	 * 
	 * @since 0.6
	 */
	public CoreSession getSession();
	
	/**
	 * Current live experiences.
	 * 
	 * @return A collection of objects of type {@link Experience}.
	 * @see VariantCoreStateRequest#getLiveExperiences()
	 * @since 0.6
	 */
	public Collection<Experience> getLiveExperiences();

}
