package com.variant.core.schema;

import java.util.List;

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
	 * Get all tests instrumented on this view.
	 * @return
	 */
	List<Test> getInstrumentedTests();

	/**
	 * Does this view have variants (! isInvariant) in a particular test?
	 * @param test
	 * @return
	 */
	public boolean isInvariantIn(Test test);
}
