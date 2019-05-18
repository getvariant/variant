package com.variant.core.schema.impl;

import java.util.Optional;

import com.variant.core.schema.State;
import com.variant.core.schema.StateScopedHook;
import com.variant.core.schema.parser.error.SemanticError.Location;

/**
 * 
 * @author Igor
 *
 */
public class StateScopedHookImpl extends BaseHookImpl implements StateScopedHook {

	private final com.variant.core.schema.State state;
	
	public StateScopedHookImpl(String className, Optional<String> init, Location location, com.variant.core.schema.State state) {
		super(className, init, location);
		this.state = state;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public State getState() {
		return state;
	}


}
