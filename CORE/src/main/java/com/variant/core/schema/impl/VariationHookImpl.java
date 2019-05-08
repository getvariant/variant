package com.variant.core.schema.impl;

import java.util.Optional;

import com.variant.core.schema.Hook;
import com.variant.core.schema.parser.error.SemanticError.Location;

/**
 * 
 * @author Igor
 *
 */
public class VariationHookImpl extends BaseHookImpl implements Hook.Variation {

	private final com.variant.core.schema.Variation test;
	
	public VariationHookImpl(String className, Optional<String> init, Location location, com.variant.core.schema.Variation test) {
		super(className, init, location);
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
