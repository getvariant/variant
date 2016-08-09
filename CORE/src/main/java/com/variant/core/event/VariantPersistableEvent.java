package com.variant.core.event;

import java.util.Collection;

import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * Decorated Variant event. Instantiated by the Variant Core library
 * in order to enrich client-supplied {@link VariantEvent} object with
 * extra functionality supplied by Variant.  Client code may need these
 * extra methods if it wants to implement a custom {@link EventPersister}.
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantPersistableEvent extends VariantEvent {

	
	/**
	 * The current variant session.
	 * 
	 * @return An object of type {@link VariantCoreStateRequest}.
	 * 
	 * @since 0.6
	 */
	public VariantCoreSession getSession();
	
	/**
	 * <p>Test experiences that:
	 * <ol>
	 * <li>Were targeted for the session that triggered the event.</li>
	 * <li>Correspond to tests, instrumented on the state corresponding to the state request
	 *    which triggered the event.</li>
	 * <li>Correspond to tests that satisfied qualification hooks, if any, for this session
	 *    which triggered the event.</li>
	 * </ol>
	 * 
	 * @return A collection of objects of type {@link Test.Experience}.
	 * 
	 * @since 0.6
	 */
	public Collection<Experience> getLiveExperiences();

}
