package com.variant.core.schema;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Representation of the <code>/states[]</code> array element.
 * 
 * @since 0.5
 */
public interface Variation {

	/**
	 * The containing variation schema.
	 * 
	 * @return An object of type {@link Schema}. Cannot be null.
	 * @since 0.6
	 */
	Schema getSchema();

	/**
	 * This variation's name, as provided by the <code>/variations[]/name</code> element.
	 * 
	 * @return The name of this variation. Cannot be null.
	 * @since 0.5
	 */
	String getName();	

	/**
	 * This variation's experiences, as provided by the <code>/variations[]/experiences</code> element. 
	 * Each experience represents an alternate code path. 
	 * Exactly one variation must be <code>control</code>, which typically represents the existing code path.
	 * 
	 * @return A list of {@link Variation.Experience} objects. Cannot be null.
	 * @since 0.5
	 */
	List<Experience> getExperiences();
	
	/**
	 * Get an experience by its name.
	 * 
	 * @param name Name of the experience of interest..
	 * @return An {@link Optional}, containing the experience with the given name or empty if no such experience in this variation.
	 * @since 0.5
	 */
	Optional<Experience> getExperience(String name);
		
	/**
	 * Get this variation's control experience.
	 * 
	 * @return An {@link Variation.Experience} object. Cannot be null.
	 * @since 0.5
	 */
	Experience getControlExperience();

	/**
	 * Is this variation currently online. 
	 * <p>
	 * Offline variations  are treated specially:
	 * <ol>
     * <li>A session traversing this variation will always see control experience.
     * <li>If the session already has an entry for this variation in its targeting tracker, 
     *     the entry will be ignored, but not discarded. If not, no entries will be added 
     *     to the user’s targeting tracker.
     * <li>No trace events will be logged for this variation.
     * </ol>
	 * @return true if the test is online, false if not.
	 * @since 0.5
	 */
	boolean isOn();
	
	/**
	 * List of variation-scoped lifecycle hooks, as provided by the <code>/variations[]/hooks</code> element. 
	 * 
	 * @return An {@link Optional}, containing immutable list of {@link VariationScopedHook} objects in the order they were defined,
	 *         or empty if no hooks were defined by this Variation.
	 * @since 0.7
	 */
	Optional<List<VariationScopedHook>> getHooks();

	/**
	 * List of this variations's state instrumentations, as provided by the <code>/variations[]/onStates[]</code> array element.
	 * 
	 * @return A list of objects of type {@link OnState}. Cannot be null.
	 * @since 0.5
	 */
	List<OnState> getOnStates();

	/**
	 * A state instrumentation on a given state.
	 * 
	 * @return An {@link Optional}, containing the {@link OnState} instrumentation on the given state, 
	 *         or empty if given state is not instrumented by this variation.
	 * 
	 * @since 0.5
	 */
	Optional<OnState> getOnState(State state);
		
	/**
	 * Get a list of variations conjointly concurrent with this variation, 
	 * as provided by the <code>/variations[]/conjointVariationRefs</code> property.
	 * 
	 * @return An {@link Optional}, containing the list of variations in order they were listed by
	 *         the <code>/variations[]/conjointVariationRefs</code> property, or empty if 
	 *         <code>/variations[]/conjointVariationRefs</code> property was omitted.
	 * 
	 * @since 0.5
	 */
	Optional<List<Variation>> getConjointVariations();
		
	/**
	 * Is this variation serial with a given variation? Two variations are serial when there does not exist a state
	 * instrumented by both. Serial variations can be targeted independently without any additional considerations. 
	 * 
	 * @param other
	 * @return
	 * @since 0.5
	 */
	default boolean isSerialWith(Variation other) {
		return !isConcurrentWith(other);
	}

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
		Variation getVariation();
		
		/**
		 * Is this the control experience?
		 * 
		 * @return True if this experience is control, false otherwise.
	     * @since 0.5
		 */
		boolean isControl();
		
		/**
		 * This experience's probabilistic weight. These are used by Variant's default
		 * targeting algorithm, if no custom tergeting hooks are configured at run time.
		 * 
		 * @return An {@link Optional}, containing the probabilistic weight defined for this experience, 
	     *         or empty if not defined.
         *
	     * @since 0.5
		 */
		Optional<Number> getWeight();
		
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
		 * A set of all state variants for this instrumentation, both explicitly defined and inferred. 
		 * 
		 * @return A set of objects of type {@link StateVariant}.
	     * @since 0.5
		 */
		Set<StateVariant> getVariants();

		/**
		 * The state variant by a set of experiences. At least one experience must be specified.
		 * It is caller's responsibility to ensure that passed experience list is consistent in that
		 * a) all variations must be instrumented on this state, and b) no two experiences are from
		 * the same variation.
		 * 
		 * @return The {@link Optional} container, holding the state variant given by the set
		 *         of experiences if exists, or empty if no state variant corresponds to the 
		 *         given set of experiences.
		 *
	     * @since 0.9
		 */
		Optional<StateVariant> getVariant(Experience exp1st, Experience...exprest);

	}
}
