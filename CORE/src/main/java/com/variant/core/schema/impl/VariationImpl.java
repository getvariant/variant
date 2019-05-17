package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.variant.core.error.VariantException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;
import com.variant.core.schema.VariationScopedHook;

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
	private Optional<List<Variation>> conjointVariations = Optional.empty();
	private List<VariationExperienceImpl> experiences;
	//private VariantSpace variantSpace;
	private List<VariationOnStateImpl> onStates;
	
	// Hooks are keyed by name.
	private Optional<List<VariationScopedHook>> hooks = Optional.empty();

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
	public Optional<OnState> getOnState(State state) {
		for (OnState vos: onStates) if (vos.getState().equals(state)) return Optional.of(vos);
		return Optional.empty();
	}

	/**
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Optional<List<Variation>> getConjointVariations() {
		return conjointVariations;
	}
	
	/**
	 */
	@Override
	public boolean isConcurrentWith(Variation other) {
			
		for (OnState thisOnState: getOnStates()) {
			for (OnState otherOnState: other.getOnStates()) 
				if (thisOnState.getState().equals(otherOnState.getState())) 
					// We found a state instrumented by both variations.
					return true;
		}
		return false;
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
	public Optional<List<VariationScopedHook>> getHooks() {
		return hooks.map(hooks -> Collections.unmodifiableList(hooks));
	}

	@Override
	public boolean isConjointWith(Variation other) {
		VariationImpl otherImpl = (VariationImpl) other;
		return conjointVariations.isPresent() && conjointVariations.get().contains(other) || 
			   otherImpl.conjointVariations.isPresent() && otherImpl.conjointVariations.get().contains(this);		
	}

	//---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param experiences
	 */
	public void setExperiences(List<VariationExperienceImpl> experiences) {
		this.experiences = experiences;
	}
	
	/**
	 * 
	 * @param add the onStates list.
	 */
	public void addOnStates(List<VariationOnStateImpl> onStates) {
		this.onStates = onStates;
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
	public void setConjointVariations(List<Variation> conjointVariations) {
		this.conjointVariations = Optional.of(conjointVariations);
	}
	
	/**
	 * 
	 * @return
	 *
	public VariantSpace getVariantSpace() {
		return variantSpace;
	}

	/**
	 * Add a life-cycle hook to this test
	 * @param hook
	 * @return true if hook didn't exist, false if did.
	 */
	public void setHooks(List<VariationScopedHook> hooks) {
		this.hooks = Optional.of(hooks);
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

