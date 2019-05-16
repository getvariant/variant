package com.variant.core.schema.impl;

import java.util.Optional;

import com.variant.core.schema.Variation;
import com.variant.core.schema.VariationScopedHook;
import com.variant.core.schema.parser.error.SemanticError.Location;

/**
 * 
 * @author Igor
 *
 */
public class VariationHookImpl extends BaseHookImpl implements VariationScopedHook {

	private final com.variant.core.schema.Variation test;
	
	public VariationHookImpl(String className, Optional<String> init, Location location, com.variant.core.schema.Variation test) {
		super(className, init, location);
		this.test = test;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public Variation getVariation() {
		return test;
	}

}
