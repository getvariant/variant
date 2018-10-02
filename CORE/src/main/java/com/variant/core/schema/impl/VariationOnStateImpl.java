package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.List;

import com.variant.core.impl.VariantSpace;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.util.immutable.ImmutableList;

/**
 * An element of the onState array of the test definition.
 * @author Igor
 */
public class VariationOnStateImpl implements Variation.OnState {

	private StateImpl state;
	private VariationImpl var;
	private List<StateVariant> variants = new ArrayList<StateVariant>();
	private VariantSpace variantSpace;

	/**
	 */
	public VariationOnStateImpl(VariationImpl var, StateImpl state) {
		this.state = state;
		this.var = var;
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
	public List<StateVariant> getVariants() {
		return variants == null ? null : new ImmutableList<StateVariant>(variants);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//
		
	/**
	 * @param variant
	 */
	public void addVariant(StateVariant variant) {
		variants.add(variant);
	}

	/**
	 * Get (and, if not yet, build) his object's variant space. 
	 * Note that we have the variation and its conjoint set at the time of construction, 
	 * but variants are added one by one later. Caller must only call this when last 
	 * variant was added.
	 * 
	 * @return
	 * @throws VariantRuntimeException 
	 */
	public VariantSpace variantSpace() {
		if (variantSpace == null) variantSpace = new VariantSpace(this);
		return variantSpace;
	}

}
