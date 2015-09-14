package com.variant.core.schema;

import java.util.List;
import java.util.Map;

import com.variant.core.exception.VariantRuntimeException;

/**
 * 
 * @author Igor
 *
 */
public interface State {

	
	/**
	 * State's name
	 * @return
	 */
	public String getName();	
	
	/**
	 *  States's all control properties as a map.
	 * @return
	 */
	public Map<String,String> getParameterMap();

	/**
	 * State's control property by name.
	 * @param name
	 * @return
	 */
	public String getParameter(String name);

	/**
	 * Get all tests instrumented on this view.  Includes both, tests
	 * that have variants on this view as well as those where this
	 * view is nonvariant.
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
	 * Is this view nonvariant in a particular test?
	 * @param test
	 * @return
	 * @throws VariantRuntimeException if this view is not instrumented by the given test.
	 */
	public boolean isNonvariantIn(Test test);
}
