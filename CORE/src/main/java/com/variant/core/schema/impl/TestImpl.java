package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.impl.VariantSpace;
import com.variant.core.schema.Test;
import com.variant.core.schema.View;

/**
 * 
 * @author Igor
 *
 */
public class TestImpl implements Test {

	// As defined:
	private String name;
	private boolean isOn = true;
	private int idleDaysToLive = VariantProperties.getInstance().targetingPersisterIdleDaysToLive();
	private List<TestImpl> covariantTests;
	private List<TestExperienceImpl> experiences;
	private VariantSpace variantSpace;
	private List<TestOnViewImpl> onViews;
	private List<Targeter> customTargeters = new ArrayList<Targeter>();
	
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
	 * @param isOn
	 */
	void setIsOn(boolean isOn) {
		this.isOn = isOn;
	}
	
	/**
	 * 
	 * @param isOn
	 */
	void setIdleDaysToLive(int days) {
		this.idleDaysToLive = days;
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
	public List<OnView> getOnViews() {
		return (List<Test.OnView>) (List<?>) Collections.unmodifiableList(onViews);
	}

	/**
	 * 
	 */
	@Override
	public OnView getOnView(View view) {
		for (OnView tov: onViews) if (tov.getView().equals(view)) return tov;
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
	public boolean isDisjointWith(Test other) {
		
		if (this.equals(other)) throw new IllegalArgumentException("Argument cannot be equal to this");
		
		for (OnView thisOnView: getOnViews()) {
			if (thisOnView.isNonvariant()) continue;
			for (OnView otherOnView: other.getOnViews()) {
				if (otherOnView.isNonvariant()) continue;
				if (thisOnView.getView().equals(otherOnView.getView())) return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 */
	@Override
	public void registerCustomTargeter(Targeter targeter) {
		customTargeters.add(targeter);
	}
	
	/**
	 * 
	 */
	@Override
	public List<Targeter> getCustomTargeters() {
		return Collections.unmodifiableList(customTargeters);
	}
	
	/**
	 * 
	 */
	@Override
	public void clearCustomTargeters() {
		customTargeters = new ArrayList<Targeter>();
	}

	/**
	 * 
	 */
	public boolean isOn() {
		return isOn;
	}
	
	/**
	 * 
	 */
	public int getIdleDaysToLive() {
		return idleDaysToLive;
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

	/**
	 * Target this test, i.e. generate an experience.
	 * @return
	 */
	public Experience target(VariantSession session) {
		Experience result = null;
		for (Targeter t: customTargeters) {
			result = t.target(this, session);
			if (result != null) break;
		}
		if (result == null) {
			result = new TestTargeterDefault().target(this, session);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

