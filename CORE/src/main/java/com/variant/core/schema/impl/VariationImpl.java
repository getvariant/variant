package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import com.variant.core.impl.VariantException;
import com.variant.core.impl.VariantSpace;
import com.variant.core.schema.Hook;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;

/**
 * 
 * @author Igor
 *
 */
public class VariationImpl implements Variation {
	
	// As defined:
	private Schema schema;
	private String name;
	private boolean isOn = true;
	private List<VariationImpl> conjointTests;
	private List<TestExperienceImpl> experiences;
	private VariantSpace variantSpace;
	private List<VariationOnStateImpl> onStates;
	
	// Hooks are keyed by name.
	private LinkedHashSet<Hook> hooks = new LinkedHashSet<Hook>();

	// Runtime will cache stuff in this instance.
	private HashMap<String, Object> runtimeAttributes = new HashMap<String, Object>();

	/**
	 * 
	 * @param name
	 */
	public VariationImpl(Schema schema, String name) {
		this.schema = schema;
		this.name = name;
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
		return (List<Experience>) (List<?>) Collections.unmodifiableList(experiences);
	}

	/**
	 */
	@Override
	public Optional<Experience> getExperience(String name) {
		for (Experience exp: experiences) if (exp.getName().equals(name)) return Optional.of(exp);
		return Optional.empty();
	}

	/**
	 */
	@Override
	public Experience getControlExperience() {
		for (Experience exp: experiences) if (exp.isControl()) return exp;
		throw new VariantException.Internal(
				String.format("No control experience found for variation [%s] in schema [%s]", name, schema.getMeta().getName()));
	}

	/**
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<OnState> getOnStates() {
		return (List<Variation.OnState>) (List<?>) Collections.unmodifiableList(onStates);
	}

	/**
	 */
	@Override
	public OnState getOnState(State view) {
		for (OnState tov: onStates) if (tov.getState().equals(view)) return tov;
		return null;
	}

	/**
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Variation> getConjointTests() {
		if (conjointTests == null) return null;
		return (List<Variation>) (List<?>) Collections.unmodifiableList(conjointTests);
	}
	
	/**
	 */
	@Override
	public boolean isSerialWith(Variation other) {
		
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

	/**
	 * 
	 */
	@Override
	public List<Hook> getHooks() {
		ArrayList<Hook> result = new ArrayList<Hook>(hooks.size());
		result.addAll(hooks);
		return Collections.unmodifiableList(result);
	}

	@Override
	public boolean isConcurrentWith(Variation other) {
		return !isSerialWith(other);
	}

	@Override
	public boolean isConjointWith(Variation other) {
		VariationImpl otherImpl = (VariationImpl) other;
		return conjointTests != null && conjointTests.contains(other) || 
			   otherImpl.conjointTests != null && otherImpl.conjointTests.contains(this);		
	}

	//---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param experiences
	 */
	public void setExperiences(List<TestExperienceImpl> experiences) {
		this.experiences = experiences;
	}
	
	/**
	 * 
	 * @param onViews
	 */
	public void setOnViews(List<VariationOnStateImpl> onViews) {
		this.onStates = onViews;
	}
	
	/**
	 * 
	 * @param isOn
	 */
	public void setIsOn(boolean isOn) {
		this.isOn = isOn;
	}
		
	/**
	 * Caller must ensure that the covarTests are sorted in ordinal order.
	 * @param tests
	 */
	public void setConjointTests(List<VariationImpl> covarTests) {
		this.conjointTests = covarTests;
	}
	
	/**
	 * 
	 * @return
	 */
	public VariantSpace getVariantSpace() {
		return variantSpace;
	}

	/**
	 * Add a life-cycle hook to this test
	 * @param hook
	 * @return true if hook didn't exist, false if did.
	 */
	public boolean addHook(Hook hook) {
		return hooks.add(hook);
	}

	/**
	 * Tests are equal if they have the same name.
	 */
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof Variation)) return false;
		return ((Variation) other).getName().equals(this.getName());
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

