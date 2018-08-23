package com.variant.core.schema;

import java.util.List;
import java.util.Map;

/**
 * Representation of the <code>state</code> schema property.
 * 
 * @since 0.5
 */
public interface State {

	/**
	 * The schema object containing this state object.
	 * 
	 * @return An object of type {@link Schema}
	 * @since 0.6
	 */
	public Schema getSchema();
	
	/**
	 * The name of this test.
	 * 
	 * @return The name of this test.
	 * @since 0.5
	 */
	public String getName();	
	
	/**
	 * Immutable map of sate parameters defined by this state.
	 * 
	 * @return Immutable map of sate parameters defined by this state.
	 * @since 0.6
	 */
	public Map<String,String> getParameters();

	/**
	 * <p>List of tests which instrument this state.
	 *  
	 * @return List of tests in the order they were defined.
	 * @since 0.5
	 */
	List<Test> getInstrumentedTests();

	/**
	 * Returns <code>true</code> if this state is instrumented by a given test. In other words,
	 * is a given test contained in the return value of {@link #getInstrumentedTests()}?
	 * 
	 * @param test The test of interest.
	 * @return true if this state is instrumented by the given test, false otherwise. 
	 * @since 0.5
	 */
	public boolean isInstrumentedBy(Test test);

	/**
	 * Returns <code>true</code> if this state is declared as non-variant in a given test. 
	 * It is the responsibility of the caller to ensure that this state is instrumented by the given test, 
	 * i.e. that {@link #isInstrumentedBy(Test)} returns true.
	 * 
	 * @param test The test of interest.
	 * 
	 * @return <code>true</code> if this state is non-variant in the given test, <code>false</code> if this test.
	 * @throws VariantRuntimeException if this state is not instrumented by the given test.
	 * @since 0.5
	 */
	public boolean isNonvariantIn(Test test);
	
	/**
	 * <p>List of state-scoped life-cycle hooks defined with this state.
	 * 
	 * @return A list of {@link Hook} objects in the ordinal order.
	 * @since 0.8
	 */
	public List<Hook> getHooks();

}
