package com.variant.core.schema.impl;

import java.util.Optional;

import com.variant.core.schema.Hook;
import com.variant.core.schema.parser.error.SemanticError.Location;

/**
 * 
 * @author Igor
 *
 */
public class StateHookImpl extends BaseHookImpl implements Hook.State {

	private final com.variant.core.schema.State state;
	
	public StateHookImpl(String className, Optional<String> init, Location location, com.variant.core.schema.State state) {
		super(className, init, location);
		this.state = state;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public com.variant.core.schema.State getState() {
		return state;
	}


}
