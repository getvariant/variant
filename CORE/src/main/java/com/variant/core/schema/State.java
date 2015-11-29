package com.variant.core.schema;

import java.util.List;
import java.util.Map;

import com.variant.core.exception.VariantRuntimeException;

/**
 * Representation of a state schema element.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public interface State {

	
	/**
	 * The name of this test.
	 * 
	 * @return The name of this test.
	 * @since 0.5
	 */
	public String getName();	
	
	/**
	 * States's all state parameters as a map. Each parameter is a key-value pair where
	 * both key and value are strings.
	 *
	 * @return A map of state parameters.
	 * @since 0.5
	 */
	public Map<String,String> getParameterMap();

	/**
	 * All tests instrumented on this state.  Includes both, tests
	 * that have variants for this state as well as those where this
	 * state is nonvariant.
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
