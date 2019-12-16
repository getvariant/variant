package com.variant.share.schema.impl;

import java.util.Optional;

import com.variant.share.schema.Variation;
import com.variant.share.schema.VariationScopedHook;
import com.variant.share.schema.parser.error.SemanticError.Location;

/**
 * 
 * @author Igor
 *
 */
public class VariationScopedHookImpl extends BaseHookImpl implements VariationScopedHook {

	private final com.variant.share.schema.Variation test;
	
	public VariationScopedHookImpl(String className, Optional<String> init, Location location, com.variant.share.schema.Variation test) {
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
