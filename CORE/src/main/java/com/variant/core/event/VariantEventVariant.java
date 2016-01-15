package com.variant.core.event;

import java.util.Collection;

import com.variant.core.schema.Test.Experience;

/**
 * This object encapsulates state pertinent to both a variant event and a test experience 
 * which was in effect at the time of generation of the event. This state is flushed by an 
 * implementation of {@link com.variant.core.event.EventPersister} at the time when the
 * related event is flushed.
 * 
 * @author Igor Urisman.
 * @since 0.5
 */
public interface VariantEventVariant {
	
	/**
	 * Unique event ID, as generated at the time of persistence by {@link com.variant.core.event.EventPersister#persist(Collection)}.
	 * 
	 * @return Event ID or null if not yet set.
	 * @since 0.5
	 */
	public Long getId();

	/**
	 * The associated event.
	 * 
	 * @return An object of type {@link com.variant.core.event.VariantEvent}
     * @since 0.5
	 */
	public VariantEvent getEvent();
		
	/**
	 * The associated test experience.
	 * 
	 * @return An object of type {@link com.variant.core.schema.Test.Experience}.
     * @since 0.5
	 */
	public Experience getExperience();

	/**
	 * Is the associated test experience a control experience for its test?
	 * 
	 * @return true if the experience returned by {@link #getExperience()} is control,
	 *         false otherwise.
     * @since 0.5
	 */
	public boolean isExperienceControl();
	
	/**
	 * Is the state associated with this event nonvaraint in the test associated with this experience?
	 * 
	 * @return true if the instrumentation of the test returned by {@link #getExperience()}.getTest()
	 *         is nonvariant on the state for returned by {@link #getEvent()}.getState(), false otherwise.
     * @since 0.5
	 */
	public boolean isStateNonvariant();

}
