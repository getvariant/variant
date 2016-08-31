package com.variant.core.xdm.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.variant.core.exception.VariantInternalException;
import com.variant.core.impl.CorePropertiesImpl;
import com.variant.core.impl.VariantSpace;
import com.variant.core.xdm.Schema;
import com.variant.core.xdm.State;
import com.variant.core.xdm.Test;

/**
 * 
 * @author Igor
 *
 */
public class TestImpl implements Test {
	
	// As defined:
	private Schema schema;
	private String name;
	private CorePropertiesImpl properties;
	private boolean isOn = true;
	private List<TestImpl> covariantTests;
	private List<TestExperienceImpl> experiences;
	private VariantSpace variantSpace;
	private List<TestOnStateImpl> onViews;
	
	// Runtime will cache stuff in this instance.
	private HashMap<String, Object> runtimeAttributes = new HashMap<String, Object>();

	/**
	 * 
	 * @param name
	 */
	TestImpl(Schema schema, String name, CorePropertiesImpl properties) {
		this.schema = schema;
		this.name = name;
		this.properties = properties;
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
	void setOnViews(List<TestOnStateImpl> onViews) {
		this.onViews = onViews;
	}
	
	/**
	 * 
	 * @param isOn
	 */
	void setIsOn(boolean isOn) {
		this.isOn = isOn;
	}
		
	/**
	 * Caller must ensure that the covarTests are sorted in ordinal order.
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
	 */
	@Override
	public Schema getSchema() {
		return schema;
	}

	/**
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
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
	public List<OnState> getOnStates() {
		return (List<Test.OnState>) (List<?>) Collections.unmodifiableList(onViews);
	}

	/**
	 * 
	 */
	@Override
	public OnState getOnView(State view) {
		for (OnState tov: onViews) if (tov.getState().equals(view)) return tov;
		return null;
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
	public boolean isSerialWith(Test other) {
		
		if (this.equals(other)) throw new IllegalArgumentException("Argument cannot be equal to this");
		
		for (OnState thisOnView: getOnStates()) {
			if (thisOnView.isNonvariant()) continue;
			for (OnState otherOnView: other.getOnStates()) {
				if (otherOnView.isNonvariant()) continue;
				if (thisOnView.getState().equals(otherOnView.getState())) return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 */
	@Override
	public boolean isOn() {
		return isOn;
	}

	@Override
	public boolean isConcurrentWith(Test other) {
		return !isSerialWith(other);
	}

	@Override
	public boolean isCovariantWith(Test other) {
		return covariantTests.contains(other) || ((TestImpl)other).covariantTests.contains(this);		
	}

	//---------------------------------------------------------------------------------------------//
	//                                    PUBLIC EXTENSION                                         //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Tests are equal if they have the same name.
	 */
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof Test)) return false;
		return ((Test) other).getName().equals(this.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
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

	
	@Override
	public String toString() {
		return name;
	}
}
