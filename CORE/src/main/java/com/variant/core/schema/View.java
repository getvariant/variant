package com.variant.core.schema;

import java.util.List;

import com.variant.core.VariantRuntimeException;

/**
 * 
 * @author Igor
 *
 */
public interface View {

	
	/**
	 * View's declared name
	 * @return
	 */
	public String getName();	
	
	/**
	 *  View's declared control path
	 * @return
	 */
	public String getPath();

	/**
	 * Get all tests instrumented on this view.  Includes both, tests
	 * that have variants on this view as well as those where this
	 * view is invariant invariant.
	 * 
	 * @return list of tests in ordinal order
	 */
	List<Test> getInstrumentedTests();

	/**
	 * Is this view instrumented by a particular test?
	 * @param test
	 * @return
	 */
	public boolean isInstrumentedBy(Test test);

	/**
	 * Does this view have variants (not isInvariant) in a particular test?
	 * @param test
	 * @return
	 * @throws VariantRuntimeException if this view is not instrumented by the given test.
	 */
	public boolean isInvariantIn(Test test);
}
