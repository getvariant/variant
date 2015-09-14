package com.variant.core.schema;

import java.util.List;


/**
 * In memory representation of experiment meta-data.
 * @author Igor
 */
public interface Schema {

	/**
	 * Get a list or all state in the order they were defined.
	 * @return
	 */
	public List<State> getStates();

	/**
	 * Get a state by name.
	 * @param name
	 * @return State or null if none with given name.
	 */
	public State getState(String name);

	/**
	 * Get a list of all tests in the order they were declared.
	 * @return
	 */
	public List<Test>getTests();
	
	/**
	 * Get a test by name.
	 * @param name
	 * @return
	 */
	public Test getTest(String name);
	
}
