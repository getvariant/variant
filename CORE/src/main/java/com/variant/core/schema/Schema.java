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
	 * <p>This schema's ID. Each deployed schema has a unique ID.
	 * 
	 * @return Schema ID.
	 * @since 0.6
	 */
	public String getId();

	/**
	 * <p>The list of all states in ordinal order, i.e. the order in which they were defined.
	 * 
	 * @return A list of {@link State} objects.
	 * @since 0.5
	 */
	public List<State> getStates();

	/**
	 * <p>Get a state by name.
	 * 
	 * @param name The name of the state of interest.
	 * @return State or null if none with the given name.
	 * @since 0.5
	 */
	public State getState(String name);

	/**
	 * <p>The list of all tests in ordinal order, i.e. the order in which they were defined.
	 * 
	 * @return A list of {@link Test} objects.
	 */
	public List<Test>getTests();
	
	/**
	 * <p>Get a test by name.
	 * 
	 * @param name The name of the test of interest.
	 * @return Test or null if none with the given name.
	 * @since 0.5
	 */
	public Test getTest(String name);
	
}
