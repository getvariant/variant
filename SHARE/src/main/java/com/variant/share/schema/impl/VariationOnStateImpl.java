package com.variant.share.schema.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.variant.share.schema.State;
import com.variant.share.schema.StateVariant;
import com.variant.share.schema.Variation;
import com.variant.share.schema.Variation.Experience;
import com.variant.share.util.immutable.ImmutableSet;

/**
 * An element of the onState array of the test definition.
 * @author Igor
 */
public class VariationOnStateImpl implements Variation.OnState {

	private final StateImpl state;
	private final VariationImpl var;
	private final VariantSpace variantSpace;

	/**
	 */
	public VariationOnStateImpl(VariationImpl var, StateImpl state) {
		this.state = state;
		this.var = var;
		variantSpace = new VariantSpace(this);
	}
			
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	@Override
	public State getState() {
		return state;
	}
	
	@Override
	public Variation getVariation() {
		return var;
	}
		
	@Override
	public Set<StateVariant> getVariants() {
		return new ImmutableSet<StateVariant>(variantSpace.getAll());
	}
	
	@Override
	public Optional<StateVariant> getVariant(Experience exp1st, Experience...exprest) {
		HashSet<Experience> coordinates = new HashSet<Experience>();
		coordinates.add(exp1st);
		for (Experience exp: exprest) coordinates.add(exp);
		return Optional.ofNullable(variantSpace.get(coordinates));
	}

	//---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//
		
	public Optional<StateVariant> getVariant(Set<Experience> coordinates) {
		return Optional.ofNullable(variantSpace.get(coordinates));
	}

	/**
	 * 
	 * @param variant
	 */
	public void addVariant(StateVariant variant) {
		variantSpace.addVariant(variant);
	}

	/**
	  *
	 */
	@Override
	public String toString() {
		return state.getName();
	}
}
