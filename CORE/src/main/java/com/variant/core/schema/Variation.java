package com.variant.core.schema;

import java.util.List;
import java.util.Optional;

/**
 * Representation of the <code>variation</code> schema property.
 * 
 * @since 0.5
 */
public interface Variation {

	/**
	 * The schema, containing this variation.
	 * 
	 * @return An object of type {@link Schema}
	 * @since 0.6
	 */
	Schema getSchema();

	/**
	 * The name of this variation.
	 * 
	 * @return The name of this variation.
	 * @since 0.5
	 */
	String getName();	

	/**
	 * A list, in ordinal order, of all of this variation's experiences.
	 * 
	 * @return A list of {@link Variation.Experience} objects.
	 * @since 0.5
	 */
	List<Experience> getExperiences();
	
	/**
	 * Get an experience by its name. Names are case sensitive.
	 * 
	 * @param name The name of the experience.
	 * @return An {@link Optional}, containing the experience with the given name or empty if no such experience in this variation.
	 * @since 0.5
	 */
	Optional<Experience> getExperience(String name);
		
	/**
	 * Get this variation's control experience.
	 * 
	 * @return An {@link Variation.Experience} object.
	 * @since 0.5
	 */
	Experience getControlExperience();

	/**
	 * <p>Is this test currently on? Tests that are not on are treated specially:
	 * <ol>
     * <li>A user traversing this test will always see control experience.
     * <li>If a user already has an entry for this test in his targeting tracker, it will be ignored, 
     *     but not discarded. If not, no entries will be added to the user’s targeting tracker.
     * <li>No trace events will be logged for this test.
     * </ol>
	 * @return true if the test is on, false if not.
	 * @since 0.5
	 */
	boolean isOn();
	
	/**
	 * <p>List of life-cycle hooks, defined in the scope of this variation.
	 * 
	 * @return A list of {@link Hook} object in the ordinal order.
	 * @since 0.7
	 */
	List<Hook> getHooks();

	/**
	 * List, in ordinal order, of all of this variations's state instrumentations.
	 * 
	 * @return A list of objects of type {@link OnState}.
	 * @since 0.5
	 */
	List<OnState> getOnStates();

	/**
	 * This variation's instrumentation details on a particular state.
	 * 
	 * @return An object of type {@link OnState}, if given state instrumented by this variation, 
	 * or <code>null</code> otherwise.
	 * @since 0.5
	 */
	OnState getOnState(State state);
		
	/**
	 * Get a list of variations conjointly concurrent with this instrumentation.
	 * 
	 * @return A list, in ordinal order, of variations listed on the <code>conjointVariationRefs</code> property.
	 * 
	 * @since 0.5
	 */
	List<Variation> getConjointTests();
		
	/**
	 * Is this variation serial with a given variation? Two variations are serial when there does not exist a state
	 * instrumented by both. Serial variations can be targeted independently without any additional considerations. 
	 * 
	 * @param other
	 * @return
	 * @since 0.5
	 */
	boolean isSerialWith(Variation other);

	/**
	 * Is this variation concurrent with a given variation? This is equivalent to
	 * {@code !isSerialWith(other)}. Concurrent variations cannot be targeted independently,
	 * unless they are declared to be conjointly concurrent and the host application has
	 * provided hybrid state variants.
	 * 
	 * @param other
	 * @return
	 * @since 0.5
	 */
	boolean isConcurrentWith(Variation other);

	/**
	 * Is this variation conjointly concurrent with the a given variation? 
	 * Concurrent variations cannot be targeted independently,
	 * unless they are declared to be conjointly concurrent and the host application has
	 * provided hybrid state variants.
	 * 
	 * @param other
	 * @return
	 * @since 0.5
	 */
	boolean isConjointWith(Variation other);

	/**
	 * Representation of a variation experience.
	 * 
	 * @since 0.5
	 */
	interface Experience {

		/**
		 * The name of the experience.
		 * 
		 * @return The name of the experience.
	     * @since 0.5
		 */
		String getName();
		
		/**
		 * The variation to which this experience belongs.
		 * 
		 * @return An object of type {@link Variation}
	     * @since 0.5
		 */
		Variation getTest();
		
		/**
		 * Is this the control experience?
		 * 
		 * @return True if this experience is control, false otherwise.
	     * @since 0.5
		 */
		boolean isControl();
		
		/**
		 * This experience's probabilistic weight. These are used by Variant's default
		 * test targeter if no custom targeting was provided.
		 * 
		 * @return Probabilistic weight, if declared, null otherwise.
	     * @since 0.5
		 */
		Number getWeight();
		
		/**
		 * Is a given state phantom in this experience?
		 * When a state variant is declared phantom, the host application must provide that no session,
		 * targeted for this experience will ever request this state. See documentation for more on <i>mixed instrumentation</i>.
		 * 
		 * @return true if this state variant is declared phantom, or false otherwise.
		 * @since 0.6
		 *
		*/
		boolean isPhantom(State state);
		
	}

	/**
	 * Representation of a variation instrumentation on a particular state.
	 * Corresponds to an element of the <code>variations/onStates</code> schema list.
	 * 
	 * @since 0.5
	 *
	 */
	static interface OnState {
		
		/**
		 * The state this instrumentation is for.
		 * 
		 * @return An object of type {@link State}
	     * @since 0.5
		 */
		State getState();
		
		/**
		 * The variation to which this instrumentation belongs.
		 * 
		 * @return An object of type {@link Variation}
		 * @since 0.5
		 */
		Variation getVariation();
		
		/**
		 * Is this instrumentation non-variant?
		 * 
		 * @return True if this instrumentation is non-variant, false otherwise.
	     * @since 0.5
		 */
		boolean isNonvariant();
		
		/**
		 * A list of all state variants for this instrumentation. 
		 * @return A list of objects of type {@link StateVariant}.
	     * @since 0.5
		 */
		List<StateVariant> getVariants();
		
	}
}
