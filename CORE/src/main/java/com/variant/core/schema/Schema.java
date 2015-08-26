package com.variant.core.schema;

import java.util.List;


/**
 * In-memory representation of the configuration instance.
 * @author Igor
 */
public interface Schema {

	/**
	 * Get a list or all views in the order they were defined.
	 * @return
	 */
	public List<View> getViews();

	/**
	 * Get a view by name.
	 * @param name
	 * @return View or null if not found.
	 */
	public View getView(String name);

	/**
	 * Find the best match by the actual path.
	 * @param path
	 * @return
	 */
	public View matchViewByPath(String path);

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
	
	/**
	 * Callers may register custom view selectors that know more about the nature of the PATH.
	 * If such selector is registered, it will be used by the <code>matchViewByPath()</code> call
	 * above.
	 * 
	 * @param selector
	 */
	public void registerCustomViewSelectorByPath(ViewSelectorByPath selector);

}
