package com.variant.core.schema.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.variant.core.VariantInternalException;
import com.variant.core.runtime.VariantSpace;
import com.variant.core.schema.Test;

/**
 * 
 * @author Igor
 *
 */
public class TestImpl implements Test {

	// As defined:
	private String name;
	private List<TestImpl> covariantTests;
	private List<TestExperienceImpl> experiences;
	private VariantSpace variantSpace;
	private List<TestOnViewImpl> onViews;
	
	// Runtime will cache stuff in this instance.
	private HashMap<String, Object> runtimeAttributes = new HashMap<String, Object>();
	
	/**
	 * 
	 * @param name
	 */
	TestImpl(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @param experiences
	 */
	void setExperiences(List<TestExperienceImpl> experiences) {
		this.experiences = experiences;
	}
	
	/**
	 * 
	 * @param onViews
	 */
	void setOnViews(List<TestOnViewImpl> onViews) {
		this.onViews = onViews;
	}
	
	/**
	 * 
	 * @param tests
	 */
	void setCovariantTests(List<TestImpl> covarTests) {
		this.covariantTests = covarTests;
	}
	
	/**
	 * 
	 * @return
	 */
	VariantSpace getVariantSpace() {
		return variantSpace;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * View's declared name
	 * @return
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Experience> getExperiences() {
		return  (List<Experience>) (List<?>) Collections.unmodifiableList(experiences);
	}

	/**
	 * 
	 */
	@Override
	public Experience getExperience(String name) {
		for (TestExperienceImpl e: experiences) {
			if (e.getName().equalsIgnoreCase(name)) return e;
		}
		return null;
	}
	
	/**
	 * 
	 */
	@Override
	public Experience getControlExperience() {
		for (TestExperienceImpl e: experiences) {
			if (e.isControl()) return e;
		}
		throw new VariantInternalException("No control experience found in test [' + getName() + ']");
	}

	/**
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Test.OnView> getOnViews() {
		return (List<Test.OnView>) (List<?>) Collections.unmodifiableList(onViews);
	}

	/**
	 * Tests are held in a HashSet, keyed by test name.
	 */
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof Test)) return false;
		return ((Test) other).getName().equalsIgnoreCase(this.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Covariant tests declared by this test.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Test> getCovariantTests() {
		return (List<Test>) (List<?>) Collections.unmodifiableList(covariantTests);
	}
	
	/**
	 * 
	 */
	@Override
	public boolean isDisjointWith(Test other) {
		
		if (this.equals(other)) throw new IllegalArgumentException("Argument cannot be equal to this");
		
		for (OnView thisOnView: getOnViews()) {
			if (thisOnView.isInvariant()) continue;
			for (OnView otherOnView: other.getOnViews()) {
				if (otherOnView.isInvariant()) continue;
				if (thisOnView.getView().equals(otherOnView.getView())) return false;
			}
		}
		return true;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                    PUBLIC EXTENSION                                         //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Get а runtime attribute.  These are intended to be attributes not directly contained in the
	 * schema definitions, but computed at run time, and only valid for the lifetime of the schema, i.e.
	 * of this object.
	 * 
	 * @param key
	 * @return
	 */
	public synchronized Object getRuntimeAttribute(String key) {
		return runtimeAttributes.get(key);
	}
	
	/**
	 * Put runtime attribute
	 * @param key
	 * @return Object previously associated with this key, or null if none.
	 */
	public synchronized Object putRuntimeAttribute(String key, Object attribute) {
		return runtimeAttributes.put(key, attribute);
	}

}

