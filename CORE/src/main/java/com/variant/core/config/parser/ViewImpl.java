package com.variant.core.config.parser;

import com.variant.core.VariantInternalException;
import com.variant.core.config.Test;
import com.variant.core.config.View;

/**
 * 
 * @author Igor
 *
 */
class ViewImpl implements View {

	private String name;
	private String path;

	/**
	 * Package scoped constructor;
	 * @param name
	 * @param path
	 */
	ViewImpl(String name, String path) {
		this.name = name;
		this.path = path;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}

	/**
	 * Views are held in a HashSet, keyed by view name.
	 */
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof View)) return false;
		return ((View) other).getName().equalsIgnoreCase(this.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean isInvariantIn(Test test) {

		for (Test.OnView tov: test.getOnViews()) {
			if (tov.getTest().equals(test)) return tov.isInvariant();
		}
		throw new VariantInternalException(String.format("Test [%s] is not instrumented on view [%s]", test.getName(), name));
	}

}
