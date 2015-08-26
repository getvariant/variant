package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.variant.core.Variant;
import com.variant.core.VariantRuntimeException;
import com.variant.core.error.ErrorTemplate;
import com.variant.core.schema.Test;
import com.variant.core.schema.View;

/**
 * 
 * @author Igor
 *
 */
public class ViewImpl implements View {

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

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * Views are held in a HashSet, keyed by view name.
	 */
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof View)) return false;
		return ((View) other).getName().equals(this.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public List<Test> getInstrumentedTests() {
		
		ArrayList<Test> result = new ArrayList<Test>();
		
		for (Test test: Variant.Factory.getInstance().getSchema().getTests()) {
			for (Test.OnView tov: test.getOnViews()) {
				if (tov.getView().equals(this)) result.add(test);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public boolean isInstrumentedBy(Test test) {

		for (Test.OnView tov: test.getOnViews()) {
			if (tov.getView().equals(this)) return true;
		}
		return false;
	}		

	@Override
	public boolean isNonvariantIn(Test test) {

		for (Test.OnView tov: test.getOnViews()) {
			if (tov.getView().equals(this)) return tov.isNonvariant();
		}
		throw new VariantRuntimeException(ErrorTemplate.RUN_VIEW_NOT_INSTRUMENTED_FOR_TEST, name, test.getName());
	}

}
