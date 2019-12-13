package com.variant.share.schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Representation of the <code>/states[]</code> array element.
 * 
 * @since 0.5
 */
public interface State {

	/**
	 * The containing variation schema.
	 * 
	 * @return An object of type {@link Schema}. Cannot be null.
	 * @since 0.6
	 */
	Schema getSchema();
	
	/**
	 * This state's name, as provided by the <code>/states[]/name</code> element.
	 * 
	 * @return The name of this state. Cannot be null.
	 * @since 0.5
	 */
	String getName();	
	
	/**
	 * Sate parameters defined by this state, as provided by the <code>/states[]/parameters</code> element.
	 * 
	 * @return An {@link Optional}, containing immutable map of state parameters,
	 *         or empty if the </code>/states[]/parameters</code> element was omitted.
	 * @since 0.6
	 */
	Optional<Map<String, String>> getParameters();

	/**
	 * List of state-scoped lifecycle hooks, as provided by the <code>/states[]/hooks</code> element. 
	 * 
	 * @return An {@link Optional}, containing immutable list of {@link StateScopedHook} objects in the order they were defined,
	 *         or empty if the </code>/states[]/hooks</code> element was omitted.
	 * @since 0.8
	 */
	Optional<List<StateScopedHook>> getHooks();

	/**
	 * A list of variations which instrument this state. Includes both online and offline variations.
	 *  
	 * @return An immutable list of {@link Variation} objects. Cannot be null but may be empty.
	 * @since 0.5
	 */
	List<Variation> getInstrumentedVariations();

	/**
	 * Is this state instrumented by a given variation. In other words, is a given variation contained 
	 * in the list, returned by {@link #getInstrumentedVariants()}?
	 * 
	 * @param variation The variation of interest.
	 * @return <code>true</code> if this state is instrumented by the given variation, or <code>false</code> otherwise. 
	 * @since 0.5
	 */
	boolean isInstrumentedBy(Variation variation);
	
}
