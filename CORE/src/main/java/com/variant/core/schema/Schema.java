package com.variant.core.schema;

import java.util.List;


/**
 * Representation of an XDM schema. A complete description of a set of experiments running on
 * a Variant server. Has two top level entities: {@link State}s and {@link Test}s. A schema
 * object is instantiated by the schema parser which reads and parses the schema file.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface Schema {

	/**
	 * <p>This schema's name.
	 * 
	 * @return Schema name, as provided in the meta clause.
	 * @since 0.7
	 */
	public String getName();

	/**
	 * <p>This schema's comment.
	 * 
	 * @return Schema comment, as provided in the meta clause.
	 * @since 0.7
	 */
	public String getComment();
	
	/**
	 * <p>List of user hooks with the schema domain.
	 * 
	 * @return A list of {@link Hook} objects in the ordinal order.
	 * @since 0.7
	 */
	public List<Hook> getHooks();

	/**
	 * <p>This schema's ID.
	 * 
	 * @return Schema ID.
	 * @since 0.6
	 */
	public String getId();

	/**
	 * <p>The list of all states in ordinal order, the order in which they were defined.
	 * 
	 * @return A list of {@link State} objects.
	 * @since 0.5
	 */
	public List<State> getStates();

	/**
	 * <p>Get a state by name. State names are case sensitive.
	 * 
	 * @param name The name of the state of interest.
	 * @return State with the given name, or null if none. 
	 * @since 0.5
	 */
	public State getState(String name);

	/**
	 * <p>The list of all tests in ordinal order, the order in which they were defined.
	 * 
	 * @return A list of {@link Test} objects.
	 */
	public List<Test>getTests();
	
	/**
	 * <p>Get a test by name. Test names are case sensitive.
	 * 
	 * @param name The name of the test of interest.
	 * @return Test with the given name, or null if none.
	 * @since 0.5
	 */
	public Test getTest(String name);
	
}
