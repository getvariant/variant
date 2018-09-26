package com.variant.core.schema;

import java.util.List;
import java.util.Optional;


/**
 * Representation of a variation schema. A complete description of a set of experience variations 
 * running on a Variant server. 
 * <p>
 * Any schema has three top level entities: <code>meta</code>, <code>states</code>, and <code>variations</code>, in that order. A schema
 * object is instantiated by the schema parser which reads and parses the schema file.
 * 
 * @since 0.5
 */
public interface Schema {

	/**
	 * <p>This schema's <code>meta</code> section.
	 * 
	 * @return An object of type {@link Meta}
	 * @since 0.9
	 */
	Meta getMeta();

	/**
	 * The list, in ordinal order, of all states in this schema.
	 * 
	 * @return A list of {@link State} objects.
	 * @since 0.5
	 */
	List<State> getStates();

	/**
	 * Get a state by its name. Names are case sensitive.
	 * 
	 * @param name The state name.
	 * @return An {@link Optional}, containing the state with the given name or empty if no such state in the schema.
	 * @since 0.5
	 */
	Optional<State> getState(String name);

	/**
	 * The list, in ordinal order, of all variations in this schema.
	 * 
	 * @return A list of {@link Variation} objects.
	 */
	List<Variation>getVariations();
	
	/**
	 * Get a variation by its name. Names are case sensitive.
	 * 
	 * @param name The variation name.
	 * @return An {@link Optional}, containing the variation with the given name or empty if no such variation in the schema.
	 * @since 0.5
	 */
	Optional<Variation> getVariation(String name);

}
