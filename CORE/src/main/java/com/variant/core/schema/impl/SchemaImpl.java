package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import com.variant.core.ViewSelectorByPath;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.View;

/**
 * @author Igor
 */
public class SchemaImpl implements Schema {

	// Views are keyed by name
	private LinkedHashSet<View> views = new LinkedHashSet<View>();
	
	// Tests are keyed by name
	private LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
	
	// Caller may pass a custom view selector.
	private ViewSelectorByPath viewSelector = new ViewSelectorByPath.Default();
	
	/**
	 * Package instantiation.
	 */
	SchemaImpl() {}
	
	/**
	 * Add view to the set.
	 * @param view
	 * @return true if element didn't exist, false if did.
	 */
	boolean addView(View view) {
		return views.add(view);
	}

	/**
	 * Add test to the set.
	 * @param test
	 * @return true if element didn't exist, false if did.
	 */
	boolean addTest(Test test) {
		return tests.add(test);
	}
		
    //---------------------------------------------------------------------------------------------//
	//                                    PUBLIC INTERFACE                                         //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Views in the order they were created as an immutable list.
	 */
	@Override
	public List<View> getViews() {
		ArrayList<View> result = new ArrayList<View>(views.size());
		for (View v: views) {
			result.add(v);
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get a view by name
	 */
	public View getView(String name) {
		for (View view: views) {
			if (view.getName().equals(name)) return view;
		}
		return null;
	}

	/**
	 * Get all tests.
	 */
	@Override
	public List<Test> getTests() {
		ArrayList<Test> result = new ArrayList<Test>(tests.size());
		for (Test test: tests) {
			result.add(test);
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Gat a test by name.
	 */
	@Override
	public Test getTest(String name) {
		for (Test test: tests) {
			if (test.getName().equals(name)) return test;
		}
		return null;
	}

	/**
	 * 
	 */
	@Override
	public View matchViewByPath(String path) {
		
		return viewSelector.select(path, views);
	}

	/**
	 * 
	 */
	@Override
	public void registerCustomViewSelectorByPath(ViewSelectorByPath selector) {
		
		if (selector == null) 
			throw new IllegalArgumentException("Selector cannot be null");
		viewSelector = selector;
	}
	
}
