package com.variant.core.config;

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
	 * Does this view has variants (! isInvariant) in a particular test?
	 * @param test
	 * @return
	 */
	public boolean isInvariantIn(Test test);
}
