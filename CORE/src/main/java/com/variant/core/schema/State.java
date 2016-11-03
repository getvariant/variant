package com.variant.core.schema;

import java.util.List;

import com.variant.core.exception.VariantRuntimeException;

/**
 * Representation of State XDM element.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface State {

	/**
	 * The schema that created this State.
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
	 * Value of a state parameter.
	 * 
	 * @return The value of a state parameter, if declared by this state, null otherwise.
	 * @since 0.6
	 */
	public String getParameter(String name);

	/**
	 * <p>All tests instrumented on this state. Comprises all declared instrumentations.
	 *  
	 * @return List of tests in ordinal order, i.e. order they were defined.
	 * @since 0.5
	 */
	List<Test> getInstrumentedTests();

	/**
	 * Is this state instrumented by a particular test? In other words, is a test contained in the
	 * return value of {@link #getInstrumentedTests()}.
	 * 
	 * @param test The test of interest.
	 * @return
	 * @since 0.5
	 */
	public boolean isInstrumentedBy(Test test);

	/**
	 * Is this state non-variant in a particular test?
	 * 
	 * @param test The test of interest.
	 * 
	 * @return True if this state is non-variant in the given test.
	 * @throws VariantRuntimeException if this state is not instrumented by the given test.
	 * @since 0.5
	 */
	public boolean isNonvariantIn(Test test);
}
