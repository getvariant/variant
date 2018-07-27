package com.variant.core.schema;

import java.util.List;


/**
 * Representation of a variation schema. A complete description of a set of experience variations 
 * running a Variant server. 
 * <p>
 * Any schema three top level entities: {@link Meta} {@link State}s and {@link Test}s. A schema
 * object is instantiated by the schema parser which reads and parses the schema file.
 * 
 * @since 0.5
 */
public interface Schema {

	/**
	 * <p>This schema's <code>meta</code> section.
	 * 
	 * @return Schema name, as provided in the meta clause.
	 * @since 0.9
	 */
	Meta getMeta();

	/**
	 * <p>The list of all states in ordinal order, the order in which they were defined.
	 * 
	 * @return A list of {@link State} objects.
	 * @since 0.5
	 */
	List<State> getStates();

	/**
	 * <p>Get a state by name. State names are case sensitive.
	 * 
	 * @param name The name of the state of interest.
	 * @return State with the given name, or null if none. 
	 * @since 0.5
	 */
	State getState(String name);

	/**
	 * <p>The list of all tests in ordinal order, the order in which they were defined.
	 * 
	 * @return A list of {@link Test} objects.
	 */
	List<Test>getTests();
	
	/**
	 * <p>Get a test by name. Test names are case sensitive.
	 * 
	 * @param name The name of the test of interest.
	 * @return Test with the given name, or null if none.
	 * @since 0.5
	 */
	Test getTest(String name);
	
}
