package com.variant.core.config;

import java.util.List;


/**
 * In-memory representation of the configuration instance.
 * @author Igor
 */
public interface Config {

	/**
	 * Get a list or all views in the order they were defined.
	 * @return
	 */
	public List<View> getAllViews();

	/**
	 * Get a view by name.
	 * @param name
	 * @return View or null if not found.
	 */
	public View getView(String name);
	
	/**
	 * Get a list of all tests in the order they were declared.
	 * @return
	 */
	public List<Test>getAllTests();
	
	/**
	 * Get a test by name.
	 * @param name
	 * @return
	 */
	public Test getTest(String name);
}
