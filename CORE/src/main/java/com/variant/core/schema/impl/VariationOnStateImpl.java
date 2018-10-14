package com.variant.core.schema.impl;

import java.util.Set;

import com.variant.core.impl.VariantSpace;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.util.immutable.ImmutableSet;

/**
 * An element of the onState array of the test definition.
 * @author Igor
 */
public class VariationOnStateImpl implements Variation.OnState {

	private final StateImpl state;
	private final VariationImpl var;
	//private Set<StateVariant> variants = new LinkedHashSet<StateVariant>();
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
		
	/**
	 * 
	 * @return
	 */
	@Override
	public Set<StateVariant> getVariants() {
		return new ImmutableSet<StateVariant>(variantSpace.getAll());
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//
		
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
