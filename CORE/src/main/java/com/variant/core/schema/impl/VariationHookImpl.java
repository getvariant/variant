package com.variant.core.schema.impl;

import com.variant.core.schema.Hook;

/**
 * 
 * @author Igor
 *
 */
public class VariationHookImpl extends BaseHookImpl implements Hook.Variation {

	private final com.variant.core.schema.Variation test;
	
	public VariationHookImpl(String className, String init, com.variant.core.schema.Variation test) {
		super(className, init);
		this.test = test;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public com.variant.core.schema.Variation getVariation() {
		return test;
	}

}
