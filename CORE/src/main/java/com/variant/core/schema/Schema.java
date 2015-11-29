package com.variant.core.schema;

import java.util.List;


/**
 * Representation of an experiment schema.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface Schema {

	/**
	 * The list of all states in ordinal order, i.e. the order in which they were defined.
	 * 
	 * @return A list of {@link State} objects.
	 * @since 0.5
	 */
	public List<State> getStates();

	/**
	 * Get a state by name.
	 * 
	 * @param name The name of the state of interest.
	 * @return State or null if none with the given name.
	 * @since 0.5
	 */
	public State getState(String name);

	/**
	 * The list of all tests in ordinal order, i.e. the order in which they were defined.
	 * 
	 * @return A list of {@link Test} objects.
	 */
	public List<Test>getTests();
	
	/**
	 * Get a test by name.
	 * 
	 * @param name The name of the test of interest.
	 * @return Test or null if none with the given name.
	 * @since 0.5
	 */
	public Test getTest(String name);
	
}
