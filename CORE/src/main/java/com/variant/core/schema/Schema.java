package com.variant.core.schema;

import java.util.List;
import java.util.Optional;


/**
 * Representation of the root schema element. 
 * <p>
 * Each variation schema describes a set of related code variations. It has three top level entities: 
 * {@link Meta}, a list of {@link State}s, and a list of {@link Variation}s, in that order. 
 * A Variant server can manage any number of variation schemata.
 * 
 * @since 0.5
 */
public interface Schema {

	/**
	 * This schema's <code>/meta</code> element.
	 * 
	 * @return An object of type {@link Meta}
	 * @since 0.9
	 */
	Meta getMeta();

	/**
	 * This schema's <code>/states</code> element.
	 * 
	 * @return An immutable list of {@link State} objects in order they were defined in the variation schema.
	 * @since 0.5
	 */
	List<State> getStates();

	/**
	 * Get a state by its name.
	 * 
	 * @param name The state name of interest
	 * @return An {@link Optional}, containing the state with the given name, if exists, or empty 
	 * if no such state is present in the schema.
	 * 
	 * @since 0.5
	 */
	Optional<State> getState(String name);

	/**
	 * This schema's <code>/variations</code> element.
	 * 
	 * @return An immutable list of {@link Variation} objects in order they were defined in the variation schema.
	 */
	List<Variation>getVariations();
	
	/**
	 * Get a variation by its name.
	 * 
	 * @param name The variation name of interest
	 * @return An {@link Optional}, containing the variation with the given name, if exists, or empty 
	 * if no such variation is present in the schema.
     *
	 * @since 0.5
	 */
	Optional<Variation> getVariation(String name);

}
